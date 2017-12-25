package timecard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SummaryRow {

  private static final String[] HEADERS = { "PositionId", "LastName", "FirstName", "HoursWorked", "FourtyHours", "ShiftDifferential", "SpreadOfHours" };
  private SimpleStringProperty positionId;
  private SimpleStringProperty lastName;
  private SimpleStringProperty firstName;
  private SimpleStringProperty hoursWorked;
  private SimpleBooleanProperty fourtyHours;
  private SimpleStringProperty shiftDifferential;
  private SimpleIntegerProperty spreadOfHours;

  public SummaryRow(String positionId, String lastName, String firstName, String hoursWorked, boolean fourtyHours, String shiftDifferential, Integer spreadOfHours) {
    this.positionId = new SimpleStringProperty(positionId);
    this.lastName = new SimpleStringProperty(lastName);
    this.firstName = new SimpleStringProperty(firstName);
    this.hoursWorked = new SimpleStringProperty(hoursWorked);
    this.fourtyHours = new SimpleBooleanProperty(fourtyHours);
    this.shiftDifferential = new SimpleStringProperty(shiftDifferential);
    this.spreadOfHours = new SimpleIntegerProperty(spreadOfHours);
  }

  public static String[] getHeaderNames() {
    return HEADERS;
  }

  public String getPositionId() {
    return positionId.get();
  }

  public String getLastName() {
    return lastName.get();
  }

  public String getFirstName() {
    return firstName.get();
  }

  public String getHoursWorked() {
    return hoursWorked.get();
  }

  public boolean getFourtyHours() {
    return fourtyHours.get();
  }

  public String getShiftDifferential() {
    return shiftDifferential.get();
  }

  public Integer getSpreadOfHours() {
    return spreadOfHours.get();
  }

  public String toCsvString() {
    return positionId.get() + ", " +
      lastName.get() + ", " +
      firstName.get() + ", " +
      hoursWorked.get() + ", " +
      fourtyHours.get() + ", " +
      shiftDifferential.get() + ", " +
      spreadOfHours.get() + "\n";
  }
}
