import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;
import java.io.File;
import timecard.CsvHandler;
import timecard.TimeCard;
import timecard.TimePair;
import timecard.SummaryRow;

public class CsvHandlerTest {
  @Test
  public void testEmptyConstructor() {
    CsvHandler csvHandler = new CsvHandler();
    assertEquals(csvHandler.getInputFilePath(), "");
    assertEquals(csvHandler.getOutputFilePath(), "AuditResults.csv");
    assertEquals(csvHandler.isReady(), false);
  }

  @Test
  public void testProcessSummaryRows() {
    CsvHandler csvHandler = new CsvHandler();
    ArrayList<SummaryRow> summaryRows;
    try {
      File file = new File("src/test/resources/ExampleTimecardReport.csv");
      csvHandler.setInputFile(file);
      summaryRows = csvHandler.processSummaryRows();
      assertEquals(summaryRows.size(), 60);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}
