package timecard;

import java.io.File;
import timecard.View;
import timecard.CsvHandler;
import timecard.FileNameExtensionError;

public class Controller {
  View view;
  CsvHandler csvHandler;

  public Controller() {
    csvHandler = new CsvHandler();
  }

  public void setView(View view) {
    this.view = view;
    this.view.setup(this);
  }

  public void setFile(File file) {
    try {
      csvHandler.setFile(file);
      view.setStatusText(file.getName() + " selected.");
    } catch(FileNameExtensionError er) {
      view.sendPopupWindow(er.getMessage());
    }
  }

  public void setRoundingOption(Integer roundToNearest) {
    csvHandler.setRoundingOption(roundToNearest);
  }

  public boolean isReadyToProcess() {
    if(csvHandler.isReady()) {
      return true;
    } else {
      return false;
    }
  }

  // TODO: argument should be a hash of configuration key value pairs
  public void processFile() throws Exception {
    if(csvHandler.isReady()) {
      try {
        csvHandler.processFile();
      } catch(Exception ex) {
        csvHandler.setFile(null);
        view.setStatusText("There was a problem processing this file.");
        view.sendPopupWindow(ex.getMessage());
        ex.printStackTrace();
        throw ex;
      }
    }
  }
}
