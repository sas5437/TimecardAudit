package timecard;

import java.io.File;
import timecard.View;
import timecard.FileNameExtensionError;

public class Controller {
  View view;
  CsvHandler csvHandler;

  public Controller() {
    view = new View(this);
    csvHandler = new CsvHandler();
    view.setVisible(true);
  }

  public void setFile(File file) {
    try {
      csvHandler.setFile(file);
    } catch(FileNameExtensionError er) {
      view.setStatusText(er.getMessage());
    }
  }

  public void setRoundingOption(boolean isRoundingToFifteen) {
    csvHandler.setRoundingOption(isRoundingToFifteen);
  }

  public boolean isReadyToProcess() {
    if(csvHandler.isReady()) {
      return true;
    } else {
      return false;
    }
  }

  // TODO: argument should be a hash of configuration key value pairs
  public void processFile() {
    if(csvHandler.isReady()) {
      try {
        csvHandler.processFile();
      } catch(Exception ex) {
        view.setStatusText(ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
}
