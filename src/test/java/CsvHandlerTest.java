import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;
import java.io.File;
import timecard.CsvHandler;
import timecard.TimeCard;
import timecard.TimePair;

public class CsvHandlerTest {
  @Test
  public void testEmptyConstructor() {
    CsvHandler csvHandler = new CsvHandler();
    assertEquals(csvHandler.getInputFilePath(), "");
    assertEquals(csvHandler.getOutputFilePath(), "AuditResults.csv");
    assertEquals(csvHandler.isReady(), false);
  }

  @Test
  public void testProcessFile() {
    CsvHandler csvHandler = new CsvHandler();
    try {
      File file = new File("src/test/resources/IchiranTimecardReport-new.csv");
      csvHandler.setInputFile(file);
      csvHandler.processFile();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    // Expect 60 TimeCards from this file, 60 workers clocked time
    assertEquals(csvHandler.getTimeCards().size(), 60);
  }
}
