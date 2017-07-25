package timecard;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import timecard.FileNameExtensionError;
import timecard.TimePair;
import timecard.TimeCard;

public class CsvHandler {
  
  File file;
  boolean isRoundedToFifteen;
  Integer companyCodeHeaderIndex;
  Integer positionIDHeaderIndex;
  Integer lastNameHeaderIndex;
  Integer firstNameHeaderIndex;
  Integer timeInHeaderIndex;
  Integer timeOutHeaderIndex; 
  Integer hoursHeaderIndex;
  Integer payCodeHeaderIndex;
  Integer departmentHeaderIndex;

  public CsvHandler() {
    file = null;
    isRoundedToFifteen = false;
  }

  public void setFile(File file) throws FileNameExtensionError {
    if(file.getName().matches("(?i).*\\.csv$")){
      throw new FileNameExtensionError(".csv");
    }
    this.file = file;
  }

  public void setRoundingOption(boolean isRoundedToFifteen) {
    this.isRoundedToFifteen = isRoundedToFifteen;
  }

  public boolean isReady() {
    if(file == null || file.getName().matches("(?i).*\\.csv$")) {
      return false;
    } else {
      return true;
    }
  }

  public void processFile() throws Exception {
    BufferedReader csvFile = new BufferedReader(new FileReader(file));
    FileWriter auditFile = new FileWriter("AuditResults.csv");

    // Process header to find columns we need
    String dataRow = csvFile.readLine();
    String[] headerArray = dataRow.split(",");
    companyCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)company\\scode");
    positionIDHeaderIndex = getHeaderIndex(headerArray, "(?i)position\\sid");
    lastNameHeaderIndex = getHeaderIndex(headerArray, "(?i)last\\sname");
    firstNameHeaderIndex = getHeaderIndex(headerArray, "(?i)first\\sname");
    timeInHeaderIndex = getHeaderIndex(headerArray, "(?i)in\\stime");
    timeOutHeaderIndex = getHeaderIndex(headerArray, "(?i)out\\stime");
    hoursHeaderIndex = getHeaderIndex(headerArray, "(?i)hours");
    payCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)pay\\scode");
    departmentHeaderIndex = getHeaderIndex(headerArray, "(?i)worked\\sdepartment");

    HashMap<String, TimeCard> timeCards = new HashMap<String, TimeCard>();

    // Process and write CSV header
    String[] dataArray = dataRow.split(",");
    auditFile.write(
      dataArray[positionIDHeaderIndex] + "," +
      dataArray[lastNameHeaderIndex] + "," +
      dataArray[firstNameHeaderIndex] + "," +
      dataArray[departmentHeaderIndex] + "," +
      dataArray[timeInHeaderIndex] + "," +
      dataArray[timeOutHeaderIndex] + "," +
      "Adjusted In Time," +
      "Adjusted Out Time," +
      dataArray[hoursHeaderIndex] + "," +
      "Shift Differential," +
      dataArray[payCodeHeaderIndex] + "\n"
    );

    // Process CSV data
    dataRow = csvFile.readLine();
    int rowNumber = 1;
    TimePair timePair = null;

    while (dataRow != null) {
      rowNumber += 1;
      timePair = createTimePair(dataRow.split(","));
      upsertTimeCards(timePair);
      writeAudit(auditFile, timePair);
      dataRow = csvFile.readLine();
    }
    auditFile.close();
    csvFile.close();

    FileWriter summaryFile = new FileWriter("SummaryResults.csv");
    summaryFile.write("Position ID, Employee Name, Hours Worked, 40+ Hours?, Shift Differential, Spread of Hours\n");
    for(TimeCard timeCard : timeCards){
      summaryFile.write(
        timeCard.getKey() + "," +
        employeeNames.get(entry.getKey()) + "," +
        new DecimalFormat("##.##").format(employeeTotalHours.get(entry.getKey())) + "," +
        (employeeTotalHours.get(entry.getKey()) >= 40 ? "X" : "") + "," +
        new DecimalFormat("##.##").format(employeeTotalShiftDifferential.get(entry.getKey())) + "," +
        timeCard.getNumberOfDaysSpreadHours() + "\n"
      );
    }
    summaryFile.close();
  }

  private void writeAudit(FileWriter auditFile, TimePair timePair) {
    auditFile.write(
      dataArray[positionIDHeaderIndex] + "," +
      dataArray[lastNameHeaderIndex] + "," +
      dataArray[firstNameHeaderIndex] + "," +
      dataArray[departmentHeaderIndex] + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(in) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(out) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(adjustedIn) + "," +
      DateTimeFormatter.ofPattern("MM/dd/yy H:mm").format(adjustedOut) + "," +
      hours + "," +
      hoursWorkedAfterMidnight + "," +
      payCode + "\n"
    );
  }


  private void upsertTimeCards(TimePair timePair) {
    String key = "";
    String dateKey = "";
    String payCode = "";
    String hoursWorkedAfterMidnight = "0.00";
    String hours = "0.0";
    LocalDateTime in = null;
    LocalDateTime out = null;
    LocalDateTime adjustedIn = null;
    LocalDateTime adjustedOut = null;
    String dept = "";

    try {
      dept = dataArray[departmentHeaderIndex];
    } catch (Exception e) {
      dept = "";
    }
    try {
      in = stringToDate(dataArray[timeInHeaderIndex]);
    } catch (Exception e) {
      throw new Exception("Clock In could not be parsed on line " + rowNumber + " of the CSV.");
    }
    try {
      out = stringToDate(dataArray[timeOutHeaderIndex]);
    } catch (Exception e) {
      throw new Exception("Clock Out could not be parsed on line " + rowNumber + " of the CSV.");
    }
    try {
      payCode = dataArray[payCodeHeaderIndex];
    } catch(Exception e) {
      payCode = "";
    }
    try {
      hours = Double.valueOf(dataArray[hoursHeaderIndex]);
    } catch (Exception e) {
      hours = 0.0;
    }
    try {
      positionId = dataArray[positionIDHeaderIndex];
    } catch(Exception e) {
      throw new Exception("Could not parse Position Id on line " + rowNumber + ".");
    }

    if(timeCards.get(positionId) == null) {
      timeCards.put(
          positionId, 
          new TimeCard(
            dataArray[dataArray[companyCodeHeaderIndex], 
            dataArray[firstNameHeaderIndex], 
            dataArray[lastNameHeaderIndex], 
            positionId)
          );
    }

    timeCards.get(positionId).addTimePair(
        new TimePair(in, out, department, payCode, hours));
  }

  private LocalDateTime dateToAdjustedDate(LocalDateTime dateTime) {
    if(isRoundedToFifteen) {
      int unroundedMinutes = dateTime.getMinute();
      int mod = unroundedMinutes % 15;
      return dateTime.plusMinutes(mod < 8 ? -mod : (15-mod));
    } else {
      return dateTime;
    }
  }

  private LocalDateTime stringToDate(String string) {
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

  private int getHeaderIndex(String[] headerArray, String pattern) throws Exception {
    for(int i = 0; i < headerArray.length; i++){
      if(headerArray[i].matches(pattern))
        return i;
    }
    throw new Exception("Unable to parse header for " + pattern + " given headers " + String.join(", ", headerArray));
  }
}
