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
      double hoursWorked = Duration.between(clockIn, clockOut).toMinutes() / 60.0;
      // return new DecimalFormat("##.##").format(hoursWorked);
      return hoursWorked;
    } else if (clockIn.getHour() >= 6 && clockOut.getHour() < 6) {
      double hoursWorked = Duration.between(LocalDateTime.of(clockOut.getYear(), clockOut.getMonth(), clockOut.getDayOfMonth(), 0, 0), clockOut).toMinutes() / 60.0;
      // return new DecimalFormat("##.##").format(hoursWorked);
      return hoursWorked;
    } else {
      return 0.0;
    }
  }

}
