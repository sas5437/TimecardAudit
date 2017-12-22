package timecard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

// Company Code,Last Name,First Name,Worked Department,State,In time,Out time,Out Punch Type,Hours,Pay Code
public class TimeCard {

  private static final Integer REGULAR_WORK_WEEK = 40;
  private static int TEN_HOURS_IN_SECONDS = 36000;

  String positionId;
  String companyCode;
  String lastName;
  String firstName;
  ArrayList<TimePair> timePairs;
  Integer roundMinutesTo;
  HashMap<String, HashMap<String, LocalDateTime>> employeeSpreadOfHours;
  
  public TimeCard() {
    positionId = "";
    companyCode = "";
    lastName = "";
    firstName = "";
    roundMinutesTo = 1;
    timePairs = new ArrayList<TimePair>();
  }

  public TimeCard(String companyCode, String firstName, String lastName, String positionId) {
    this.positionId = positionId;
    this.companyCode = companyCode;
    this.lastName = lastName;
    this.firstName = firstName;
    this.roundMinutesTo = 1;
    timePairs = new ArrayList<TimePair>();
  }

  public TimeCard(String companyCode, String firstName, String lastName, String positionId, Integer roundMinutesTo) {
    this.positionId = positionId;
    this.companyCode = companyCode;
    this.lastName = lastName;
    this.firstName = firstName;
    this.roundMinutesTo = roundMinutesTo;
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

  public Double getTotalHoursWorked() {
    Double total = 0.0;
    for(TimePair timePair : timePairs) {
      total += timePair.getDuration();
    }
    return total;
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

  public Integer getDaysWithSpread() {
      Integer daysWithSpread = 0;
      HashMap<Integer, ArrayList<TimePair>> domHashMap = new HashMap<Integer, ArrayList<TimePair>>();
      ArrayList<TimePair> tmpTimePairs;
      LocalDateTime earliestIn;
      LocalDateTime latestOut;
      // Populate domHashMap
      for(TimePair timePair : timePairs) {
        if(domHashMap.get(timePair.getDayOfMonth()) == null)
          domHashMap.put(timePair.getDayOfMonth(), new ArrayList<TimePair>());
        domHashMap.get(timePair.getDayOfMonth()).add(timePair);
      }
      // Process each day of the month by iterating over the keys
      for(Integer key : domHashMap.keySet()) {
        tmpTimePairs = domHashMap.get(key);
        // First, sort the TimePairs by clockIn
        Collections.sort(tmpTimePairs, new TimePairComparator());
        // Second, grab clock in of first, grab clock out of last
        earliestIn = tmpTimePairs.get(0).getClockInAdjusted();
        latestOut = tmpTimePairs.get(tmpTimePairs.size()-1).getClockOutAdjusted();
        if(Duration.between(earliestIn, latestOut).getSeconds() > TEN_HOURS_IN_SECONDS){
          daysWithSpread += 1;
        }
      }
      return daysWithSpread;
  }

  // "Shift Differential" hourly rate adjustment
  public Double getTotalHoursAfterMidnight() {
    Double hoursAfterMidnight = 0.0;
    for(TimePair timePair : timePairs)
      hoursAfterMidnight += timePair.getHoursAfterMidnight();
    return hoursAfterMidnight;
  }

  public String getFormattedTotalShiftDifferential() {
    return new DecimalFormat("##.##").format(getTotalHoursAfterMidnight()).toString();
  }

  public boolean hasOvertime() {
    return getTotalHoursWorked() >= REGULAR_WORK_WEEK;
  }

  public String getFormattedTotalHoursWorked() {
    return new DecimalFormat("##.##").format(getTotalHoursWorked()).toString();
  }
}
