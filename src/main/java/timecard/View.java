package timecard;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.Tab;

import timecard.Controller;

public class View {

  final Integer TABLE_HEIGHT = 600;
  final Integer TABLE_WIDTH = 850;
  final Integer MENU_HEIGHT = 30;
  final Integer STATUS_LABEL_HEIGHT = 25;
  final Integer STATUS_LABEL_WIDTH = TABLE_WIDTH-75;
  final Integer WINDOW_HEIGHT = TABLE_HEIGHT + STATUS_LABEL_HEIGHT + MENU_HEIGHT;
  final Integer WINDOW_WIDTH = TABLE_WIDTH;
  final String IMPORT_TAB_TEXT = "Raw Input File";
  final String EXPORT_TAB_TEXT = "Calculated Summary";
  Controller controller;
  File inputFile;
  FileChooser fileChooser;
  MenuItem exportMenuItem;
  Button selectInputFileButton;
  Label statusLabel;
  Label minuteLabel;
  Stage stage;
  TableView<TimePairRow> importTableView;
  TableView<SummaryRow> exportTableView;

  public View(Stage stage, Controller controller) {
    this.stage = stage;
    this.controller = controller;
    inputFile = null;

    importTableView = new TableView<TimePairRow>();
    importTableView.setMinHeight(TABLE_HEIGHT-20);
    importTableView.setMinWidth(TABLE_WIDTH);
    
    exportTableView = new TableView<SummaryRow>();
    exportTableView.setMinHeight(TABLE_HEIGHT-20);
    exportTableView.setMinWidth(TABLE_WIDTH);

    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    Tab importTab = new Tab();
    Tab exportTab = new Tab();
    importTab.setText(IMPORT_TAB_TEXT);
    exportTab.setText(EXPORT_TAB_TEXT);
    importTab.setContent(importTableView);
    exportTab.setContent(exportTableView);
    tabPane.getTabs().addAll(importTab, exportTab);

    statusLabel = new Label("Select an input file");
    statusLabel.setMinWidth(STATUS_LABEL_WIDTH);
    statusLabel.setMaxWidth(STATUS_LABEL_WIDTH);
    minuteLabel = new Label("Test");
    setRoundingOption(1);

    GridPane grid = new GridPane();
    grid.setVgap(0);
    grid.setHgap(10);
    grid.add(tabPane, 0, 2);
    
    GridPane statusGrid = new GridPane();
    statusGrid.setVgap(5);
    statusGrid.setHgap(10);
    statusGrid.setPadding(new Insets(5, 5, 5, 5));
    statusGrid.add(minuteLabel, 1, 0);
    statusGrid.add(statusLabel, 0, 0);
    grid.add(statusGrid, 0, 3);

    MenuBar menuBar = new MenuBar();
    menuBar.setMinHeight(MENU_HEIGHT);
    menuBar.setMaxHeight(MENU_HEIGHT);
    Menu fileMenu = new Menu("File");
    MenuItem openMenuItem = new MenuItem("Open");
    exportMenuItem = new MenuItem("Export..");
    MenuItem exitMenuItem = new MenuItem("Exit");
    fileMenu.getItems().addAll(openMenuItem, exportMenuItem, exitMenuItem);
    openMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
    openMenuItem.setOnAction(actionEvent -> handleOpenFile());
    exportMenuItem.setOnAction(actionEvent -> handleExport());
    exitMenuItem.setOnAction(actionEvent -> System.exit(0));
    exitMenuItem.setAccelerator(KeyCombination.keyCombination("Esc"));
    
    Menu optionsMenu = new Menu("Options");
    MenuItem oneMinuteMenuItem = new MenuItem("Round to nearest minute");
    MenuItem fifteenMinuteMenuItem = new MenuItem("Round to nearest quarter hour");
    MenuItem thirtyMinuteMenuItem = new MenuItem("Round to nearest half hour");
    optionsMenu.getItems().addAll(oneMinuteMenuItem, fifteenMinuteMenuItem, thirtyMinuteMenuItem);
    oneMinuteMenuItem.setOnAction(actionEvent -> setRoundingOption(1));
    fifteenMinuteMenuItem.setOnAction(actionEvent -> setRoundingOption(15));
    thirtyMinuteMenuItem.setOnAction(actionEvent -> setRoundingOption(30));

    Menu helpMenu = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    MenuItem gettingStartedMenuItem = new MenuItem("Getting Started...");
    helpMenu.getItems().addAll(aboutMenuItem, gettingStartedMenuItem);
    aboutMenuItem.setOnAction(actionEvent -> openAboutPage());
    gettingStartedMenuItem.setOnAction(actionEvent -> openGettingStartedPage());

    menuBar.getMenus().addAll(fileMenu, optionsMenu, helpMenu);
    
    BorderPane root = new BorderPane();
    root.setTop(menuBar);
    root.setCenter(grid);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle("ADP Time Card Audit");
    stage.setMinHeight(WINDOW_HEIGHT);
    stage.setMaxHeight(WINDOW_HEIGHT);
    stage.setMinWidth(WINDOW_WIDTH);
    stage.setMaxWidth(WINDOW_WIDTH);
    setStatusText("No file selected.");
    stage.show();
    toggleExportMenu();
  }

