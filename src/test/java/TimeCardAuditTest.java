import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class TimeCardAuditTest {
  @Test
  public void evaluatesCsv() {
    Path csvFile = Paths.get("../resources/IchiranTimecardReport-new.csv");
    byte[] fileArray;
    try {
      fileArray = Files.readAllBytes(csvFile);
      System.out.println(csvFile.toString());
      System.out.println(fileArray.toString());
    } catch(IOException e) {
      System.out.println(e.toString());
    }
  }
}
