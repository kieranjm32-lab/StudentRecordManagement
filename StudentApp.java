import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentApp - the Swing GUI for the Student Record Management System.
 * Clean, modest interface using small concepts: singly linked list visualizer,
 * monochrome/grayscale palette, and simple structured components.
 * Run with:  java StudentApp
 */
public class StudentApp extends JFrame {

    // ---------- modest monochrome palette (colour removed) ----------
    static final Color BG          = new Color(0xF6F6F6); // neutral light background
    static final Color SURFACE     = Color.WHITE;         // card/panel surface
    static final Color SURFACE_ALT = new Color(0xEEEEEE); // light neutral gray fill
    static final Color BORDER      = new Color(0xD0D0D0); // neutral soft border
    static final Color TEXT_MAIN   = new Color(0x222222); // dark charcoal primary text
    static final Color TEXT_MUTED  = new Color(0x666666); // neutral medium gray
    static final Color HL_CHECK    = new Color(0xDDDDDD); // light gray for comparing / inspecting
    static final Color HL_SWAP     = new Color(0xAAAAAA); // medium gray for swapping
    static final Color HL_MATCH    = new Color(0x2A2A2A); // dark charcoal inverted for match / found

    // Aliases for compatibility
    static final Color NAVY   = TEXT_MAIN;
    static final Color BLUE   = HL_CHECK;
    static final Color TINT   = SURFACE_ALT;
    static final Color GREEN  = HL_MATCH;
    static final Color ORANGE = HL_SWAP;
    static final Color RED    = TEXT_MAIN;
    static final Color MUTED  = TEXT_MUTED;

    // ---------- model ----------
    private final StudentRecordManager manager = new StudentRecordManager();
    private final FileHandler files = new FileHandler("students.txt");

    // ---------- widgets (package-private so a test can drive them) ----------
    JTextField fReg, fName, fAge, fMarks, fCourse, fCode, fPos, fSearch;
    JLabel formTitle, errLabel, statusLabel, lblTotal, lblAvg, lblTop;
    JButton btnSubmit, btnCancel, btnEdit, btnDelete, btnSearch, btnSortReg, btnSortMarks, btnClear;
    JTable table;
    DefaultTableModel model;
    ChainPanel chain;

    private String editingReg = null;
    boolean busy = false;
    private javax.swing.Timer anim;
    private javax.swing.Timer clearTimer;

