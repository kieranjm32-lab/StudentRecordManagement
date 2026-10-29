import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler - saves and loads records so data survives between runs.
 * Format: one student per line:  name|registerNumber|age|marks|courseName|courseCode
 */
public class FileHandler {
    private final String fileName;

    public FileHandler(String fileName) {
        this.fileName = fileName;
    }

    public void save(List<Student> students) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
            for (Student s : students) {
                w.write(s.toFileString());
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save to " + fileName + " (" + e.getMessage() + ")");
        }
    }

    public List<Student> load() {
        List<Student> list = new ArrayList<Student>();
        File f = new File(fileName);
        if (!f.exists()) return list;               // first run: nothing to load
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    list.add(Student.fromFileString(line));
                } catch (RuntimeException e) {
                    System.out.println("Skipped a bad line in " + fileName);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + fileName + " (" + e.getMessage() + ")");
        }
        return list;
    }
}
