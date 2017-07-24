import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.time.Duration;
import java.time.LocalDateTime;


public class TimePairTest {
  @Test
  public void testGetHoursAfterMidnight(){
    LocalDateTime clockIn = LocalDateTime.of(2017, 7, 21, 11, 0);
    LocalDateTime clockOut = LocalDateTime.of(2017, 7, 21, 23, 0);
    String department = "";
    String payCode = "";
    Double duration = 0.0;
    TimePair timePair = new TimePair(clockIn, clockOut, department, payCode, duration);
    assertEquals(timePair.getHoursAfterMidnight(), 0.0, 0.0);
    
    clockOut = LocalDateTime.of(2017, 7, 22, 3, 30);
    timePair = new TimePair(clockIn, clockOut, department, payCode, duration);
    assertEquals(timePair.getHoursAfterMidnight(), 3.5, 0.0);

    clockIn = LocalDateTime.of(2017, 7, 22, 3, 30);
    clockOut = LocalDateTime.of(2017, 7, 22, 9, 30);
    timePair = new TimePair(clockIn, clockOut, department, payCode, duration);
    assertEquals(timePair.getHoursAfterMidnight(), 6.0, 0.0);
  }
}
