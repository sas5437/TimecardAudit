package timecard;

import java.awt.Desktop;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import timecard.Controller;

public class View extends JFrame {
  final String FIFTEEN_MINUTES = "15 minutes";
  final String ONE_MINUTE = "1 minute";
  final String WINDOW_TITLE = "ADP Time Card Auditor v1";

  Controller controller;
  File file;

  JFileChooser chooser;
  FileNameExtensionFilter filter;
  JButton runButton;
  JLabel statusbar;

  public View() {
    super();
  }

  public void setup(Controller controller) {
    this.controller = controller;
    file = null;
    String[] minuteRoundListOptions = { ONE_MINUTE, FIFTEEN_MINUTES };
    
    setTitle(WINDOW_TITLE);
    setSize(500, 275);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

    runButton = new JButton("Run");
    JButton fileButton = new JButton("Select File");

    JComboBox minuteRoundList = new JComboBox(minuteRoundListOptions); 
    JLabel minuteListLabel = new JLabel("Round calculations to the nearest");
    statusbar = new JLabel();
    statusbar.setForeground(Color.BLUE);

    minuteRoundList.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String selectedItem = (String)minuteRoundList.getSelectedItem();
        if(selectedItem.equals(ONE_MINUTE)) {
          controller.setRoundingOption(1);

        } else if (selectedItem.equals(FIFTEEN_MINUTES)) {
          controller.setRoundingOption(15);

        }
      }
    });


    // Create a file chooser that allows you to pick a CSV file
    fileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        chooser = new JFileChooser();
        filter = new FileNameExtensionFilter("Comma Separated Value", "CSV", "csv");
        chooser.setFileFilter(filter);
        int option = chooser.showOpenDialog(View.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
          controller.setInputFile(file);
          String filename = (file == null ? "nothing" : file.getName());
          statusbar.setText(filename + " selected.");
        }
        else {
          statusbar.setText("Canceled.");
        }
        toggleRunButton();
      }
    });

    // Create a file chooser that allows you to pick a CSV file
    runButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(!controller.isReadyToProcess()){
          JOptionPane.showMessageDialog(View.this, "Please select a file first.");

        } else {
          try {
            String fileName = controller.getInputFilePath();
            statusbar.setText("... processing " + fileName);
            controller.processFile();

          } catch (Exception e) {
            statusbar.setText("There was a problem processing the file.");
            try {
              FileWriter errorFile = new FileWriter("ErrorLog.txt");
              errorFile.write(" \n\nMessage: " + e.getMessage() + "\n\nStack Trace: " + e.getStackTrace()[0].toString());
              errorFile.close();
              java.awt.Desktop.getDesktop().edit(new File("ErrorLog.txt"));

            } catch (IOException ex) {
              statusbar.setText("Problem processing the file and logging the exception.");

            }
            toggleRunButton();
          }
        }
      }
    });

    minuteRoundList.setSelectedIndex(0);

    JPanel topPanel = new JPanel();
    JPanel centerPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JPanel statusPanel = new JPanel();

    topPanel.add(fileButton);
    centerPanel.add(minuteListLabel);
    centerPanel.add(minuteRoundList);
    bottomPanel.add(runButton);
    statusPanel.add(statusbar);

    Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    c.add(topPanel, BorderLayout.NORTH);
    c.add(centerPanel, BorderLayout.CENTER);
    c.add(bottomPanel, BorderLayout.SOUTH);
    c.add(statusPanel, BorderLayout.SOUTH);
    toggleRunButton();
    resetStatusBar();
  }

  public void resetStatusBar() {
    setStatusText("No file selected.");
  }

  public void setStatusText(String message) {
    statusbar.setText(message);
  }

  public void sendPopupWindow(String message) {
    JTextArea textArea = new JTextArea(6, 25);
    textArea.setText(message);
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    JOptionPane.showMessageDialog(View.this, scrollPane);
  }

  private void toggleRunButton(){
    if(controller.isReadyToProcess()){
      runButton.setEnabled(true);
    } else {
      runButton.setEnabled(false);
    }
  }
}
