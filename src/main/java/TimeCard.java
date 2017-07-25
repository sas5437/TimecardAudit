package timecard;

import java.util.ArrayList;

// Company Code,Last Name,First Name,Worked Department,State,In time,Out time,Out Punch Type,Hours,Pay Code
public class TimeCard {

  String companyCode;
  String lastName;
  String firstName;
  ArrayList<TimePair> timePairs;
  
  public TimeCard() {
    companyCode = "";
    lastName = "";
    firstName = "";
    timePairs = new ArrayList<TimePair>();
  }

  public TimeCard(String companyCode, String firstName, String lastName) {
    this.companyCode = companyCode;
    this.firstName = firstName;
    this.lastName = lastName;
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

  public void addTimePair(TimePair timePair) {
    this.timePairs.add(timePair);
  }

  public ArrayList<TimePair> getTimePairs() {
    return timePairs;
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
}
