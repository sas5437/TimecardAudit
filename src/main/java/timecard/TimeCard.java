package timecard;

import java.util.ArrayList;

// Company Code,Last Name,First Name,Worked Department,State,In time,Out time,Out Punch Type,Hours,Pay Code
public class TimeCard {

  String positionId;
  String companyCode;
  String lastName;
  String firstName;
  ArrayList<TimePair> timePairs;
  
  public TimeCard() {
    positionId = "";
    companyCode = "";
    lastName = "";
    firstName = "";
    timePairs = new ArrayList<TimePair>();
  }

  public TimeCard(String companyCode, String firstName, String lastName, String positionId) {
    this.companyCode = companyCode;
    this.firstName = firstName;
    this.lastName = lastName;
    this.positionId = positionId;
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

    // TODO: move the rest of this method into TimeCard as a calculation method or on addTimePair trigger update
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
    return 0.0;
  }

  public Integer getNumberOfDaysWithSpreadOfHours() {
    // > 10 hours in 1 day adds to count
    return 0;
  }

  // "Shift Differential" hourly rate adjustment
  public Double getTotalHoursAfterMidnight() {

    return 0.0;
  }
}
