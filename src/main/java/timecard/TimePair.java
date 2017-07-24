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

  public TimePair() {
  }

  public TimePair(LocalDateTime clockIn, LocalDateTime clockOut, String department,
      String payCode, Double duration) {
    this.clockIn = clockIn;
    this.clockOut = clockOut;
    this.department = department;
    this.payCode = payCode;
    this.duration = duration;
  }

  public LocalDateTime getClockIn() {
    return clockIn;
  }
  
  public LocalDateTime getClockOut() {
    return clockOut;
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

  public Double getSpreadOfHours() {
    if(clockIn == null || clockOut == null)
      return 0.0;
    Double spreadOfHours = Duration.between(clockIn, clockOut).getSeconds() / 3600.0;
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
}
