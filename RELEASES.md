# MARK Releases

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
