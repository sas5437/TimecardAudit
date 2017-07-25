import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;

import timecard.*;

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
    TimeCard timeCard = new TimeCard("A01", "Scott", "Serok");
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
    TimeCard timeCard = new TimeCard("A01", "Scott", "Serok");
    TimePair timePair = new TimePair();
    timeCard.addTimePair(timePair);
    assertEquals(timeCard.getTimePairs().size(), 1);
  }
}
