import java.util.ArrayList;
import java.util.List;

/**
 * StudentRecordManager - owns the singly linked list of Students and
 * performs every operation: add, delete, search, update, display, sort.
 *
 *   head -> [Student] -> [Student] -> [Student] -> NULL
 */
public class StudentRecordManager {
    private Node head;   // first node
    private Node tail;   // last node (makes adding at the end O(1))
    private int size;

    // ---------------- ADD ----------------
    /** Add at the end of the list. O(1) because we keep a tail pointer. */
    public boolean addStudent(Student s) {
        if (searchStudent(s.getRegisterNumber()) != null) {
            return false;                       // duplicate register number
        }
        Node n = new Node(s);
        if (head == null) {
            head = tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
        return true;
    }

    /**
     * Insert at a given position (1 = first). Only two links change, no shifting.
     * Returns false for a duplicate register number or an invalid position.
     */
    public boolean insertAt(int position, Student s) {
        if (position < 1 || position > size + 1) return false;
        if (searchStudent(s.getRegisterNumber()) != null) return false;
        Node n = new Node(s);
        if (position == 1) {                    // new head
            n.next = head;
            head = n;
            if (tail == null) tail = n;
        } else {
            Node prev = head;
            for (int i = 1; i < position - 1; i++) prev = prev.next;  // walk to node before
            n.next = prev.next;                 // new node -> next one
            prev.next = n;                      // previous node -> new node
            if (n.next == null) tail = n;
        }
        size++;
        return true;
    }

    // ---------------- SEARCH (linear search) ----------------
    public Student searchStudent(String registerNumber) {
        Node cur = head;
        while (cur != null) {                   // check every node, one by one
            if (cur.data.getRegisterNumber().equalsIgnoreCase(registerNumber.trim())) {
                return cur.data;
            }
            cur = cur.next;
        }
        return null;
    }

    // ---------------- UPDATE ----------------
    /** Any parameter that is null keeps the old value. */
    public boolean updateStudent(String registerNumber, String name, Integer age,
                                 Course course, Double marks) {
        Student s = searchStudent(registerNumber);
        if (s == null) return false;
        if (name != null)   s.setName(name);
        if (age != null)    s.setAge(age);
        if (course != null) s.setCourse(course);
        if (marks != null)  s.setMarks(marks);
        return true;
    }

    // ---------------- DELETE ----------------
    public boolean deleteStudent(String registerNumber) {
        Node prev = null, cur = head;
        while (cur != null) {
            if (cur.data.getRegisterNumber().equalsIgnoreCase(registerNumber.trim())) {
                if (prev == null) head = cur.next;     // deleting the head: HEAD moves on
                else prev.next = cur.next;             // re-link neighbours
                if (cur == tail) tail = prev;
                size--;
                return true;
            }
            prev = cur;
            cur = cur.next;
        }
        return false;
    }

    // ---------------- DISPLAY (traversal) ----------------
    public void displayStudents() {
        if (head == null) {
            System.out.println("No records found.");
            return;
        }
        System.out.println();
        System.out.printf("%-4s %-12s %-20s %-4s %-18s %-6s%n",
                "No.", "Register No.", "Name", "Age", "Course", "Marks");
        System.out.println("-----------------------------------------------------------------------");
        Node cur = head;                        // start at HEAD, walk to NULL
        int i = 1;
        while (cur != null) {
            Student s = cur.data;
            System.out.printf("%-4d %-12s %-20s %-4d %-18s %-6.1f%n",
                    i++, s.getRegisterNumber(), s.getName(), s.getAge(),
                    s.getCourse().getCourseName(), s.getMarks());
            cur = cur.next;
        }
        System.out.println("Total students: " + size);
    }

    // ---------------- SORT (bubble sort) ----------------
    /**
     * Bubble sort: compare neighbours, swap if out of order, repeat.
     * byMarks = true  -> highest marks first
     * byMarks = false -> register number ascending
     */
    public void sortStudents(boolean byMarks) {
        bubbleSort(byMarks, null);
    }

    /**
     * Same bubble sort, but also records every step so the GUI can replay it.
     * Each step is {type, i, j}: type 0 = compare positions i and j,
     * type 1 = swap positions i and j, type 2 = a new pass starts (i = pass number).
     */
    public List<int[]> sortStudentsRecorded(boolean byMarks) {
        List<int[]> steps = new ArrayList<int[]>();
        bubbleSort(byMarks, steps);
        return steps;
    }

    private void bubbleSort(boolean byMarks, List<int[]> steps) {
        if (head == null || head.next == null) return;
        boolean swapped;
        int pass = 0;
        do {
            swapped = false;
            pass++;
            if (steps != null) steps.add(new int[]{2, pass, 0});
            Node cur = head;
            int i = 0;
            while (cur.next != null) {
                if (steps != null) steps.add(new int[]{0, i, i + 1});
                if (outOfOrder(cur.data, cur.next.data, byMarks)) {
                    Student tmp = cur.data;     // swap the records
                    cur.data = cur.next.data;
                    cur.next.data = tmp;
                    swapped = true;
                    if (steps != null) steps.add(new int[]{1, i, i + 1});
                }
                cur = cur.next;
                i++;
            }
        } while (swapped);                      // stop when a full pass has no swaps
    }

    private boolean outOfOrder(Student a, Student b, boolean byMarks) {
        if (byMarks) return a.getMarks() < b.getMarks();
        return compareRegister(a.getRegisterNumber(), b.getRegisterNumber()) > 0;
    }

    /** Numbers compare as numbers (99 < 101); otherwise compare as text. */
    private int compareRegister(String a, String b) {
        try {
            return Long.compare(Long.parseLong(a), Long.parseLong(b));
        } catch (NumberFormatException e) {
            return a.compareToIgnoreCase(b);
        }
    }

    // ---------------- helpers ----------------
    public int getSize() { return size; }

    public List<Student> getAll() {
        List<Student> list = new ArrayList<Student>();
        for (Node cur = head; cur != null; cur = cur.next) list.add(cur.data);
        return list;
    }
}
