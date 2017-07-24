package timecard;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

// Company Code,Last Name,First Name,Worked Department,State,In time,Out time,Out Punch Type,Hours,Pay Code
public class TimeCard {

  private static final Integer REGULAR_WORK_WEEK = 40;
  private static final double SPREAD_OF_HOURS_LIMIT = 10.0;

  String positionId;
  String companyCode;
  String lastName;
  String firstName;
  ArrayList<TimePair> timePairs;
  boolean isRoundedFifteen;
  HashMap<String, HashMap<String, LocalDateTime>> employeeSpreadOfHours;
  
  public TimeCard() {
    positionId = "";
    companyCode = "";
    lastName = "";
    firstName = "";
    this.isRoundedFifteen = true;
    timePairs = new ArrayList<TimePair>();
  }

  public TimeCard(String companyCode, String firstName, String lastName, String positionId, boolean isRoundedFifteen) {
    this.positionId = positionId;
    this.companyCode = companyCode;
    this.lastName = lastName;
    this.firstName = firstName;
    this.isRoundedFifteen = isRoundedFifteen;
    timePairs = new ArrayList<TimePair>();
  }

  public void setCompanyCode(String companyCode) {
    this.companyCode = companyCode;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setPositionId(String positionId) {
    this.positionId = positionId;
  }

  public String getPositionId() {
    return positionId;
  }

  public String getCompanyCode() {
    return companyCode;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return lastName + ", " + firstName;
  }

  public void addTimePair(TimePair timePair) {
    this.timePairs.add(timePair);
  }

  public ArrayList<TimePair> getTimePairs() {
    return timePairs;
  }

  // Used for spread of hours calculation
  // SOH uses adjusted clock in and clock out times
  // and must look at the earliest clock in (after 6am)
  // and the latest clock out (before 6 am next day) to
  // determine the full spread of hours
  public Double getTotalSpreadOfHours() {
    Double total = 0.0;
    for(TimePair timePair : timePairs) {
      total += timePair.getSpreadOfHours();
    }
    return total;
  }

  public Integer getNumberOfDaysWithSpreadOfHours() {
    int count = 0;
    for(TimePair timePair : timePairs)
      if(timePair.getSpreadOfHours() > SPREAD_OF_HOURS_LIMIT)
        count += 1;
    return count;
  }

  // "Shift Differential" hourly rate adjustment
  public Double getTotalHoursAfterMidnight() {

    return 0.0;
  }

  public Double getFormattedTotalShiftDifferential() {
    return 0.0;
  }

  public Double getTotalHoursWorked() {
    Double totalHoursWorked = 0.0;
    for(TimePair timePair : timePairs)
      totalHoursWorked += timePair.getDuration();
    return totalHoursWorked;
  }

  public boolean hasOvertime() {
    return getTotalHoursWorked() >= REGULAR_WORK_WEEK;
  }

  public String getFormattedTotalHoursWorked() {
    return new DecimalFormat("##.##").format(getTotalHoursWorked()).toString();
  }

  private LocalDateTime dateToAdjustedDate(LocalDateTime dateTime) {
    if(isRoundedFifteen) {
      int unroundedMinutes = dateTime.getMinute();
      int mod = unroundedMinutes % 15;
      return dateTime.plusMinutes(mod < 8 ? -mod : (15-mod));
    } else {
      return dateTime;
    }
  }
}
