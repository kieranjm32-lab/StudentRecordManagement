import java.util.List;
import java.util.Scanner;

/**
 * Main - the user interface and controller.
 * Shows the menu, reads input, validates it, calls StudentRecordManager,
 * prints the result, and returns to the menu until the user chooses Exit.
 */
public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final StudentRecordManager manager = new StudentRecordManager();
    private static final FileHandler files = new FileHandler("students.txt");

    public static void main(String[] args) {
        // Load saved records (if any)
        List<Student> saved = files.load();
        for (Student s : saved) manager.addStudent(s);
        if (!saved.isEmpty()) System.out.println("Loaded " + saved.size() + " record(s) from students.txt");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ", 1, 7);
            System.out.println();
            switch (choice) {
                case 1: addStudent();    break;
                case 2: manager.displayStudents(); break;
                case 3: searchStudent(); break;
                case 4: updateStudent(); break;
                case 5: deleteStudent(); break;
                case 6: sortStudents();  break;
                case 7:
                    files.save(manager.getAll());
                    System.out.println("Records saved. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    // ---------------- menu ----------------
    private static void printMenu() {
        System.out.println();
        System.out.println("STUDENT RECORD MANAGEMENT SYSTEM");
        System.out.println("================================");
        System.out.println("1. Add Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Sort Records");
        System.out.println("7. Exit");
        System.out.println();
    }

    // ---------------- operations ----------------
    private static void addStudent() {
        System.out.println("> Add Student");
        String reg = readText("Register Number: ");
        if (manager.searchStudent(reg) != null) {
            System.out.println("Error: a student with register number " + reg + " already exists.");
            return;
        }
        String name = readText("Name: ");
        int age = readInt("Age: ", 1, 100);
        String courseName = readText("Course (e.g. CSE): ");
        String courseCode = readText("Course Code: ");
        double marks = readDouble("Marks (0-100): ", 0, 100);
        String pos = readOptional("Position (press Enter to add at the end): ");

        Student s = new Student(name, reg, age, new Course(courseName, courseCode), marks);
        boolean ok;
        if (pos.isEmpty()) {
            ok = manager.addStudent(s);
        } else {
            try {
                ok = manager.insertAt(Integer.parseInt(pos), s);
                if (!ok) System.out.println("Error: position must be between 1 and " + (manager.getSize() + 1) + ".");
                else System.out.println("[OK] Student inserted at position " + pos + ".");
                if (ok) files.save(manager.getAll());
                return;
            } catch (NumberFormatException e) {
                System.out.println("Error: position must be a number.");
                return;
            }
        }
        if (ok) {
            System.out.println("[OK] Student added successfully.");
            files.save(manager.getAll());
        } else {
            System.out.println("Error: could not add student.");
        }
    }

    private static void searchStudent() {
        System.out.println("> Search Student");
        String reg = readText("Register Number: ");
        Student s = manager.searchStudent(reg);
        if (s == null) {
            System.out.println("NOT FOUND: no student with register number " + reg);
        } else {
            System.out.println("FOUND");
            s.display();
        }
    }

    private static void updateStudent() {
        System.out.println("> Update Student");
        String reg = readText("Register Number: ");
        Student s = manager.searchStudent(reg);
        if (s == null) {
            System.out.println("NOT FOUND: no student with register number " + reg);
            return;
        }
        System.out.println("Current record:");
        s.display();
        System.out.println("(press Enter to keep the current value)");

        String name = readOptional("New name: ");
        Integer age = readOptionalInt("New age: ", 1, 100);
        String cName = readOptional("New course: ");
        String cCode = readOptional("New course code: ");
        Double marks = readOptionalDouble("New marks: ", 0, 100);

        Course course = null;
        if (!cName.isEmpty() || !cCode.isEmpty()) {
            course = new Course(cName.isEmpty() ? s.getCourse().getCourseName() : cName,
                                cCode.isEmpty() ? s.getCourse().getCourseCode() : cCode);
        }
        manager.updateStudent(reg, name.isEmpty() ? null : name, age, course, marks);
        files.save(manager.getAll());
        System.out.println("[OK] Student updated successfully.");
    }

    private static void deleteStudent() {
        System.out.println("> Delete Student");
        String reg = readText("Register Number: ");
        Student s = manager.searchStudent(reg);
        if (s == null) {
            System.out.println("NOT FOUND: no student with register number " + reg);
            return;
        }
        String sure = readText("Delete " + s.getName() + "? (y/n): ");
        if (sure.equalsIgnoreCase("y")) {
            manager.deleteStudent(reg);
            files.save(manager.getAll());
            System.out.println("[OK] Student deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static void sortStudents() {
        System.out.println("> Sort Records (bubble sort)");
        System.out.println("1. By register number");
        System.out.println("2. By marks (highest first)");
        int c = readInt("Choose: ", 1, 2);
        manager.sortStudents(c == 2);
        files.save(manager.getAll());
        System.out.println("[OK] Records sorted.");
        manager.displayStudents();
    }

    // ---------------- input helpers (validation) ----------------
    private static String clean(String s) {
        return s.replace("|", " ").trim();       // '|' is the file separator
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String t = clean(in.nextLine());
            if (!t.isEmpty()) return t;
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readOptional(String prompt) {
        System.out.print(prompt);
        return clean(in.nextLine());
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(in.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    private static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(in.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static Integer readOptionalInt(String prompt, int min, int max) {
        while (true) {
            String t = readOptional(prompt);
            if (t.isEmpty()) return null;
            try {
                int v = Integer.parseInt(t);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Please enter a whole number from " + min + " to " + max + ", or press Enter.");
        }
    }

    private static Double readOptionalDouble(String prompt, double min, double max) {
        while (true) {
            String t = readOptional(prompt);
            if (t.isEmpty()) return null;
            try {
                double v = Double.parseDouble(t);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) { /* fall through */ }
            System.out.println("Please enter a number from " + min + " to " + max + ", or press Enter.");
        }
    }
}
