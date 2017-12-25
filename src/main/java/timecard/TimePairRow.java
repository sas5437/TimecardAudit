package timecard;

import javafx.beans.property.SimpleStringProperty;

public class TimePairRow {

  private static final String[] HEADERS = { "CompanyCode", "PositionId", "FirstName", "LastName", "TimeIn", "TimeOut", "Hours", "PayCode", "Department" };
  private SimpleStringProperty companyCode;
  private SimpleStringProperty positionId;
  private SimpleStringProperty lastName;
  private SimpleStringProperty firstName;
  private SimpleStringProperty timeIn;
  private SimpleStringProperty timeOut;
  private SimpleStringProperty hours;
  private SimpleStringProperty payCode;
  private SimpleStringProperty department;

  public TimePairRow(String companyCode, String positionId, String firstName, String lastName, String timeIn, String timeOut, String hours, String payCode, String department) {
    this.companyCode = new SimpleStringProperty(companyCode);
    this.positionId = new SimpleStringProperty(positionId);
    this.lastName = new SimpleStringProperty(lastName);
    this.firstName = new SimpleStringProperty(firstName);
    this.timeIn = new SimpleStringProperty(timeIn);
    this.timeOut = new SimpleStringProperty(timeOut);
    this.hours = new SimpleStringProperty(hours);
    this.payCode = new SimpleStringProperty(payCode);
    this.department = new SimpleStringProperty(department);
  }

  public static String[] getHeaderNames() {
    return HEADERS;
  }

  public String getCompanyCode() {
    return companyCode.get();
  }

  public String getPositionId() {
    return positionId.get();
  }

  public String getFirstName() {
    return firstName.get();
  }

  public String getLastName() {
    return lastName.get();
  }

  public String getTimeIn() {
    return timeIn.get();
  }

  public String getTimeOut() {
    return timeOut.get();
  }

  public String getHours() {
    return hours.get();
  }

  public String getPayCode() {
    return payCode.get();
  }

  public String getDepartment() {
    return department.get();
  }
}
