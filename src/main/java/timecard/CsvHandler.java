package timecard;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
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
import timecard.TimePairRow;
import timecard.TimeCard;

public class CsvHandler {
  
  File inputFile;
  String outputFilePath;
  Integer roundMinutesTo;
  Integer companyCodeHeaderIndex;
  Integer positionIdHeaderIndex;
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
  ArrayList<TimePairRow> timePairRows;
  ArrayList<SummaryRow> summaryRows;

  public CsvHandler() {
    inputFile = null;
    roundMinutesTo = 0;
    timeCards = new HashMap<String, TimeCard>();
    outputFilePath = "AuditResults.csv";
    employeeNames = new HashMap<String, String>();
    employeeTotalHours = new HashMap<String, Double>();
    employeeTotalShiftDifferential = new HashMap<String, Double>();
    timePairRows = new ArrayList<TimePairRow>();
    summaryRows = new ArrayList<SummaryRow>();
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
    if(timeCards.size() < 1)
      return;
    for(TimeCard timeCard : timeCards.values())
      timeCard.setRoundMinutes(roundMinutesTo);
  }

  public Integer getRoundingOption() {
    return roundMinutesTo;
  }

  public boolean isReady() {
    if(inputFile != null) {
      return true;
    }
    return false;
  }

  public ArrayList<TimePairRow> processTimePairRows() throws Exception {
    if(!isReady())
      throw new Exception("Not ready to get table data.");
    csvFile = new BufferedReader(new FileReader(inputFile));
    processHeaders(csvFile.readLine());
    String thisLine = "";
    String[] data;

    while ((thisLine = csvFile.readLine()) != null) {
      data = thisLine.split(",", -1);
      timePairRows.add(
        new TimePairRow(
          data[companyCodeHeaderIndex],
          data[positionIdHeaderIndex],
          data[lastNameHeaderIndex],
          data[firstNameHeaderIndex],
          data[timeInHeaderIndex],
          data[timeOutHeaderIndex],
          data[hoursHeaderIndex],
          data[payCodeHeaderIndex],
          data[departmentHeaderIndex]));
    }

    csvFile.close();
    return timePairRows;
  }

  public ArrayList<SummaryRow> processSummaryRows() throws Exception {
    if(!isReady())
      throw new Exception("Not ready to process input file.");
    csvFile = new BufferedReader(new FileReader(inputFile));
    // Process CSV header
    processHeaders(csvFile.readLine());
    // Process CSV data
    processData();
    csvFile.close();
    return summaryRows;
  }

  public Collection<TimeCard> getTimeCards() {
    return timeCards.values();
  }

  public void exportSummaryFile() throws Exception {
    if(timeCards.isEmpty())
      return;
    // TODO allow user to specify export file
    FileWriter summaryFile = new FileWriter("SummaryResults.csv");
    summaryFile.write("Position ID, Employee Last Name, Employee First Name, Hours Worked, 40+ Hours?, Shift Differential, Spread of Hours\n");
    for(SummaryRow summaryRow : summaryRows) {
      summaryFile.write(summaryRow.toCsvString());
    }
    summaryFile.close();
  }

  private void processHeaders(String headerRow) throws Exception {
    // Process header to find columns we need
    String[] headerArray = headerRow.split(",");
    companyCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)company\\scode");
    positionIdHeaderIndex = getHeaderIndex(headerArray, "(?i)position\\sid");
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
    String[] dataArray = dataRow.split(",", -1);
    int rowNumber = 1;
    TimeCard timeCard = null;
    TimePair timePair = null;
    summaryRows = new ArrayList<SummaryRow>();
    timeCards = new HashMap<String, TimeCard>();

    // process the CSV file into TimePair and TimeCard models
    while (dataRow != null) {
      rowNumber += 1;
      dataArray = dataRow.split(",", -1);
      timeCard = findOrCreateTimeCard(dataArray);
      createTimePair(dataArray, rowNumber, timeCard);
      dataRow = csvFile.readLine();
    }

    // summarize the data model into an easy to consume report data set
    for(Map.Entry<String, TimeCard> entry : timeCards.entrySet()) {
      summaryRows.add(new SummaryRow(
        entry.getKey(),
        entry.getValue().getLastName(),
        entry.getValue().getFirstName(),
        entry.getValue().getFormattedTotalHoursWorked(),
        entry.getValue().hasOvertime(),
        entry.getValue().getFormattedTotalShiftDifferential(),
        entry.getValue().getDaysWithSpread()));
    }
  }

  private TimeCard findOrCreateTimeCard(String[] dataArray) {
    String positionId = dataArray[positionIdHeaderIndex];
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

  private void createTimePair(String[] dataArray, int rowNumber, TimeCard timeCard) throws Exception {
    String key = "";
    String positionId = "";
    String dateKey = "";
    String payCode = "";
    String hoursWorkedAfterMidnight = "0.00";
    Double hours = 0.0;
    LocalDateTime in = null;
    LocalDateTime out = null;
    String dept = "";

    dept = dataArray[departmentHeaderIndex];
    payCode = dataArray[payCodeHeaderIndex];
    positionId = dataArray[positionIdHeaderIndex];
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
      hours = Double.valueOf(dataArray[hoursHeaderIndex]);
    } catch (Exception e) {
      hours = 0.0;
    }
    timeCard.addTimePair(in, out, dept, payCode, hours);
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
