/* TimecardAudit.java

Author: Scott Serok
Created: 30 Dec 2016
Modified: n/a

This program is intended for non-production use. The project started as a simple
audit script, and it's still just a simple audit script in a single Java file.

This program is intended only to audit ADP Workforce Now V11 Timecard CSV export.

The program is intended to show the number of hours worked after midnight if a shift started before midnight.
The CSV export must contain the following headers:
Company Code	Payroll Name	File Number	Pay Date	Time In	Time Out	Hours	Earnings Code	Worked Department
The only headers used for auditing are Pay Date, Time In, Time Out, Hours, and Earnings Code

*/

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.awt.Desktop;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.time.Duration;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class TimecardAudit extends JFrame {
  final String FIFTEEN_MINUTES = "15 minutes";
  final String ONE_MINUTE = "1 minute";

  File file;
  static boolean isRoundedToFifteen;

  public TimecardAudit(){
    super("ADP Timecard Report Audit v0.1");
    setSize(300, 175);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

    file = null;
    isRoundedToFifteen = false;
    String[] minuteRoundListOptions = { ONE_MINUTE, FIFTEEN_MINUTES };

    Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

    JButton runButton = new JButton("Run");
    JButton fileButton = new JButton("Select File");
    JComboBox minuteRoundList = new JComboBox(minuteRoundListOptions);
    final JLabel statusbar = new JLabel("No file selected");
    final JLabel minuteListLabel = new JLabel("Round calculations to the nearest");

    // Create a file chooser that allows you to pick a CSV file
    fileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Value", "CSV", "csv");
        chooser.setFileFilter(filter);
        int option = chooser.showOpenDialog(TimecardAudit.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          statusbar.setText("You opened " + ((chooser.getSelectedFile()!=null)?
                            chooser.getSelectedFile().getName():"nothing"));
          file = chooser.getSelectedFile();
          statusbar.setText(file.getName() + " selected.");
        }
        else {
          statusbar.setText("You canceled.");
        }
      }
    });

    // Create a file chooser that allows you to pick a CSV file
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(file == null){
          JOptionPane.showMessageDialog(TimecardAudit.this, "Please select a file first.");
        } else {

          String selectedItem = (String)minuteRoundList.getSelectedItem();
          if(selectedItem.equals(ONE_MINUTE)) {
            isRoundedToFifteen = false;
          } else if (selectedItem.equals(FIFTEEN_MINUTES)) {
            isRoundedToFifteen = true;
          }

          try {
            statusbar.setText("... processing " + file.getName());
            processFile(file, isRoundedToFifteen);
            statusbar.setText("Finished processing " + file.getName());
          } catch (IOException e) {
            statusbar.setText("Could not load the file.");
          } catch (Exception e) {
            statusbar.setText("There was a problem processing the file.");
            try {
              FileWriter errorFile = new FileWriter("ErrorLog.txt");
              errorFile.write(" \n\nMessage: " + e.getMessage() + "\n\nStack Trace: " + e.getStackTrace()[0].toString());
              errorFile.close();
              java.awt.Desktop.getDesktop().edit(new File("ErrorLog.txt"));
            } catch (IOException ex) {
              statusbar.setText("Problem processing the file and logging the exception.");
            }
          }
        }
      }
    });

    minuteRoundList.setSelectedIndex(0);

    JPanel topPanel = new JPanel();
    JPanel centerPanel = new JPanel();
    JPanel bottomPanel = new JPanel();

    topPanel.add(fileButton);
    topPanel.add(runButton);
    centerPanel.add(minuteListLabel);
    centerPanel.add(minuteRoundList);
    bottomPanel.add(statusbar);

    c.add(topPanel, BorderLayout.NORTH);
    c.add(centerPanel, BorderLayout.CENTER);
    c.add(bottomPanel, BorderLayout.SOUTH);
  }

  private static LocalDateTime dateToAdjustedDate(LocalDateTime dateTime) {
    if(isRoundedToFifteen) {
      int unroundedMinutes = dateTime.getMinute();
      int mod = unroundedMinutes % 15;
      return dateTime.plusMinutes(mod < 8 ? -mod : (15-mod));
    } else {
      return dateTime;
    }
  }

  private static LocalDateTime stringToDate(String string) {
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

  private static String calculateHoursAfterMidnight(LocalDateTime in, LocalDateTime out) {
    if(in.getHour() < 6){
      double hoursWorked = Duration.between(in, out).toMinutes() / 60.0;
      return new DecimalFormat("##.##").format(hoursWorked);
    } else if (in.getHour() >= 6 && out.getHour() < 6) {
      double hoursWorked = Duration.between(LocalDateTime.of(out.getYear(), out.getMonth(), out.getDayOfMonth(), 0, 0), out).toMinutes() / 60.0;
      return new DecimalFormat("##.##").format(hoursWorked);
    } else {
      return "0.00";
    }
  }

  public static int getHeaderIndex(String[] headerArray, String pattern) throws Exception {
    for(int i = 0; i < headerArray.length; i++){
      if(headerArray[i].matches(pattern))
        return i;
    }
    throw new Exception("Unable to parse header for " + pattern + " given headers " + String.join(", ", headerArray));
  }

  public void processFile(File inputFile, boolean isRoundedToFifteen) throws Exception {
    if(!inputFile.getName().matches("(?i).*\\.csv$")){
      System.out.println("CSV file not detected. Exiting.");
      return;
    }
    BufferedReader csvFile = new BufferedReader(new FileReader(inputFile));
    FileWriter auditFile = new FileWriter("AuditResults.csv");

    // Process header to find columns we need
    String dataRow = csvFile.readLine();
    String[] headerArray = dataRow.split(",");
    Integer positionIDHeaderIndex = getHeaderIndex(headerArray, "(?i)position\\sid");
    Integer lastNameHeaderIndex = getHeaderIndex(headerArray, "(?i)last\\sname");
    Integer firstNameHeaderIndex = getHeaderIndex(headerArray, "(?i)first\\sname");
    Integer timeInHeaderIndex = getHeaderIndex(headerArray, "(?i)in\\stime");
    Integer timeOutHeaderIndex = getHeaderIndex(headerArray, "(?i)out\\stime");
    Integer hoursHeaderIndex = getHeaderIndex(headerArray, "(?i)hours");
    Integer payCodeHeaderIndex = getHeaderIndex(headerArray, "(?i)pay\\scode");
    Integer departmentHeaderIndex = getHeaderIndex(headerArray, "(?i)worked\\sdepartment");
    HashMap<String, String> employeeNames = new HashMap<String, String>();
    HashMap<String, Double> employeeTotalHours = new HashMap<String, Double>();
    HashMap<String, Double> employeeTotalShiftDifferential = new HashMap<String, Double>();
    HashMap<String, HashMap<String, HashMap<String, LocalDateTime>>> employeeSpreadOfHours = new HashMap<String, HashMap<String, HashMap<String, LocalDateTime>>>();
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
    int rowNumber = 1;
    while (dataRow != null){
      rowNumber += 1;
      dataArray = dataRow.split(",");

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
        adjustedIn = dateToAdjustedDate(in);
      } catch (Exception e) {
        throw new Exception("Clock In could not be adjusted on line " + rowNumber + " of the CSV.");
      }

      try {
        adjustedOut = dateToAdjustedDate(out);
      } catch (Exception e) {
        throw new Exception("Clock out could not be adjusted on line " + rowNumber + " of the CSV.");
      }

      try {
        if(dataArray[departmentHeaderIndex].matches("(?i)rest.*") && in != null && out != null) {
          hoursWorkedAfterMidnight = calculateHoursAfterMidnight(adjustedIn, adjustedOut);
        } else {
          hoursWorkedAfterMidnight = "0.00";
        }
      } catch (Exception e) {
        hoursWorkedAfterMidnight = "0.00";
      }

      try {
        hours = dataArray[hoursHeaderIndex];
      } catch (Exception e) {
        hours = "0.00";
      }

      try {
        payCode = dataArray[payCodeHeaderIndex];
      } catch(Exception e) {
        payCode = "";
      }

      key = dataArray[positionIDHeaderIndex];

      if(employeeNames.get(key) == null){
        employeeNames.put(key, dataArray[firstNameHeaderIndex] + " " + dataArray[lastNameHeaderIndex]);
      }

      // Add to total hours for this employee
      if(employeeTotalHours.get(key) == null){
        employeeTotalHours.put(key, Double.parseDouble(hours));
      } else {
        employeeTotalHours.put(
          key,
          employeeTotalHours.get(key) + Double.parseDouble(hours)
        );
      }

      // Add to total amount of shift differential hours
      if (employeeTotalShiftDifferential.get(key) == null)
        employeeTotalShiftDifferential.put(key, 0.0);

      employeeTotalShiftDifferential.put(
        key,
        (employeeTotalShiftDifferential.get(key) + Double.parseDouble(hoursWorkedAfterMidnight))
      );

      // Setup spread of hours calculation by gathering earliest and latest clock in/out for a day (splitting days at 6am)
      if(adjustedIn.getHour() < 6) {
        dateKey = Integer.toString(adjustedIn.minusDays(1).getMonthValue()) + adjustedIn.minusDays(1).getDayOfMonth();
      } else {
        dateKey = Integer.toString(adjustedIn.getMonthValue()) + adjustedIn.getDayOfMonth();
      }

      if (employeeSpreadOfHours.get(key) == null)
        employeeSpreadOfHours.put(key, new HashMap<String, HashMap<String, LocalDateTime>>());

      if (employeeSpreadOfHours.get(key).get(dateKey) == null)
        employeeSpreadOfHours.get(key).put(dateKey, new HashMap<String, LocalDateTime>());

      if (employeeSpreadOfHours.get(key).get(dateKey).get("IN") == null)
        employeeSpreadOfHours.get(key).get(dateKey).put("IN", in);

      if (employeeSpreadOfHours.get(key).get(dateKey).get("OUT") == null)
        employeeSpreadOfHours.get(key).get(dateKey).put("OUT", in);

      if(in.getHour() > 6 && in.getHour() < employeeSpreadOfHours.get(key).get(dateKey).get("IN").getHour())
        employeeSpreadOfHours.get(key).get(dateKey).put("IN", in);

      if((out.getHour() > 6 && out.getHour() > employeeSpreadOfHours.get(key).get(dateKey).get("OUT").getHour()) ||
         (out.getHour() < 6 && employeeSpreadOfHours.get(key).get(dateKey).get("OUT").getHour() < 6 && out.getHour() > employeeSpreadOfHours.get(key).get(dateKey).get("OUT").getHour()) ||
         (out.getHour() < 6 && employeeSpreadOfHours.get(key).get(dateKey).get("OUT").getHour() > 6)) {
        employeeSpreadOfHours.get(key).get(dateKey).put("OUT", out);
      }

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

      dataRow = csvFile.readLine();
    }
    auditFile.close();
    csvFile.close();

    FileWriter summaryFile = new FileWriter("SummaryResults.csv");
    summaryFile.write("Position ID, Employee Name, Hours Worked, 40+ Hours?, Shift Differential, Spread of Hours\n");
    for(HashMap.Entry<String, Double> entry : employeeTotalHours.entrySet()){
      int count = 0;
      for(HashMap.Entry<String, HashMap<String, LocalDateTime>> dateMap : employeeSpreadOfHours.get(entry.getKey()).entrySet()){
        if(Duration.between(employeeSpreadOfHours.get(entry.getKey()).get(dateMap.getKey()).get("IN"), employeeSpreadOfHours.get(entry.getKey()).get(dateMap.getKey()).get("OUT")).toMinutes() > 10*60)
          count+=1;
      }
      summaryFile.write(
        entry.getKey() + "," +
        employeeNames.get(entry.getKey()) + "," +
        new DecimalFormat("##.##").format(employeeTotalHours.get(entry.getKey())) + "," +
        (employeeTotalHours.get(entry.getKey()) >= 40 ? "X" : "") + "," +
        new DecimalFormat("##.##").format(employeeTotalShiftDifferential.get(entry.getKey())) + "," +
        count + "\n"
      );
    }
    summaryFile.close();

    // Lastly, open the audit file for the user
    java.awt.Desktop.getDesktop().edit(new File("AuditResults.csv"));
    java.awt.Desktop.getDesktop().edit(new File("SummaryResults.csv"));
	}

	public static void main(String[] args) {
    TimecardAudit tca = new TimecardAudit();
    tca.setVisible(true);
  }
}
