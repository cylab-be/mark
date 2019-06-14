
def fuzzySetMembership(x, membershipFunction):
  # required to get "copy by value" behavior,
  #   otherwise we destroy the membershipFunction by popping it
  msf = membershipFunction[:]
  x   = float(x)
  
  # cursor
  prevPnt = None
  nextPnt = msf.pop(0)

  # are we left of the leftmost point in the msf definition  
  if x <= nextPnt[0]:
    return nextPnt[1]

  # advance the cursor until prevPnt < x < nextPnt,
  #   or we reach the end of the membershipFunction definition
  while (x > nextPnt[0]) and msf:
    prevPnt = nextPnt
    nextPnt = msf.pop(0)

  # if we are within the last internal,
  if x < nextPnt[0]:
    # interpolate
    return prevPnt[1] + (nextPnt[1]-prevPnt[1]) * (x-prevPnt[0]) / (nextPnt[0]-prevPnt[0])
  else:
    # otherwise we are to the right of the rightmost point in the definition
    return nextPnt[1]
  
def fuzzyAnd(x, y):
  return min(x, y)

def fuzzyOr(x, y):
  return max(x, y)
  
