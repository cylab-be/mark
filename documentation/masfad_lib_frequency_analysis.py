# for the frequency analysis signal processing
import scipy
import scipy.fftpack
import scipy.signal
import numpy

# for the plot (first 2 lines needed for offline processing)
import sys
if not "matplotlib" in sys.modules:
  import matplotlib
  matplotlib.use('Agg')
import pylab
from datetime import datetime
import StringIO, base64

# plot settings
showSignal = True
showSpectrum = True
showSmoothing = True
showZoom = True
showPeaks = True
showThreshold = True
showWithoutZeroPeak = True

# some constants
fftThresholdAboveAverage = 2
debug = False

def findPeriodicityWithParams(timestamps, timeStep, timePeriod, plotFilename):
  encodedImage = None

  # convert timestamps into time differences
  firstTimestamp = float(timestamps.pop(0))
  timeDifferences = [ float(i)-firstTimestamp for i in timestamps ]

  if debug:
    print "time differences:", timeDifferences

  # derive the "signal" vector from the samples
  #   by counting the number of samples per time-bin
  nrOfSamples = timePeriod / timeStep
  reqCount    = nrOfSamples * [ 0 ]
  for value in timeDifferences:
    t = int(value/timeStep) % nrOfSamples
    reqCount [t] += 1

  if debug:
    print "request count:", reqCount

  if plotFilename:
    reqTime  = [ firstTimestamp + x*timeStep for x in range(nrOfSamples) ]

  # remove the DC component
  reqCount -= scipy.mean(reqCount)

  if debug:
    print "remove DC component"
    print "request count:", reqCount

  # compute the fft and the frequency values on the x-axis
  fft   = abs(scipy.fft(reqCount))
  freqs = scipy.fftpack.fftfreq(nrOfSamples, timeStep)

  # keep the positive half of the spectrum
  nrOfSamples = nrOfSamples/2 
  fft   = fft  [range(nrOfSamples)] 
  freqs = freqs[range(nrOfSamples)] 
  
  if plotFilename:
    fftForPlot = fft.copy()

  # smooth the spectrum (for the maximum detection to work)
  #   this is a bit of a kitchen recipy we choose
  fftIn = fft
  for i in range(5):
    fftSmooth = scipy.signal.order_filter(fftIn, [1]*5, 4)
    fftSmooth = scipy.signal.convolve(fftSmooth, [1.0/5]*5, 'same')
    fftIn     = fftSmooth

  if plotFilename:
    fftSmoothForPlot = fftSmooth.copy()

  # threshold we will impose for a "peak" to be valid
  fftThreshold  = fftThresholdAboveAverage * scipy.mean(fftSmooth)

  # find the extent of the peak at zero
  i = 0
  iMax = len(fftSmooth)/4
  while (i<iMax) and (fftSmooth[i]<=fftSmooth[i+1]):
    i = i +1
  i = i +5
  while (i<iMax) and (fftSmooth[i]>=fftSmooth[i+1]):
    i = i +1

  print fftSmooth[0:i+5]
  print "zero peak up to frequency", freqs[i]

  # erase the peak at 0
  mean      = 0
  stdv      = 2 * fftSmooth[0:i].std()
  amplitude = max(fftSmooth[0:i])

  valsToSubtract = numpy.array([ 1/(stdv * numpy.sqrt(2*numpy.pi))*numpy.exp(-(i-mean)**2/(2*stdv**2)) for i in range(nrOfSamples) ])
  valsToSubtract *= amplitude/valsToSubtract[0]
  fftSmooth -= valsToSubtract

  if plotFilename:
    fftWithoutZeroPeakPlot = fftSmooth.copy()

  # find all the maxima above the threshold
  listOfMaxima = []
  maxPeak = 0.0
  while True:
    # locate the peak
    peakPosition = fftSmooth.argmax()
  
    # if peak is below threshold, quit
    if fftSmooth[peakPosition] < fftThreshold:
      break

    # add the peak to the list of maxima
    listOfMaxima.append(peakPosition)

    # keep the value of the first (=highest) peak
    if not maxPeak:
      maxPeak = fftSmooth[peakPosition]

    # erase the peak
    mean      = peakPosition
    stdv      = 2 * fftSmooth[max(0,peakPosition-100):min(peakPosition+100,nrOfSamples-1)].std()
    amplitude = fftSmooth[peakPosition]
    valsToSubtract = numpy.array([ 1/(stdv * numpy.sqrt(2*numpy.pi))*numpy.exp(-(i-mean)**2/(2*stdv**2)) for i in range  (nrOfSamples) ])
  
    valsToSubtract *= amplitude/valsToSubtract[peakPosition]
    fftSmooth -= valsToSubtract

  # did we find any
  if listOfMaxima:
    # sort them and take the lowest one
    listOfMaxima.sort()
    result = [ 1.0/freqs[listOfMaxima[0]], maxPeak/fftThreshold ]
  else:
    result = None

  # plot the graphs
  if plotFilename:
    
    if listOfMaxima:    
      # determine the anotations
      periodForMaximum = 1.0 / freqs[listOfMaxima[0]]
      if periodForMaximum < 5 * 60:
        anotation = 'T=%.1fsec' % (periodForMaximum)
      elif periodForMaximum < 2 * 60 * 60:
        anotation = 'T=%.1fmin' % (periodForMaximum/60)
      elif periodForMaximum < 2 * 24 * 60 * 60:
        anotation = 'T=%.1fhrs' % (periodForMaximum/(60*60))
      else:
        anotation = 'T=%.1fdays' % (periodForMaximum/(24*60*60))
    else:
        anotation = None
        
    tickFontSizeSmall = 8
    tickFontSize = 10
    fig = pylab.figure()

    nrOfSubplots = 0
    if showSignal:
      nrOfSubplots += 1
    if showSpectrum or showSmoothing:
      nrOfSubplots += 1
      if showZoom:
        nrOfSubplots += 1

    iSubplot = 0
    if showSignal:
      iSubplot += 1
  
      maxIndex = nrOfSamples
      #maxIndex = int(1.1 * value/timeStep) % nrOfSamples
      if debug:
        print "nrOfSamples:", nrOfSamples, ", maxIndex:", maxIndex
  
      pylab.subplot(str(nrOfSubplots)+"1"+str(iSubplot))
      pylab.xlabel('time')
      pylab.ylabel('request count')
      pylab.xticks(fontsize=tickFontSizeSmall, rotation=90)
      pylab.yticks(fontsize=tickFontSize)
      pylab.vlines(reqTime[:maxIndex], 0, reqCount[:maxIndex])
      locs,labels = pylab.xticks()
      pylab.xticks(locs, map(lambda x: datetime.fromtimestamp(x).strftime("%d-%m/%H:%M"), locs))

    if showSpectrum or showSmoothing:
      iSubplot += 1
      pylab.subplot(str(nrOfSubplots)+"1"+str(iSubplot))
      pylab.xlabel('frequency')
      pylab.ylabel('magnitude')
      pylab.xticks(fontsize=tickFontSize)
      pylab.yticks(fontsize=tickFontSize)
      pylab.grid(True)
      if showSpectrum:
        pylab.plot(freqs, fftForPlot, "b.")
      if showSmoothing:
        pylab.plot(freqs, fftSmoothForPlot, "r")
      if showWithoutZeroPeak:
        pylab.plot(freqs, fftWithoutZeroPeakPlot, "g")
      if showThreshold:
       pylab.plot([ 0, freqs[-1] ],[ fftThreshold, fftThreshold ], "r")
      if listOfMaxima and showPeaks:
        pylab.ylim(0, 1.4 * fftSmoothForPlot[listOfMaxima[0]]);
        if anotation:
          pylab.annotate(anotation, \
            xy=(freqs[listOfMaxima[0]], fftSmoothForPlot[listOfMaxima[0]]), \
            xycoords='data', xytext=( 30, 10), textcoords='offset points', \
            fontsize=tickFontSize, \
            arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2"), \
            )

    if showZoom and (showSpectrum or showSmoothing):
      iSubplot += 1
      pylab.subplot(str(nrOfSubplots)+"1"+str(iSubplot))

      maxFreqIndex = 500
      pylab.xlabel('frequency')
      pylab.ylabel('magnitude')
      pylab.xticks(fontsize=tickFontSize)
      pylab.yticks(fontsize=tickFontSize)
      pylab.grid(True)
      if showSpectrum:
        pylab.plot(freqs[:maxFreqIndex], fftForPlot[:maxFreqIndex], "b.")
      if showSmoothing:
        print fftSmoothForPlot[:maxFreqIndex]
        pylab.plot(freqs[:maxFreqIndex], fftSmoothForPlot[:maxFreqIndex], "r")
      if showWithoutZeroPeak:
        pylab.plot(freqs[:maxFreqIndex], fftWithoutZeroPeakPlot[:maxFreqIndex], "g")
      if showThreshold:
        pylab.plot([ 0, freqs[maxFreqIndex] ],[ fftThreshold, fftThreshold ], "r")
      if listOfMaxima and showPeaks:
        pylab.ylim(0, 1.4 * fftSmoothForPlot[listOfMaxima[0]]);
        if anotation:
          pylab.annotate(anotation, \
            xy=(freqs[listOfMaxima[0]], fftSmoothForPlot[listOfMaxima[0]]), \
            xycoords='data', xytext=( 30, 10), textcoords='offset points', \
            fontsize=tickFontSize, \
            arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=.2"), \
            )

    # set the figure size 
    pylab.gcf().set_size_inches(16,9)
    
    # save the figure as an image
    imgdata = StringIO.StringIO()
    pylab.savefig(imgdata, format='png', dpi=60, transparent=True)
    imgdata.seek(0)  # rewind the data

    encodedImage = base64.b64encode(imgdata.buf)

  # return the result
  return [ result, encodedImage ]

def printPeriodicity(period):
  if period < 5 * 60:
    print 'peak found for period =', period, 'seconds'
  elif period < 2 * 60 * 60:
    print 'peak found for period =', period/60, 'minutes'
  elif period < 2 * 24 * 60 * 60:
    print 'peak found for period =', period/(60*60), 'hours'
  else:
    print 'peak found for period =', period/(24*60*60), 'days'
  

def findPeriodicity(timestamps, plotFilename):

  #timeStep   = 3600                # in seconds
  #timePeriod = 365 * 24 * 60 * 60  # in seconds
  #result = findPeriodicityWithParams(timestamps, timeStep, timePeriod, plotFilename)
  #if result:
  #  printPeriodicity(result)
  
  timeStep   =   60                 # in seconds
  timePeriod =    7 * 24 * 60 * 60  # in seconds
  result = findPeriodicityWithParams(timestamps, timeStep, timePeriod, plotFilename)
  
  #if result:
  #  printPeriodicity(result[0])
  # print "  with value", result[1]
  return result
  
