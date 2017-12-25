package timecard;

import javafx.application.Application;
import javafx.stage.Stage;

/* TimeCardAudit.java

Author: Scott Serok
Created: 30 Dec 2016
Modified: n/a

This program is intended for non-production use. The project started as a simple
audit script, and it's still just a simple audit script in a single Java file.

This program is intended only to audit ADP Workforce Now V11 Timecard CSV export.

The program is intended to show the number of hours worked after midnight if a shift started before midnight.
The CSV export must contain the following headers:
Company Code	Payroll Name	File Number	Pay Date	Time In	Time Out	Hours	Earnings Code	Worked Department
The only headers used for auditing are Pay Date, Time In, Time Out, Hours, and Earnings Code

*/

public class TimeCardAudit extends Application {

  @Override
  public void start(Stage primaryStage) {
    Controller controller = new Controller();
    View view = new View(primaryStage, controller);
    controller.setView(view);
  }

	public static void main(String[] args) {
    launch(args);
  }
}
