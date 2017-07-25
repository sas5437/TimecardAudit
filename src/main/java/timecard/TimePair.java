package timecard;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

public class TimePair {
  
  LocalDateTime clockIn;
  LocalDateTime clockInAdjusted;
  LocalDateTime clockOut;
  LocalDateTime clockOutAdjusted;
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
    this.clockInAdjusted = dateToAdjustedDate(clockIn);
    this.clockOutAdjusted = dateToAdjustedDate(clockOut);
  }

  public LocalDateTime getClockIn() {
    return clockIn;
  }
  
  public LocalDateTime getClockOut() {
    return clockOut;
  }
 
  public LocalDateTime getClockInAdjusted() {
    return clockInAdjusted;
 }
  
  public LocalDateTime getClockOutAdjusted() {
    return clockOutAdjusted;
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
      return clockIn.minusDays(1).getDayOfMonth();
    } else {
      return clockIn.getDayOfMonth();
    }
  }

  public Double getHoursAfterMidnight() {
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

  private LocalDateTime dateToAdjustedDate(LocalDateTime dateTime) {
    if(roundTo != null && roundTo > 0) {
      int unroundedMinutes = dateTime.getMinute();
      int mod = unroundedMinutes % 15;
      return dateTime.plusMinutes(mod < 8 ? -mod : (15-mod));
    } else {
      return dateTime;
    }
  }

}
