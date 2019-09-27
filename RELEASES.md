# MARK Releases

## 0.0.27

JSON-RPC:

* only one status() method that replaces executorStatus() and dbStatus()
* added history() method that return status for the last 24h (used to show graphs on the UI)


UI:

* multiple graphs and info on the status page

## 0.0.26

JSON-RPC:

* findData must provide time limits
* findEvidence(label) without page returns all evidences
* add pause and resume methods

UI:

* pagination starts at page 1
* add button to pause / resume detection
* add fontawesome
* updated to bootstrap 4
* show much more stats

Detection:

* added "max" detector
* added "threshold" detector


## 0.0.25

* fix default config file
* show chart of score computed by a detector for given subject
* reports shows profile (parameters) of detector
* reports show data query executed by detector
* allow to inspect data queried by detector
* fix pagination bug

## 0.0.22

* fix data agents starting bug
* fix loading of jars from modules directory
* multiple code cleaning
* to run demo: ./example/run.sh
* automated deployment test


## 0.0.20

* multiple cleaning
* checkstyle + PMD + jacoco code coverage on some modules
* allow packages renamed to be.cylab.mark.*
* executor is now an interface, so we can easily swtich away from Apache Ignite if required
* upgrade multiple dependencies : Apache Ignite, surefire, failsafe, javadoc, maven-jar
* support java 7 to 11 (and tested in gitlab)
* label matching is now implemented with a regex
* Remove PHP code and use SparkJava instead
* show history of detection agent in UI
* use pagination on homepage
* allow to add references in detection report and show them in UI
