import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.ArrayList;
import timecard.Controller;

public class ControllerTest {
  @Test
  public void testEmptyConstructor() {
    Controller controller = new Controller();
    assertEquals(controller.isReadyToProcess(), false);
  }
}
