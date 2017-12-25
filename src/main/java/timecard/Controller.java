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

  public void setView(View aView) {
    view = aView;
  }

  public String getInputFilePath() {
    return csvHandler.getInputFilePath();
  }

  public void setInputFile(File file) {
    csvHandler.setInputFile(file);
  }

  public void setOutputFilePath(String filePath) {
    try {
      csvHandler.setOutputFilePath(filePath);
    } catch(FileNameExtensionError er) {
      view.setStatusText(er.getMessage());
    }
  }

  public void setRoundingOption(Integer roundToNearest) {
    csvHandler.setRoundingOption(roundToNearest);
  }

  public boolean isReadyToProcess() {
    return csvHandler.isReady();
  }

  // TODO: argument should be a hash of configuration key value pairs
  public void processFile() {
    if(csvHandler.isReady()) {
      try {
        csvHandler.processFile();
        view.setStatusText(csvHandler.getTimeCards().size() + " time cards read from file. Writing summary file...");
        csvHandler.writeSummaryFile();
      } catch(FileNameExtensionError ex) {
        view.setStatusText(ex.getMessage());
        view.showAlert(ex.getMessage());
        ex.printStackTrace();
      } catch(Exception ex) {
        view.setStatusText(ex.getMessage());
        view.showAlert(ex.getMessage());
        ex.printStackTrace();
      }
    }
  }
}
