STUDENT RECORD MANAGEMENT SYSTEM  -  Team 13
Java prototype (OOP + singly linked list), with a Swing GUI and a console version

HOW TO RUN  (needs the Java JDK, version 8 or newer)
  GUI (recommended for the demo)
     Windows : double-click  run_gui.bat
     Mac/Linux: ./run_gui.sh   or   javac *.java && java StudentApp
  Console version
     Windows : double-click  run_console.bat
     Mac/Linux: ./run_console.sh  or  javac *.java && java Main

FILES
  StudentApp.java            Swing GUI: form, table, live linked-list picture, animated search and sort
  Main.java                  Console menu (same features, text based)
  StudentRecordManager.java  Linked list + add, insert, search, update, delete, display, sort
  Student.java               One student's record (private fields, getters/setters, validation)
  Course.java                Course name and code
  Node.java                  One node of the linked list (data + next)
  FileHandler.java           Saves/loads records to students.txt

GUI FEATURES
  Add Student        - fill the form; leave "position" empty to add at the end, or type a position to insert there
  Search             - linear search by register number; each node lights up as it is checked
  Sort               - bubble sort by register number or marks; every comparison and swap is shown
  Edit / Delete      - select a row (double-click also edits), then use the buttons
  Data is saved to students.txt after every change and loaded on the next start.
  First run only: four sample students are added so the screen is not empty.
