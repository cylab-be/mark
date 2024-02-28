# API MARk Server Doc

jsonrpc API

## test

### parametre

No parameter needed

### response

String

## testString

### parametre

* String data

### response

No Response  information send

## addRawData

Add raw data to the datastore and eventually trigger analysis.

### parametre

* RawData data

### response

No Response  information send

## addEvidence

Add evidence to the datatstore

### parametre

* Evidence evidence

### response

No Response  information send

## addFile

### parametre

* byte[] bytes
* String filename

### response

ObjectId

## findFile

### parametre

* ObjectId file_id

### response

byte[]

##  findLastRawData

Find the last data records that were inserted in DB.

### parametre

No parameter needed

### response
RawData[]
## findRawData

### parametre

* String label
* Map<String, String> subject
* long from
* long till

### response

RawData[]

## findEvidence

### parametre

* String label
* Map<String, String> subject

### response

Evidence[]

## findEvidenceSince

Find all evidences for a specific detector and specific subject since a given date.

### parametre

* String label*,
* Map<String, String> subject
* long time

### response

Evidence[]

## findEvidenceSince

 Find all evidences for a specific detector since a given date.

### parametre

* String label,
* long time

### response
Evidence[]
## findEvidenceSince

Find all evidences produced since a given date.

### parametre

* long time

### response

Evidence[]

## findEvidence

Find the evidences with highest score, for given label and for all subjects. Used to display the most suspicious subjects.

### parametre

* String label

### response

Evidence[]

##  findEvidence

Find evidence of given label, for all subjects.

### parametre

* String label,
* int page

### response
Evidence[]
## findEvidenceById

Get a single evidence by id.

### parametre

* String id

### response

Evidence

## getURL

### parametre

No parameter needed

### response

URL

## findLastEvidences

Find the last evidences that were inserted in the DB.

### response

Evidence[]

## findLastEvidences

Find the evidences according to a pattern (that start with provided pattern), and if multiple evidences are found with same label, return the most recent one.

### parametre

* String label,
* Map<String, String> subject

### response

Evidence[]

## findEvidenceForPeriodAndInterval

 Find all evidences during a specific period,reponse: information on how many evidences were produced by specific agent for a given time.

### parametre

* int period,
* int interval

### response

Evidence[][]

## Object getFromCache

 get value from cache represented by a map.

### parametre

* String key

### response

value.

## storeInCache


 Store the value in the cache with the key key.

### parametre

* String key,
* Object value

### response

No Response  information send

## compareAndSwapInCache

 Compare and swap verify if the current stored value in the cache is
 equals to old_value, or if the value has never been stored in the cache
 for this key. Since multiple agents can get access to the cache, We do
 this verification to not overwrite new values from other agents.

### parametre

* String key,
* Object new_value,
* Object old_value

### response

true if it's swaped

## status

Get the status of MARK (running, ram, CPU load, version ...).

### parametre

No parameter needed

### response
Map<String, Object> 

##  activation

Get the current configuration (activation profiles).

### parametre

No parameter needed

### response
DetectionAgentProfile[]
## setAgentProfile(DetectionAgentProfile profile)

Add or update the configuration a detector. If profile.label is already defined, the configuration is updated, otherwise a new detector is added.

### parametre

No parameter needed

### response

No Response  information send

## sources

Get the configuration of data sources.

### parametre

No parameter needed

### response
DataAgentProfile[]
## pause

Pause execution (no algorithm will be scheduled).

### parametre

No parameter needed

### response

No Response  information send

## resume

Resume execution.

### parametre

No parameter needed

### response

No Response  information send

## reload

Reload the directory containing detection agents profiles.

### parametre

No parameter needed

### response

No Response  information send

## restart

Dangerous! Restart the server: wipe the DB and restart the data agents.

### parametre

No parameter needed

### response

No Response  information send

## history

Get the last previous status objects.
### parametre

No parameter needed

### response
List<Map>