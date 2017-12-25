package timecard;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

public class TimePair {
  
  LocalDateTime clockIn;
  LocalDateTime clockOut;
  String department;
  String payCode;
  Double duration;
  Integer roundTo;

  public TimePair() {
  }

  public TimePair(LocalDateTime clockIn, LocalDateTime clockOut, String department,
      String payCode, Double duration, Integer roundTo) {
    this.clockIn = clockIn;
    this.clockOut = clockOut;
    this.department = department;
    this.payCode = payCode;
    this.duration = duration;
    this.roundTo = roundTo;   // should be increments of 5 ideally
    if(this.roundTo < 1)
      this.roundTo = 1;
  }

  public LocalDateTime getClockIn() {
    return clockIn;
  }
  
  public LocalDateTime getClockOut() {
    return clockOut;
  }
  
  public LocalDateTime getClockInAdjusted() {
    return dateToAdjustedDate(clockIn);
  }
  
  public LocalDateTime getClockOutAdjusted() {
    return dateToAdjustedDate(clockOut);
  } 

  public String getDepartment() {
    return department;
  }
  
  public String getPayCode() {
    return payCode;
  }

  public Double getDuration() {
    return duration;
  }

  public Integer getRoundTo() {
    return roundTo;
  }

  public void setRoundTo(Integer roundTo) {
    this.roundTo = roundTo;
  }

  // Based off of Adjusted
  public Integer getDayOfMonth() {
    if(clockIn.getHour() < 6) {
      return getClockInAdjusted().minusDays(1).getDayOfMonth();
    } else {
      return getClockInAdjusted().getDayOfMonth();
    }
  }

  public Double getHoursAfterMidnight() {
    LocalDateTime clockIn = getClockInAdjusted();
    LocalDateTime clockOut = getClockOutAdjusted();
    if(clockIn.getHour() < 6){
      if(clockOut.getHour() >= 6) {
        return Duration.between(clockIn, LocalDateTime.of(clockOut.getYear(), clockOut.getMonth(), clockOut.getDayOfMonth(), 6, 0)).toMinutes() / 60.0;
      } else {
        return Duration.between(clockIn, clockOut).toMinutes() / 60.0;
      }
    } else if (clockIn.getHour() >= 6 && clockOut.getHour() < 6) {
      return Duration.between(LocalDateTime.of(clockOut.getYear(), clockOut.getMonth(), clockOut.getDayOfMonth(), 0, 0), clockOut).toMinutes() / 60.0;
    } else {
      return 0.0;
    }
    // return new DecimalFormat("##.##").format(hoursWorked);
  }

  public Double getSpreadOfHours() {
    if(clockIn == null || clockOut == null)
      return 0.0;
    Double spreadOfHours = Duration.between(getClockInAdjusted(), getClockOutAdjusted()).getSeconds() / 3600.0;
    if(spreadOfHours < 10) { 
      return 0.0;
    }
    return spreadOfHours;
  }

  public boolean hasHoursAfterMidnight() {
    if(clockIn == null || clockOut == null)
      return false;
    if(clockOut.getHour() < 6)
      return true;
    return false;
  }
  
  private LocalDateTime dateToAdjustedDate(LocalDateTime dateTime) {
    if(roundTo > 1) {
      int unroundedMinutes = dateTime.getMinute();
      int mod = unroundedMinutes % roundTo;
      return dateTime.plusMinutes(mod < (roundTo/2) ? -mod : (roundTo-mod));
    } else {
      return dateTime;
    }
  }
}