    public StudentApp() {
        super("Student Record Management System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                files.save(manager.getAll());
                dispose();
                System.exit(0);
            }
        });

        loadData();

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(10, 12, 10, 12));
        root.setBackground(BG);
        root.add(buildTop(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 0));
        center.setOpaque(false);
        JScrollPane formScroll = new JScrollPane(buildForm(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setPreferredSize(new Dimension(320, 100));
        center.add(formScroll, BorderLayout.WEST);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
        getRootPane().setDefaultButton(btnSubmit);
        setSize(1100, 700);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);
        refreshAll();
        say("Ready. Add a student, search by register number, or sort the list.", TEXT_MAIN);
    }

    // =====================================================================
    //  Data
    // =====================================================================
    private void loadData() {
        File sf = new File("students.txt");
        boolean firstRun = !sf.exists() || sf.length() == 0;
        for (Student s : files.load()) manager.addStudent(s);
        if (firstRun && manager.getSize() == 0) {          // sample records for the first run
            manager.addStudent(new Student("Ananya Nair", "101", 20, new Course("CSE", "C101"), 91.5));
            manager.addStudent(new Student("Rahul Verma", "102", 21, new Course("ME", "M201"), 78));
            manager.addStudent(new Student("Priya Singh", "103", 19, new Course("ECE", "E301"), 85));
            manager.addStudent(new Student("Arjun Menon", "104", 20, new Course("CSE", "C101"), 88));
            files.save(manager.getAll());
        }
    }

    private void save() { files.save(manager.getAll()); }

    // =====================================================================
    //  UI construction
    // =====================================================================
    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        // header band
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(10, 16, 10, 16)));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel t = new JLabel("Student Record Management System");
        t.setForeground(TEXT_MAIN);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 17f));
        JLabel sub = new JLabel("Team 13  ·  Singly Linked List Prototype");
        sub.setForeground(TEXT_MUTED);
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 12f));
        titles.add(t);
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        stats.setOpaque(false);
        lblTotal = statLabel();
        lblAvg = statLabel();
        lblTop = statLabel();
        stats.add(lblTotal);
        stats.add(lblAvg);
        stats.add(lblTop);
        header.add(stats, BorderLayout.EAST);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(Box.createVerticalStrut(10));

        // linked list card
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        JLabel ct = new JLabel("Singly linked list   (HEAD → Node [Data | Next] → NULL)");
        ct.setFont(ct.getFont().deriveFont(Font.BOLD, 13f));
        ct.setForeground(TEXT_MAIN);
        card.add(ct, BorderLayout.NORTH);

        chain = new ChainPanel();
        JScrollPane sp = new JScrollPane(chain, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(100, 96));
        card.add(sp, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(5, 8, 5, 8)));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(SURFACE_ALT);
        statusLabel.setForeground(TEXT_MAIN);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        card.add(statusLabel, BorderLayout.SOUTH);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(card);
        return top;
    }

    private JLabel statLabel() {
        JLabel l = new JLabel();
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(12, 14, 12, 14)));
        p.setMinimumSize(new Dimension(300, 100));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        int r = 0;

        formTitle = new JLabel("Add Student");
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 15f));
        formTitle.setForeground(TEXT_MAIN);
        c.gridy = r++;
        c.insets = new Insets(0, 0, 8, 0);
        p.add(formTitle, c);

        fReg = new JTextField();
        fName = new JTextField();
        fAge = new JTextField();
        fMarks = new JTextField();
        fCourse = new JTextField();
        fCode = new JTextField();
        fPos = new JTextField();
        r = addField(p, c, r, "Register number", fReg);
        r = addField(p, c, r, "Name", fName);
        r = addPair(p, c, r, "Age", fAge, "Marks (0-100)", fMarks);
        r = addPair(p, c, r, "Course (e.g. CSE)", fCourse, "Course code", fCode);
        r = addField(p, c, r, "Insert at position (optional)", fPos);
        fPos.setToolTipText("Leave empty to add at the end of the list");

        errLabel = new JLabel(" ");
        errLabel.setForeground(TEXT_MAIN);
        errLabel.setFont(errLabel.getFont().deriveFont(Font.BOLD, 12f));
        c.gridy = r++;
        c.insets = new Insets(6, 0, 4, 0);
        p.add(errLabel, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        btnSubmit = new JButton("Add Student");
        btnCancel = new JButton("Cancel");
        btnCancel.setVisible(false);
        btnSubmit.setFocusPainted(false);
        btnCancel.setFocusPainted(false);
        btns.add(btnSubmit);
        btns.add(btnCancel);
        c.gridy = r++;
        c.insets = new Insets(4, -5, 0, 0);
        p.add(btns, c);

        c.gridy = r;
        c.weighty = 1;
        p.add(Box.createVerticalGlue(), c);

        btnSubmit.addActionListener(e -> onSubmit());
        btnCancel.addActionListener(e -> { resetForm(); say("Update cancelled.", TEXT_MAIN); });
        return p;
    }

    /** Two small fields side by side in one row. */
    private int addPair(JPanel p, GridBagConstraints c, int r, String l1, JTextField f1, String l2, JTextField f2) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.add(miniField(l1, f1));
        row.add(miniField(l2, f2));
        c.gridy = r++;
        c.insets = new Insets(4, 0, 2, 0);
        p.add(row, c);
        return r;
    }

    private JPanel miniField(String label, JTextField f) {
        JPanel m = new JPanel(new BorderLayout(0, 2));
        m.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setForeground(TEXT_MUTED);
        m.add(l, BorderLayout.NORTH);
        m.add(f, BorderLayout.CENTER);
        return m;
    }

    private int addField(JPanel p, GridBagConstraints c, int r, String label, JTextField f) {
        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setForeground(TEXT_MUTED);
        c.gridy = r++;
        c.insets = new Insets(4, 0, 2, 0);
        p.add(l, c);
        c.gridy = r++;
        c.insets = new Insets(0, 0, 2, 0);
        p.add(f, c);
        return r;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), new EmptyBorder(10, 12, 10, 12)));

        // toolbar: search + sort
        JPanel tools = new JPanel(new BorderLayout(8, 0));
        tools.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JLabel lblSearch = new JLabel("Register no.:");
        lblSearch.setFont(lblSearch.getFont().deriveFont(Font.PLAIN, 12f));
        lblSearch.setForeground(TEXT_MUTED);
        left.add(lblSearch);
        fSearch = new JTextField(10);
        btnSearch = new JButton("Search");
        btnSearch.setFocusPainted(false);
        left.add(fSearch);
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JLabel lblSort = new JLabel("Sort by:");
        lblSort.setFont(lblSort.getFont().deriveFont(Font.PLAIN, 12f));
        lblSort.setForeground(TEXT_MUTED);
        right.add(lblSort);
        btnSortReg = new JButton("Register no.");
        btnSortMarks = new JButton("Marks");
        btnSortReg.setFocusPainted(false);
        btnSortMarks.setFocusPainted(false);
        right.add(btnSortReg);
        right.add(btnSortMarks);
        tools.add(left, BorderLayout.WEST);
        tools.add(right, BorderLayout.EAST);
        p.add(tools, BorderLayout.NORTH);

        // table
        model = new DefaultTableModel(new Object[]{"No.", "Register No.", "Name", "Age", "Course", "Marks"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xEAEAEA));
        table.setSelectionBackground(new Color(0xE0E0E0));
        table.setSelectionForeground(TEXT_MAIN);
        table.setForeground(TEXT_MAIN);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer right2 = new DefaultTableCellRenderer();
        right2.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(right2);
        table.getColumnModel().getColumn(3).setCellRenderer(right2);
        table.getColumnModel().getColumn(5).setCellRenderer(right2);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(5).setMaxWidth(90);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) onEdit(); }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(SURFACE);
        sp.setBorder(new LineBorder(BORDER));
        p.add(sp, BorderLayout.CENTER);

        // bottom buttons
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel bl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bl.setOpaque(false);
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Selected");
        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);
        bl.add(btnEdit);
        bl.add(btnDelete);
        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        br.setOpaque(false);
        btnClear = new JButton("Clear All");
        btnClear.setFocusPainted(false);
        br.add(btnClear);
        bottom.add(bl, BorderLayout.WEST);
        bottom.add(br, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> doSearch());
        fSearch.addActionListener(e -> doSearch());
        btnSortReg.addActionListener(e -> doSort(false));
        btnSortMarks.addActionListener(e -> doSort(true));
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> onClear());
        return p;
    }

    // =====================================================================
    //  Refresh helpers
    // =====================================================================
    private void refreshAll() {
        List<Student> list = manager.getAll();
        model.setRowCount(0);
        int i = 1;
        double sum = 0;
        Student top = null;
        for (Student s : list) {
            model.addRow(new Object[]{i++, s.getRegisterNumber(), s.getName(), s.getAge(),
                    s.getCourse().getCourseName() + " (" + s.getCourse().getCourseCode() + ")",
                    String.format("%.1f", s.getMarks())});
            sum += s.getMarks();
            if (top == null || s.getMarks() > top.getMarks()) top = s;
        }
        chain.setStudents(list);
        chain.scrollRectToVisible(new Rectangle(0, 0, 10, 10));      // back to HEAD
        setStat(lblTotal, String.valueOf(list.size()), "Students");
        setStat(lblAvg, list.isEmpty() ? "-" : String.format("%.1f", sum / list.size()), "Average marks");
        setStat(lblTop, top == null ? "-" : top.getName().split(" ")[0], "Top scorer");
    }

    private void setStat(JLabel l, String value, String caption) {
        l.setText("<html><span style='color:#666666;font-size:11px'>" + caption + ": </span>"
                + "<b style='color:#222222;font-size:12px'>" + escape(value) + "</b></html>");
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void say(String msg, Color color) {
        statusLabel.setForeground(TEXT_MAIN);
        statusLabel.setText(msg);
    }

    private void setBusy(boolean b) {
        busy = b;
        JButton[] all = {btnSubmit, btnCancel, btnEdit, btnDelete, btnSearch, btnSortReg, btnSortMarks, btnClear};
        for (JButton x : all) x.setEnabled(!b);
    }

    private void resetForm() {
        fReg.setText("");
        fName.setText("");
        fAge.setText("");
        fMarks.setText("");
        fCourse.setText("");
        fCode.setText("");
        fPos.setText("");
        fReg.setEditable(true);
        fPos.setEnabled(true);
        editingReg = null;
        formTitle.setText("Add Student");
        btnSubmit.setText("Add Student");
        btnCancel.setVisible(false);
        errLabel.setText(" ");
    }

    private void flash(final int index) {          // briefly highlight one node
        if (clearTimer != null) clearTimer.stop();
        chain.clearHighlights();
        chain.highlight(index, HL_MATCH);
        clearTimer = new javax.swing.Timer(2500, e -> chain.clearHighlights());
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    // =====================================================================
    //  Operations
    // =====================================================================
    /** Add Student, or save changes when editing. */
    private void onSubmit() {
        if (busy) return;
        errLabel.setText(" ");
        try {
            String name = fName.getText();
            int age = parseInt(fAge.getText(), "Age must be a whole number.");
            double marks = parseDouble(fMarks.getText(), "Marks must be a number.");
            Course course = new Course(fCourse.getText(), fCode.getText());

            if (editingReg != null) {                                  // ---- update
                Student check = new Student(name, editingReg, age, course, marks);   // validates everything first
                manager.updateStudent(editingReg, check.getName(), check.getAge(), check.getCourse(), check.getMarks());
                save();
                refreshAll();
                say("Updated " + check.getName() + " (" + editingReg + ").", HL_MATCH);
                resetForm();
                return;
            }

            Student s = new Student(name, fReg.getText(), age, course, marks);   // ---- add
            if (manager.searchStudent(s.getRegisterNumber()) != null) {
                throw new IllegalArgumentException("Register number " + s.getRegisterNumber() + " already exists.");
            }
            String posText = fPos.getText().trim();
            int where;
            if (posText.isEmpty()) {
                manager.addStudent(s);
                where = manager.getSize();
                say("Added " + s.getName() + " at position " + where + " (end of list).", HL_MATCH);
            } else {
                int pos = parseInt(posText, "Position must be a whole number.");
                if (!manager.insertAt(pos, s)) {
                    throw new IllegalArgumentException("Position must be from 1 to " + (manager.getSize() + 1) + ".");
                }
                where = pos;
                say("Inserted " + s.getName() + " at position " + pos + ". Only two links changed.", HL_MATCH);
            }
            save();
            refreshAll();
            flash(where - 1);
            table.setRowSelectionInterval(where - 1, where - 1);
            resetForm();
            fReg.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            errLabel.setText("Note: " + ex.getMessage());
        }
    }

    private void onEdit() {
        if (busy) return;
        int row = table.getSelectedRow();
        if (row < 0) { say("Notice: Select a student in the table first.", TEXT_MAIN); return; }
        Student s = manager.searchStudent((String) model.getValueAt(row, 1));
        if (s == null) return;
        editingReg = s.getRegisterNumber();
        fReg.setText(s.getRegisterNumber());
        fReg.setEditable(false);
        fName.setText(s.getName());
        fAge.setText(String.valueOf(s.getAge()));
        fMarks.setText(String.valueOf(s.getMarks()));
        fCourse.setText(s.getCourse().getCourseName());
        fCode.setText(s.getCourse().getCourseCode());
        fPos.setText("");
        fPos.setEnabled(false);
        formTitle.setText("Update Student");
        btnSubmit.setText("Save Changes");
        btnCancel.setVisible(true);
        errLabel.setText(" ");
        say("Editing " + s.getName() + ". Change any field and click Save Changes.", TEXT_MAIN);
        fName.requestFocusInWindow();
    }

    private void onDelete() {
        if (busy) return;
        int row = table.getSelectedRow();
        if (row < 0) { say("Notice: Select a student in the table first.", TEXT_MAIN); return; }
        String reg = (String) model.getValueAt(row, 1);
        String name = (String) model.getValueAt(row, 2);
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + name + " (" + reg + ")?", "Delete student",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        manager.deleteStudent(reg);
        save();
        if (editingReg != null && editingReg.equalsIgnoreCase(reg)) resetForm();
        refreshAll();
        say("Deleted " + name + ". The neighbouring nodes were linked together.", HL_MATCH);
    }

    private void onClear() {
        if (busy || manager.getSize() == 0) return;
        int ok = JOptionPane.showConfirmDialog(this, "Delete ALL records?", "Clear all",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        for (Student s : manager.getAll()) manager.deleteStudent(s.getRegisterNumber());
        save();
        resetForm();
        refreshAll();
        say("All records cleared.", HL_MATCH);
    }

    /** Linear search, shown one node at a time. */
    private void doSearch() {
        if (busy) return;
        final String reg = fSearch.getText().trim();
        if (reg.isEmpty()) { say("Notice: Type a register number to search.", TEXT_MAIN); return; }
        final List<Student> list = manager.getAll();
        if (list.isEmpty()) { say("Not found: the list is empty.", TEXT_MAIN); return; }
        if (clearTimer != null) clearTimer.stop();
        chain.clearHighlights();
        setBusy(true);
        final int[] k = {0};
        anim = new javax.swing.Timer(list.size() > 12 ? 90 : 330, null);
        anim.setInitialDelay(0);
        anim.addActionListener(e -> {
            int i = k[0];
            if (i >= list.size()) {
                anim.stop();
                chain.clearHighlights();
                setBusy(false);
                say("Not found: no student with register number " + reg + ". Checked all " + list.size() + " nodes.", TEXT_MAIN);
                return;
            }
            chain.clearHighlights();
            chain.scrollTo(i);
            Student s = list.get(i);
            if (s.getRegisterNumber().equalsIgnoreCase(reg)) {
                anim.stop();
                chain.highlight(i, HL_MATCH);
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                setBusy(false);
                say("Found: " + s.getName() + ", " + s.getCourse().getCourseName() + ", marks " + s.getMarks()
                        + ". Checked " + (i + 1) + " node" + (i == 0 ? "" : "s") + ".", HL_MATCH);
                clearTimer = new javax.swing.Timer(4000, ev -> chain.clearHighlights());
                clearTimer.setRepeats(false);
                clearTimer.start();
                return;
            }
            chain.highlight(i, HL_CHECK);
            say("Linear search: checking node " + (i + 1) + " of " + list.size() + " (" + s.getRegisterNumber() + ")...", HL_CHECK);
            k[0]++;
        });
        anim.start();
    }

    /** Bubble sort: the manager sorts for real, then we replay its recorded steps. */
    private void doSort(final boolean byMarks) {
        if (busy) return;
        if (manager.getSize() < 2) { say("Notice: Add at least two students to sort.", TEXT_MAIN); return; }
        if (clearTimer != null) clearTimer.stop();
        chain.clearHighlights();
        final List<Student> snap = new ArrayList<Student>(manager.getAll());       // order before sorting
        final List<int[]> steps = manager.sortStudentsRecorded(byMarks);
        setBusy(true);
        chain.setStudents(snap);
        final int[] pos = {0};
        final int[] counts = {0, 0, 0};            // passes, compares, swaps
        anim = new javax.swing.Timer(snap.size() > 10 ? 60 : 240, null);
        anim.setInitialDelay(0);
        anim.addActionListener(e -> {
            if (pos[0] >= steps.size()) {
                anim.stop();
                chain.clearHighlights();
                save();
                refreshAll();
                setBusy(false);
                say("Sorted by " + (byMarks ? "marks (highest first)" : "register number") + ": " + counts[0] + " passes, "
                        + counts[1] + " comparisons, " + counts[2] + " swaps.", HL_MATCH);
                return;
            }
            int[] st = steps.get(pos[0]++);
            chain.clearHighlights();
            if (st[0] == 2) {
                counts[0]++;
            } else if (st[0] == 0) {
                counts[1]++;
                chain.highlight(st[1], HL_CHECK);
                chain.highlight(st[2], HL_CHECK);
                chain.scrollTo(st[1]);
                say("Bubble sort (pass " + counts[0] + "): comparing positions " + (st[1] + 1) + " and " + (st[2] + 1) + ".", HL_CHECK);
            } else {
                counts[2]++;
                Student t = snap.get(st[1]);
                snap.set(st[1], snap.get(st[2]));
                snap.set(st[2], t);
                chain.setStudents(snap);
                chain.highlight(st[1], HL_SWAP);
                chain.highlight(st[2], HL_SWAP);
                chain.scrollTo(st[1]);
                say("Pass " + counts[0] + ": swapped positions " + (st[1] + 1) + " and " + (st[2] + 1) + ".", HL_SWAP);
            }
        });
        anim.start();
    }

    // =====================================================================
    //  Input parsing
    // =====================================================================
    private int parseInt(String text, String message) {
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }

    private double parseDouble(String text, String message) {
        try { return Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }

    // =====================================================================
    //  The linked-list picture (small concepts: Node, Data, Next, HEAD, NULL)
    // =====================================================================
    static class ChainPanel extends JPanel {
        private static final int HEAD_W = 68, NODE_W = 150, DATA_W = 108, ARROW = 32, NULL_W = 60, H = 48;
        private List<Student> list = new ArrayList<Student>();
        private final Map<Integer, Color> hl = new HashMap<Integer, Color>();

        ChainPanel() { setBackground(SURFACE); }

        void setStudents(List<Student> l) {
            list = new ArrayList<Student>(l);
            revalidate();
            repaint();
        }

        void highlight(int index, Color c) { hl.put(index, c); repaint(); }
        void clearHighlights() { hl.clear(); repaint(); }

        void scrollTo(int index) {
            int x = 14 + HEAD_W + index * (ARROW + NODE_W);
            scrollRectToVisible(new Rectangle(Math.max(0, x - 40), 0, NODE_W + ARROW + 120, 10));
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(14 + HEAD_W + list.size() * (ARROW + NODE_W) + ARROW + NULL_W + 14, 84);
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font bold = getFont().deriveFont(Font.BOLD, 13f);
            Font mono = new Font(Font.MONOSPACED, Font.BOLD, 13);
            Font small = getFont().deriveFont(Font.PLAIN, 11f);
            int y = 16, x = 14;

            // HEAD pointer
            g.setColor(SURFACE_ALT);
            g.fillRoundRect(x, y, HEAD_W, H, 6, 6);
            g.setColor(new Color(0x666666));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(x, y, HEAD_W, H, 6, 6);
            g.setColor(TEXT_MAIN);
            g.setFont(bold);
            center(g, "HEAD", x, y, HEAD_W, H);
            x += HEAD_W;

            for (int i = 0; i < list.size(); i++) {
                arrow(g, x, y + H / 2);
                x += ARROW;
                Color hc = hl.get(i);
                int yy = hc != null ? y - 3 : y;
                boolean isMatch = (hc == HL_MATCH || (hc != null && hc.getRed() < 60));
                Color nodeBg = isMatch ? HL_MATCH : (hc != null ? hc : SURFACE);
                Color border = isMatch ? new Color(0x111111) : (hc != null ? new Color(0x444444) : new Color(0x777777));

                // node body
                g.setColor(nodeBg);
                g.fillRoundRect(x, yy, NODE_W, H, 6, 6);

                // 'next' pointer compartment
                g.setColor(isMatch ? new Color(0x3E3E3E) : SURFACE_ALT);
                g.fillRect(x + DATA_W, yy + 1, NODE_W - DATA_W - 1, H - 2);

                // node outline
                g.setColor(border);
                g.setStroke(new BasicStroke(isMatch || hc != null ? 2f : 1.2f));
                g.drawRoundRect(x, yy, NODE_W, H, 6, 6);

                // compartment divider line
                g.setStroke(new BasicStroke(1f));
                g.drawLine(x + DATA_W, yy + 1, x + DATA_W, yy + H - 1);

                // student data: register number and name
                Student s = list.get(i);
                g.setColor(isMatch ? Color.WHITE : TEXT_MAIN);
                g.setFont(mono);
                g.drawString(s.getRegisterNumber(), x + 8, yy + 20);

                g.setColor(isMatch ? new Color(0xD0D0D0) : TEXT_MUTED);
                g.setFont(small);
                g.drawString(fit(g, s.getName(), DATA_W - 12), x + 8, yy + 36);

                // next pointer text
                g.setColor(isMatch ? Color.WHITE : TEXT_MUTED);
                g.setFont(small.deriveFont(Font.BOLD));
                center(g, "next", x + DATA_W, yy, NODE_W - DATA_W, H);
                x += NODE_W;
            }

            // NULL termination
            arrow(g, x, y + H / 2);
            x += ARROW;
            g.setColor(SURFACE_ALT);
            g.fillRoundRect(x, y, NULL_W, H, 6, 6);
            g.setColor(new Color(0x666666));
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(x, y, NULL_W, H, 6, 6);
            g.setColor(TEXT_MUTED);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            center(g, "NULL", x, y, NULL_W, H);
            g.dispose();
        }

        private void arrow(Graphics2D g, int x, int y) {
            g.setColor(new Color(0x555555));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x + 2, y, x + ARROW - 5, y);
            g.fillPolygon(new int[]{x + ARROW - 2, x + ARROW - 9, x + ARROW - 9},
                    new int[]{y, y - 4, y + 4}, 3);
        }

        private void center(Graphics2D g, String text, int x, int y, int w, int h) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
        }

        private String fit(Graphics2D g, String text, int width) {
            FontMetrics fm = g.getFontMetrics();
            if (fm.stringWidth(text) <= width) return text;
            while (text.length() > 1 && fm.stringWidth(text + "...") > width) text = text.substring(0, text.length() - 1);
            return text + "...";
        }
    }

    // =====================================================================
    //  main
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            Font f = new Font("SansSerif", Font.PLAIN, 13);
            for (Object key : java.util.Collections.list(UIManager.getDefaults().keys())) {
                if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource(f));
                }
            }
            new StudentApp().setVisible(true);
        });
    }
}