  private void openAboutPage() {
    String message = "There was a problem loading the About page.";
    try{
      message = new String(Files.readAllBytes(Paths.get("src/main/resources/about.txt")));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    showPage("About", message);
  }

  private void openGettingStartedPage() {
    String message = "There was a problem loading the Getting Started page.";
    try{
      message = new String(Files.readAllBytes(Paths.get("src/main/resources/getting-started.txt")));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    showPage("Getting Started", message);
  }

  private void setRoundingOption(Integer minute) {
    controller.setRoundingOption(minute);
    setupExportTableView();
    setMinuteLabel(minute);
  }

  private void setupExportTableView() {
    try {
      exportTableView.getItems().remove(0, exportTableView.getItems().size());
      exportTableView.getColumns().remove(0, exportTableView.getColumns().size());
      ObservableList<SummaryRow> tableData = FXCollections.observableArrayList(controller.getSummaryRows());
      exportTableView.setItems(tableData);
      TableColumn column;
      for(String header : SummaryRow.getHeaderNames()) {
        column = new TableColumn(header);
        column.setCellValueFactory(new PropertyValueFactory<>(header));
        exportTableView.getColumns().add(column);
      }
    } catch(Exception ex) {
      setStatusText("Error summarizing file: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void setupImportTableView() {
    try {
      importTableView.getItems().remove(0, importTableView.getItems().size());
      importTableView.getColumns().remove(0, importTableView.getColumns().size());
      ObservableList<TimePairRow> tableData = FXCollections.observableArrayList(controller.getTimePairRows());
      importTableView.setItems(tableData);
      TableColumn column;
      for(String header : TimePairRow.getHeaderNames()){
        column = new TableColumn(header);
        column.setCellValueFactory(new PropertyValueFactory<>(header));
        importTableView.getColumns().add(column);
      }
      setStatusText("File selected: " + inputFile.getName());
    } catch(Exception ex) {
      setStatusText("Error importing file: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  private void handleOpenFile() {
    fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Comma Separated Value", "*.CSV", "*.csv"));
    inputFile = fileChooser.showOpenDialog(stage);
    if(inputFile != null) {
      controller.setInputFile(inputFile);
      setupImportTableView();
      setupExportTableView();
      toggleExportMenu();
    }
  }

  private void handleExport() {
    if(!controller.isReadyToProcess()){
      showAlert("Please select an input file first. File > Open to open a CSV file.");
    } else {
      try {
        String fileName = controller.getInputFilePath();
        controller.exportSummaryFile();
        statusLabel.setText("Summary file exported to SummaryResults.csv");
      } catch (Exception e) {
        statusLabel.setText("There was a problem processing the file.");
        e.printStackTrace();
        toggleExportMenu();
      }
    }
  }

  public void setStatusText(String message) {
    statusLabel.setText(message);
  }

  public void setMinuteLabel(Integer minutes) {
    minuteLabel.setText(minutes + " min");
  }

  public void showAlert(String message) {
    new Alert(AlertType.CONFIRMATION, message).show();
  }

  public void showPage(String header, String message) {
    Alert page = new Alert(AlertType.INFORMATION, message);
    page.setHeaderText(header);
    page.setTitle(header);
    page.show();
    page.setWidth(650);
    page.setResizable(true);
  }

  private void toggleExportMenu(){
    if(controller.isReadyToProcess()){
      exportMenuItem.setDisable(false);
    } else {
      exportMenuItem.setDisable(true);
    }
  }
}
