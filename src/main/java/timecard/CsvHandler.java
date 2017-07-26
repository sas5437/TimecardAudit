package timecard;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import timecard.FileNameExtensionError;
import timecard.TimePair;
import timecard.TimeCard;

/*
*  CsvHandler is responsible for processing the contents of the input CSV
*  file into 2 output files: audit/debug CSV, and summary CSV.  The audit CSV
*  is generated so that a user can double check the numbers used to calculate
*  the output generated to the summary CSV.  Exceptions are thrown to the 
*  Controller so that any errors may be displayed to the user through the View.
*/
public class CsvHandler {
  
  File file;
  Integer roundMinutesTo;
  Integer companyCodeHeaderIndex;
  Integer positionIDHeaderIndex;
  Integer lastNameHeaderIndex;
  Integer firstNameHeaderIndex;
  Integer timeInHeaderIndex;
  Integer timeOutHeaderIndex; 
  Integer hoursHeaderIndex;
  Integer payCodeHeaderIndex;
  Integer departmentHeaderIndex;
  HashMap<String, TimeCard> timeCards;

  public CsvHandler() {
    file = null;
    roundMinutesTo = 0;
    timeCards = new HashMap<String, TimeCard>();
  }

  public void setFile(File file) throws FileNameExtensionError {
    if(file == null || file.getName().matches("(?i).*\\.csv$")){
      this.file = file;
    } else {
      throw new FileNameExtensionError(".csv");
    }
  }

  public void setRoundingOption(Integer roundMinutesTo) {
    this.roundMinutesTo = roundMinutesTo;
  }

  public boolean isReady() {
    if(file == null || !file.getName().matches("(?i).*\\.csv$")) {
      return false;
    } else {
      return true;
    }
  }

  public void processFile() throws Exception, IOException {
    BufferedReader csvFile = new BufferedReader(new FileReader(file));
    FileWriter auditFile = new FileWriter("AuditResults.csv");

    // Process header to find columns we need
    String dataRow = csvFile.readLine();
    String[] headerArray = dataRow.split(",");
    setHeaderIndexes(headerArray);

    // Write our Audit CSV Header before processing
    auditFile.write(
      headerArray[positionIDHeaderIndex] + "," +
      headerArray[lastNameHeaderIndex] + "," +
      headerArray[firstNameHeaderIndex] + "," +
      headerArray[departmentHeaderIndex] + "," +
      headerArray[timeInHeaderIndex] + "," +
      headerArray[timeOutHeaderIndex] + "," +
      "Adjusted In Time," +
      "Adjusted Out Time," +
      headerArray[hoursHeaderIndex] + "," +
      "Shift Differential," +
      headerArray[payCodeHeaderIndex] + "\n"
    );

    // Read CSV line, Process information, and write to Audit CSV output, then repeat until EOF
    dataRow = csvFile.readLine();
    int rowNumber = 1;
    TimePair timePair = null;
    while (dataRow != null) {
      rowNumber += 1;
      upsertTimeCards(dataRow.split(","), auditFile, rowNumber);
      dataRow = csvFile.readLine();
    }
    // Close our open files, both input and output
    auditFile.close();
    csvFile.close();
    // Write our output file for the business users - give them what they ask for!
    writeSummaryFile();
  }

  private void writeSummaryFile() throws IOException {
    FileWriter summaryFile = new FileWriter("SummaryResults.csv");
    summaryFile.write("Position ID, Employee Name, Hours Worked, 40+ Hours?, Shift Differential, Spread of Hours\n");
    for(String key : timeCards.keySet()){
      summaryFile.write(
        key + "," +
        timeCards.get(key).getFullName() + "," +
        new DecimalFormat("##.##").format(timeCards.get(key).getTotalHoursWorked()) + "," +
        (timeCards.get(key).getTotalHoursWorked() >= 40 ? "X" : "") + "," +
        new DecimalFormat("##.##").format(timeCards.get(key).getTotalHoursAfterMidnight()) + "," +
        timeCards.get(key).getDaysWithSpread() + "\n"
      );
    }
    summaryFile.close();
  }

