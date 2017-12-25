package timecard;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import timecard.FileNameExtensionError;
import timecard.TimePair;
import timecard.TimeCard;

public class CsvHandler {
  
  File inputFile;
  String outputFilePath;
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
  HashMap<String, String> employeeNames;
  HashMap<String, Double> employeeTotalHours;
  HashMap<String, Double> employeeTotalShiftDifferential;
  HashMap<String, HashMap<String, HashMap<String, LocalDateTime>>> employeeSpreadOfHours;
  BufferedReader csvFile;

  public CsvHandler() {
    inputFile = null;
    roundMinutesTo = 0;
    timeCards = new HashMap<String, TimeCard>();
    outputFilePath = "AuditResults.csv";
    employeeNames = new HashMap<String, String>();
    employeeTotalHours = new HashMap<String, Double>();
    employeeTotalShiftDifferential = new HashMap<String, Double>();
  }

  public void setInputFile(File file) {
    this.inputFile = file;
  }

  public String getInputFilePath() {
    if(inputFile == null)
      return "";
    return inputFile.getAbsolutePath();
  }

  public void setOutputFilePath(String filePath) throws FileNameExtensionError {
    if(!filePath.matches("(?i).*\\.csv$")){
      throw new FileNameExtensionError(".csv");
    }
    this.outputFilePath = filePath;
  }

  public String getOutputFilePath() {
    return outputFilePath;
  }

  public void setRoundingOption(Integer roundMinutesTo) {
    this.roundMinutesTo = roundMinutesTo;
  }

  public boolean isReady() {
    if(inputFile != null) {
      return true;
    }
    return false;
  }

  public void processFile() throws Exception {
    if(!isReady())
      throw new Exception("Not ready to process input file.");
    csvFile = new BufferedReader(new FileReader(inputFile));
    // Process CSV header
    processHeaders(csvFile.readLine());
    // Process CSV data
    processData();
    csvFile.close();
  }

  public Collection<TimeCard> getTimeCards() {
    return timeCards.values();
  }

  public void writeSummaryFile() throws Exception {
    if(timeCards.isEmpty())
      return;
    FileWriter summaryFile = new FileWriter("SummaryResults.csv");
    summaryFile.write("Position ID, Employee Name, Hours Worked, 40+ Hours?, Shift Differential, Spread of Hours\n");
    for(Map.Entry<String, TimeCard> entry : timeCards.entrySet()) {
      summaryFile.write(
        entry.getKey() + "," +
        entry.getValue().getFullName() + "," +
        entry.getValue().getFormattedTotalHoursWorked() + "," +
        (entry.getValue().hasOvertime() ? "X" : "") + "," +
        entry.getValue().getFormattedTotalShiftDifferential() + "," +
        entry.getValue().getDaysWithSpread() + "\n"
      );
    }
    summaryFile.close();
  }

  private void processHeaders(String headerRow) throws Exception {
    // Process header to find columns we need
    String[] headerArray = headerRow.split(",");
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

  private void processData() throws Exception {
    String dataRow = csvFile.readLine();
    String[] dataArray = dataRow.split(",");
    int rowNumber = 1;
    TimeCard timeCard = null;
    TimePair timePair = null;

    while (dataRow != null) {
      rowNumber += 1;
      timeCard = findOrCreateTimeCard(dataRow.split(","));
      timePair = createTimePair(dataRow.split(","), rowNumber);
      timeCard.addTimePair(timePair);
      dataRow = csvFile.readLine();
    }
  }

  private TimeCard findOrCreateTimeCard(String[] dataArray) {
    String positionId = dataArray[positionIDHeaderIndex];
    if(timeCards.get(positionId) == null) {
      timeCards.put(
          positionId, 
          new TimeCard(
            dataArray[companyCodeHeaderIndex], 
            dataArray[firstNameHeaderIndex], 
            dataArray[lastNameHeaderIndex], 
            positionId,
            roundMinutesTo)
          );
    }
    return timeCards.get(positionId);
  }

  private TimePair createTimePair(String[] dataArray, int rowNumber) throws Exception {
    String key = "";
    String positionId = "";
    String dateKey = "";
    String payCode = "";
    String hoursWorkedAfterMidnight = "0.00";
    Double hours = 0.0;
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
    return new TimePair(in, out, dept, payCode, hours, roundMinutesTo);
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
