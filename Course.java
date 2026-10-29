/**
 * Course - optional class holding course details.
 * Fields are private (encapsulation); access is through getters and setters.
 */
public class Course {
    private String courseName;
    private String courseCode;

    public Course(String courseName, String courseCode) {
        setCourseName(courseName);
        setCourseCode(courseCode);
    }

    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }

    public void setCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }
        this.courseName = courseName.trim();
    }

    public void setCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        this.courseCode = courseCode.trim();
    }

    @Override
    public String toString() {
        return courseName + " (" + courseCode + ")";
    }
}