  private void writeAudit(FileWriter auditFile, TimeCard timeCard, TimePair timePair) throws IOException {
    auditFile.write(
      timeCard.getPositionId() + "," +
      timeCard.getLastName() + "," +
      timeCard.getFirstName() + "," +
      timePair.getDepartment() + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(timePair.getClockIn()) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(timePair.getClockOut()) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(timePair.getClockInAdjusted()) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(timePair.getClockOutAdjusted()) + "," +
      timePair.getDuration() + "," +
      timePair.getHoursAfterMidnight() + "," +
      timePair.getPayCode() + "\n"
    );
  }


  private void upsertTimeCards(String[] dataArray, FileWriter auditFile, int rowNumber) throws Exception {
    //String key = "";
    String positionId = "";
    String dateKey = "";
    String payCode = "";
    String hoursWorkedAfterMidnight = "0.00";
    Double hours = 0.0;
    LocalDateTime in = null;
    LocalDateTime out = null;
    LocalDateTime adjustedIn = null;
    LocalDateTime adjustedOut = null;
    String department = "";

    // Parsing the input and raising useful exceptions with column and row of problem if identified
    try {
      department = dataArray[departmentHeaderIndex];
    } catch (Exception e) {
      department = "";
    }
    try {
      in = stringToDate(dataArray[timeInHeaderIndex]);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Clock In could not be parsed on line " + rowNumber + " of the CSV.");
    }
    try {
      out = stringToDate(dataArray[timeOutHeaderIndex]);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Clock Out could not be parsed on line " + rowNumber + " of the CSV.");
    }
    try {
      payCode = dataArray[payCodeHeaderIndex];
    } catch(Exception e) {
      e.printStackTrace();
      payCode = "";
    }
    try {
      hours = Double.valueOf(dataArray[hoursHeaderIndex]);
    } catch (Exception e) {
      e.printStackTrace();
      hours = 0.0;
    }
    try {
      positionId = dataArray[positionIDHeaderIndex];
    } catch(Exception e) {
      e.printStackTrace();
      throw new Exception("Could not parse Position Id on line " + rowNumber + ".");
    }

    // if we don't yet have a TimeCard for this positionId, insert a new TimeCard into the ArrayList
    if(timeCards.get(positionId) == null)
      timeCards.put(
          positionId, 
          new TimeCard(
            dataArray[companyCodeHeaderIndex], 
            dataArray[firstNameHeaderIndex], 
            dataArray[lastNameHeaderIndex], 
            positionId)
          );

    try {
      // insert a new TimePair into the TimeCard for the positionId
      TimePair timePair = new TimePair(in, out, department, payCode, hours, roundMinutesTo);
      timeCards.get(positionId).addTimePair(timePair);
      writeAudit(auditFile, timeCards.get(positionId), timePair);
    } catch(Exception e) {
      throw new Exception("Clock In could not be adjusted on line " + rowNumber + " of the CSV.");
    }
  }

  private LocalDateTime stringToDate(String string) throws Exception {
    try {
      string = string.toUpperCase();
      DateTimeFormatter formatter = null;
      if(string.matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{4}\\s.*")){
        formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
      } else if(string.matches("^\\d{1,2}\\/\\d{1,2}\\/\\d{2}\\s.*")){
        formatter = DateTimeFormatter.ofPattern("M/d/yy H:mm");
      }
      return LocalDateTime.parse(string, formatter);
    } catch (Exception e) {
      return null;
    }
  }

  private void setHeaderIndexes(String[] headerArray) throws Exception {
    companyCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)company\\scode");
    positionIDHeaderIndex = getHeaderIndex(headerArray, "(?i)position\\sid");
    lastNameHeaderIndex = getHeaderIndex(headerArray, "(?i)last\\sname");
    firstNameHeaderIndex = getHeaderIndex(headerArray, "(?i)first\\sname");
    timeInHeaderIndex = getHeaderIndex(headerArray, "(?i)in\\stime");
    timeOutHeaderIndex = getHeaderIndex(headerArray, "(?i)out\\stime");
    hoursHeaderIndex = getHeaderIndex(headerArray, "(?i)hours");
    payCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)pay\\scode");
    departmentHeaderIndex = getHeaderIndex(headerArray, "(?i)worked\\sdepartment");
  }

  private int getHeaderIndex(String[] headerArray, String pattern) throws Exception {
    for(int i = 0; i < headerArray.length; i++){
      if(headerArray[i].matches(pattern))
        return i;
    }
    throw new Exception("Unable to parse header for " + pattern + " given headers " + String.join(", ", headerArray));
  }
}
