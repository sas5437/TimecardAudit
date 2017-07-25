import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import java.time.Duration;
import java.time.LocalDateTime;
import timecard.TimePair;


public class TimePairTest {
  static LocalDateTime clockIn;
  static LocalDateTime clockOut;
  static String department;
  static String payCode;
  static Double duration;
  static TimePair timePair;
  static int roundTo;

  @Before
  public void setupInstance() {
    clockIn = LocalDateTime.of(2017, 7, 21, 11, 0);
    clockOut = LocalDateTime.of(2017, 7, 21, 23, 0);
    department = "";
    payCode = "";
    duration = 0.0;
    roundTo = 1;
  }

  @Test
  public void testNoHoursAfterMidnight() {
    timePair = new TimePair(clockIn, clockOut, department, payCode, duration, roundTo);
    assertEquals(timePair.getHoursAfterMidnight(), 0.0, 0.0);
  }

  @Test
  public void testPartialHoursAfterMidnight() {
    clockOut = LocalDateTime.of(2017, 7, 22, 3, 30);
    timePair = new TimePair(clockIn, clockOut, department, payCode, duration, roundTo);
    assertEquals(timePair.getHoursAfterMidnight(), 3.5, 0.0);
  }

  @Test
  public void testAllHoursAfterMidnight() {
    clockIn = LocalDateTime.of(2017, 7, 22, 3, 30);
    clockOut = LocalDateTime.of(2017, 7, 22, 9, 30);
    timePair = new TimePair(clockIn, clockOut, department, payCode, duration, roundTo);
    assertEquals(timePair.getHoursAfterMidnight(), 2.5, 0.0);
  }
}
