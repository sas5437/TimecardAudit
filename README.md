# ADP WFN v11 Timecard Audit
Rudimentary script with simple graphical interface to select the input file and granularity of a certain calculation.

## User Guide
Upon launching the application, the computer may prompt you to confirm that the application is safe to Run.  Check the box to accept the risk and choose to start the application.  Due to a self-signed certificate on the application, this security warning will pop up each time the application is launched.  There is no way to turn this off permanently at this time.
Once the program successfully launches, there will be a small window with 2 buttons: "Run", and "Select File".  

The <strong>ADP WFN Timecard export</strong> file must contain 1 header row. Rows 2 and greater should be data rows containing time-pairs. The following headers must be present without quotation marks: "Position ID", "First Name", "Last Name", "In Time", "Out Time", "Hours", and "Worked Department".  The file should contain time-pairs, meaning that each record should always have an "In Time" and an "Out Time" where both of these values are formatted similar to "01/25/16 14:30" where the first 2 digits are the calendar month, the second 2 digits are day of the month, and the last 2 or 4 digits are the year followed by 1 or 2 digits for the hour and 2 digits for the minute when the time was recorded.  This application assumes there can be multiple time-pairs per employee per day.  This application also assumes that "In Time" and "Out Time" that are recorded before 6:00 AM are to count towards the previous calendar day shift differential and spread of hours calculations.
Use the application in 7 steps.

 * Click the "Select File" button.
 * Choose an ADP WFN Timecard export in the <code>.csv</code> file format.
 * Click "Open".
 * You should now see the filename in the main window.
 * Click "Run".
 * 2 new <code>.csv</code> file format files should have been created, and the application will attempt to open them both up.
 * Analyze the results.

The first file generated is a replica of the ADP WFN Timecard export with an added column containing the shift differential for that time-pair.
The second file generated is a summary file containing one line per employee and total hours, total shift differential hours, and number of times the spread of hours was more than 10 hours.

## Development
Use Gradle to compile and build the project into an executable `.jar` file:  
```
$ brew install gradle
$ gradle build
$ open build/libs/TimecardAudit.jar
```
