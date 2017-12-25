package timecard;

import java.io.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;

import timecard.Controller;

public class View {
  final String FIFTEEN_MINUTES = "15 minutes";
  final String ONE_MINUTE = "1 minute";
  final String WINDOW_TITLE = "ADP Time Card Auditor v1";

  Controller controller;
  File inputFile;

  FileChooser fileChooser;
  Button runButton;
  Button selectInputFileButton;
  ComboBox<String> roundMinutesComboBox;
  Label statusLabel;
  Stage stage;

  public View(Stage stage, Controller controller) {
    this.stage = stage;
    this.controller = controller;
    inputFile = null;
  
    runButton = new Button();
    selectInputFileButton = new Button();
    roundMinutesComboBox = new ComboBox<String>();
    statusLabel = new Label("Select an input file");

    roundMinutesComboBox.getItems().addAll(ONE_MINUTE, FIFTEEN_MINUTES);
    roundMinutesComboBox.setValue(ONE_MINUTE);

    runButton.setText("Run");
    selectInputFileButton.setText("Select Input File");
    inputFile = null;

    roundMinutesComboBox.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        String selectedItem = (String)roundMinutesComboBox.getValue();
        if(selectedItem.equals(ONE_MINUTE)) {
          controller.setRoundingOption(1);
        } else if (selectedItem.equals(FIFTEEN_MINUTES)) {
          controller.setRoundingOption(15);
        }
      }
    });
    selectInputFileButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Comma Separated Value", "*.CSV", "*.csv"));
        inputFile = fileChooser.showOpenDialog(stage);
        controller.setInputFile(inputFile);
        setStatusText("File selected: " + inputFile.getName());
        toggleRunButton();
      }
    });
    runButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        if(!controller.isReadyToProcess()){
          new Alert(AlertType.CONFIRMATION, "Please select an input file first.");
        } else {
          try {
            String fileName = controller.getInputFilePath();
            statusLabel.setText("... processing " + fileName);
            controller.processFile();
          } catch (Exception e) {
            statusLabel.setText("There was a problem processing the file.");
            try {
              FileWriter errorFile = new FileWriter("ErrorLog.txt");
              errorFile.write(" \n\nMessage: " + e.getMessage() + "\n\nStack Trace: " + e.getStackTrace()[0].toString());
              errorFile.close();
              java.awt.Desktop.getDesktop().edit(new File("ErrorLog.txt"));
            } catch (IOException ex) {
              statusLabel.setText("Problem processing the file and logging the exception.");
            }
            toggleRunButton();
          }
        }
      }
    });

    GridPane root = new GridPane();
    root.setVgap(4);
    root.setHgap(10);
    root.setPadding(new Insets(5, 5, 5, 5));
    root.add(selectInputFileButton, 0, 0);
    root.add(roundMinutesComboBox, 1, 0);
    root.add(runButton, 0, 1);
    root.add(statusLabel, 0, 2);
    Scene scene = new Scene(root, 500, 275);
    stage.setTitle("ADP Time Card Audit");
    stage.setScene(scene);
    setStatusText("No file selected.");
    stage.show();
    toggleRunButton();
  }

  public void setStatusText(String message) {
    statusLabel.setText(message);
  }

  public void sendPopupWindow(String message) {
    new Alert(AlertType.CONFIRMATION, message).show();
  }

  private void toggleRunButton(){
    if(controller.isReadyToProcess()){
      new Alert(AlertType.CONFIRMATION, "READY!").show();
      runButton.setDisable(false);
    } else {
      new Alert(AlertType.CONFIRMATION, "NOT READY").show();
      runButton.setDisable(true);
    }
  }
}
