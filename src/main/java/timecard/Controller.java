package timecard;

import java.io.File;
import java.util.ArrayList;
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

  public Integer getRoundingOption() {
    return csvHandler.getRoundingOption();
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

  public ArrayList<TimePairRow> getTimePairRows() throws Exception {
    return csvHandler.processTimePairRows();
  }

  public ArrayList<SummaryRow> getSummaryRows() throws Exception {
    return csvHandler.processSummaryRows();
  }

  public void exportSummaryFile() throws Exception {
    csvHandler.exportSummaryFile();
  }
}
