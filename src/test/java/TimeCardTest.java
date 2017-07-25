import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;
import java.time.LocalDateTime;
import timecard.TimeCard;
import timecard.TimePair;

public class TimeCardTest {
  @Test
  public void testEmptyConstructor() {
    TimeCard timeCard = new TimeCard();
    assertEquals(timeCard.getCompanyCode(), "");
    assertEquals(timeCard.getFirstName(), "");
    assertEquals(timeCard.getLastName(), "");
  }

  @Test
  public void testPopulatedConstructor() {
    TimeCard timeCard = new TimeCard("A01", "Scott", "Serok", "position2342");
    assertEquals(timeCard.getCompanyCode(), "A01");
    assertEquals(timeCard.getFirstName(), "Scott");
    assertEquals(timeCard.getLastName(), "Serok");
  }

  @Test
  public void testSetters() {
    TimeCard timeCard = new TimeCard();
    timeCard.setCompanyCode("B10");
    assertEquals(timeCard.getCompanyCode(), "B10");
    timeCard.setFirstName("John");
    assertEquals(timeCard.getFirstName(), "John");
    timeCard.setLastName("Cena");
    assertEquals(timeCard.getLastName(), "Cena");
  }

  @Test
  public void testAddingTimePairs() {
    TimeCard timeCard = new TimeCard("A01", "Scott", "Serok", "posit iong345F#$T");
    TimePair timePair = new TimePair();
    timeCard.addTimePair(timePair);
    assertEquals(timeCard.getTimePairs().size(), 1);
    assertEquals(timeCard.getTimePairs().get(0), timePair);
  }

  @Test
  public void testGetTotalHoursAfterMidnight() {
    TimeCard timeCard = new TimeCard("A01", "scott", "serok", "f4f383");
    LocalDateTime in = LocalDateTime.of(2017, 1, 1, 14, 05);
    LocalDateTime out = LocalDateTime.of(2017, 1, 2, 1, 35);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 9.5, 1));
    in = LocalDateTime.of(2017, 1, 2, 2, 0);
    out = LocalDateTime.of(2017, 1, 2, 7, 0);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 5.0, 1));
    assertEquals(timeCard.getTotalHoursAfterMidnight(), 5.583, 0.001);
  }

  @Test
  public void testGetTotalHoursWorked() {
    TimeCard timeCard = new TimeCard("A01", "scott", "serok", "f4f383");
    LocalDateTime in = LocalDateTime.of(2017, 1, 1, 1, 0);
    LocalDateTime out = LocalDateTime.of(2017, 1, 2, 1, 1);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 9.5, 1));
    in = LocalDateTime.of(2017, 1, 2, 2, 0);
    out = LocalDateTime.of(2017, 1, 2, 2, 1);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 5.0, 1));
    // total hours is calculated from TimePair#duration 
    assertEquals(timeCard.getTotalHoursWorked(), 14.5, 0);
  }

  @Test
  public void testGetDaysWithSpread() {
    TimeCard timeCard = new TimeCard("A01", "scott", "serok", "f4f383");
      // with spread
    LocalDateTime in = LocalDateTime.of(2017, 1, 1, 10, 05);
    LocalDateTime out = LocalDateTime.of(2017, 1, 2, 1, 35);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 0.0, 1));
      // without spread
    in = LocalDateTime.of(2017, 1, 2, 9, 0);
    out = LocalDateTime.of(2017, 1, 2, 12, 0);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 0.0, 1));
      // with spread across 2 TimePairs
    in = LocalDateTime.of(2017, 1, 3, 9, 30);
    out = LocalDateTime.of(2017, 1, 3, 12, 10);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 0.0, 1));
    in = LocalDateTime.of(2017, 1, 3, 14, 10);
    out = LocalDateTime.of(2017, 1, 3, 23, 42);
    timeCard.addTimePair(new TimePair(in, out, "dept", "paycode", 0.0, 1));

      // spread is any calendar day (6am to 5:59am) working more than 10 hrs
      // from start of first clock in to the end of last clock out.
    assertEquals(timeCard.getDaysWithSpread(), 2, 0);
  }
}
