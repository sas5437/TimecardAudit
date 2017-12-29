# ADP WFN v11 Timecard Audit
Rudimentary script with simple graphical interface to select the input file and granularity of a certain calculation.

## Development
Use Gradle to compile and build the project into an executable `.jar` file:  
```
$ brew install gradle
$ gradle build
$ java -jar build/libs/TimecardAudit.jar
```
Running without stdout or stderr to get an end-user experience:  
```
$ open build/libs/TimecardAudit.jar
```
