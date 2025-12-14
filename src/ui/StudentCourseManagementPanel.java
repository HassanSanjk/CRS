package ui;

import model.Course;
import model.Student;
import repository.CourseRepository;
import repository.StudentRepository;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentCourseManagementPanel
 * Shows students + course info.
 */
public class StudentCourseManagementPanel extends JPanel {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private List<Student> studentList = new ArrayList<Student>();
    private List<Course> courseList = new ArrayList<Course>();

    private JTable studentTable;
    private JTable courseTable;

    private JTextField txtSelectedStudent;
    private JButton btnShowInvalidWeights;

    public StudentCourseManagementPanel() {
        studentRepository = new StudentRepository();
        courseRepository = new CourseRepository();

        loadData();
        initComponents();
        initLayout();
        initListeners();
    }

    // ---------- load data ----------

    private void loadData() {
        studentList = studentRepository.loadAllStudents();
        courseList = courseRepository.loadAllCourses();
    }

    // ---------- UI ----------

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        studentTable = new JTable(buildStudentModel());
        courseTable = new JTable(buildCourseModel());

        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtSelectedStudent = new JTextField();
        txtSelectedStudent.setEditable(false);

        btnShowInvalidWeights = new JButton("Show Invalid Course Weights");
    }

    private DefaultTableModel buildStudentModel() {
        String[] cols = {"Student ID", "First Name", "Last Name", "Major", "Year", "Email"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Student s : studentList) {
            model.addRow(new Object[]{
                    safe(s.getStudentId()),
                    safe(s.getFirstName()),
                    safe(s.getLastName()),
                    safe(s.getMajor()),
                    safe(s.getYear()),
                    safe(s.getEmail())
            });
        }

        return model;
    }

    private DefaultTableModel buildCourseModel() {
        String[] cols = {
                "Course ID", "Course Name", "Credits",
                "Semester", "Instructor",
                "Exam Weight", "Assignment Weight",
                "Total Weight", "Is Valid (100%)"
        };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Course c : courseList) {
            model.addRow(new Object[]{
                    safe(c.getCourseId()),
                    safe(c.getCourseName()),
                    c.getCredits(),
                    safe(c.getSemester()),
                    safe(c.getInstructor()),
                    c.getExamWeight(),
                    c.getAssignmentWeight(),
                    c.getTotalWeight(),
                    c.isWeightValid() ? "YES" : "NO"
            });
        }

        return model;
    }

    // ---------- layout ----------

    private void initLayout() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));
        leftPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel selectedPanel = new JPanel(new BorderLayout(5, 5));
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Selected Student"));
        selectedPanel.add(txtSelectedStudent, BorderLayout.CENTER);
        leftPanel.add(selectedPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Courses"));
        rightPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel courseButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        courseButtons.add(btnShowInvalidWeights);
        rightPanel.add(courseButtons, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.45);

        add(splitPane, BorderLayout.CENTER);
    }

    // ---------- listeners ----------

    private void initListeners() {
        studentTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onStudentSelected();
        });

        btnShowInvalidWeights.addActionListener(e -> onShowInvalidWeights());
    }

    private void onStudentSelected() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow < 0) {
            txtSelectedStudent.setText("");
            return;
        }

        // safer: read ID from table (in case table order changes later)
        String studentId = String.valueOf(studentTable.getValueAt(viewRow, 0));
        Student s = findStudentById(studentId);

        txtSelectedStudent.setText(s == null ? "" : s.toShortString());
    }

    private Student findStudentById(String studentId) {
        String target = safe(studentId).toLowerCase();

        for (Student s : studentList) {
            if (safe(s.getStudentId()).toLowerCase().equals(target)) {
                return s;
            }
        }
        return null;
    }

    private void onShowInvalidWeights() {
        List<Course> invalid = courseRepository.getCoursesWithInvalidWeights();

        if (invalid.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All courses have a valid total weight of 100%.",
                    "Course Weight Check",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Courses with invalid total weights (not 100%):\n\n");

        for (Course c : invalid) {
            sb.append(c.getCourseId()).append(" - ").append(safe(c.getCourseName()))
                    .append(" (Exam: ").append(c.getExamWeight())
                    .append(", Assignment: ").append(c.getAssignmentWeight())
                    .append(", Total: ").append(c.getTotalWeight())
                    .append(")\n");
        }

        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "Invalid Course Weights",
                JOptionPane.WARNING_MESSAGE);
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
