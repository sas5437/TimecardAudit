package timecard;

/* Auditor.java

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

public class Auditor {
  public static void main(String[] args) {
    View view = new View();
    Controller controller = new Controller();
    controller.setView(view); 
    view.setVisible(true);
  }
}
