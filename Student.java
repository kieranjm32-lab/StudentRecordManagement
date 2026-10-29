/**
 * Student - holds the record of ONE student.
 * All fields are private; setters validate the data (encapsulation).
 */
public class Student {
    private String name;
    private String registerNumber;
    private int age;
    private Course course;
    private double marks;

    public Student(String name, String registerNumber, int age, Course course, double marks) {
        setName(name);
        setRegisterNumber(registerNumber);
        setAge(age);
        setCourse(course);
        setMarks(marks);
    }

    // ---------- getters ----------
    public String getName()           { return name; }
    public String getRegisterNumber() { return registerNumber; }
    public int getAge()               { return age; }
    public Course getCourse()         { return course; }
    public double getMarks()          { return marks; }

    // ---------- setters (with validation) ----------
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public void setRegisterNumber(String registerNumber) {
        if (registerNumber == null || registerNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Register number cannot be empty.");
        }
        this.registerNumber = registerNumber.trim();
    }

    public void setAge(int age) {
        if (age < 1 || age > 100) {
            throw new IllegalArgumentException("Age must be between 1 and 100.");
        }
        this.age = age;
    }

    public void setCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        this.course = course;
    }

    public void setMarks(double marks) {
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be between 0 and 100.");
        }
        this.marks = marks;
    }

    // ---------- display ----------
    public void display() {
        System.out.println("  Name         : " + name);
        System.out.println("  Register No. : " + registerNumber);
        System.out.println("  Age          : " + age);
        System.out.println("  Course       : " + course);
        System.out.println("  Marks        : " + marks);
    }

    // ---------- file support ----------
    public String toFileString() {
        return name + "|" + registerNumber + "|" + age + "|" + marks + "|"
                + course.getCourseName() + "|" + course.getCourseCode();
    }

    public static Student fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 6) {
            throw new IllegalArgumentException("Bad record: " + line);
        }
        return new Student(p[0], p[1], Integer.parseInt(p[2]),
                new Course(p[4], p[5]), Double.parseDouble(p[3]));
    }
}
