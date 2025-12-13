package ui;

import model.Course;
import model.Student;
import repository.CourseRepository;
import repository.StudentRepository;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * StudentCourseManagementPanel (CLEAN VERSION)
 * - View students and courses
 * - Show invalid course weights
 *
 * NOTE:
 * Eligibility + enrolment is handled in EligibilityPanel (friend's module),
 * not here.
 */
public class StudentCourseManagementPanel extends JPanel {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private List<Student> studentList;
    private List<Course> courseList;

    private JTable studentTable;
    private JTable courseTable;

    private JTextField txtSelectedStudent;

    private JButton btnShowInvalidWeights;

    public StudentCourseManagementPanel() {
        this.studentRepository = new StudentRepository();
        this.courseRepository = new CourseRepository();

        initData();
        initComponents();
        initLayout();
        initListeners();
    }

    // ---------- Data Loading ----------

    private void initData() {
        studentList = studentRepository.loadAllStudents();
        courseList = courseRepository.loadAllCourses();
    }

    // ---------- UI Components ----------

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        studentTable = new JTable(createStudentTableModel());
        courseTable = new JTable(createCourseTableModel());

        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtSelectedStudent = new JTextField();
        txtSelectedStudent.setEditable(false);

        btnShowInvalidWeights = new JButton("Show Invalid Course Weights");
    }

    private DefaultTableModel createStudentTableModel() {
        // IMPORTANT: CSV doesn't store CGPA/failedCourses -> keep this panel clean.
        String[] columns = {"Student ID", "First Name", "Last Name", "Major", "Year", "Email"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Student s : studentList) {
            model.addRow(new Object[]{
                    s.getStudentId(),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getMajor(),
                    s.getYear(),
                    s.getEmail()
            });
        }

        return model;
    }

    private DefaultTableModel createCourseTableModel() {
        String[] columns = {
                "Course ID", "Course Name", "Credits",
                "Semester", "Instructor",
                "Exam Weight", "Assignment Weight",
                "Total Weight", "Is Valid (100%)"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Course c : courseList) {
            model.addRow(new Object[]{
                    c.getCourseId(),
                    c.getCourseName(),
                    c.getCredits(),
                    c.getSemester(),
                    c.getInstructor(),
                    c.getExamWeight(),
                    c.getAssignmentWeight(),
                    c.getTotalWeight(),
                    c.isWeightValid() ? "YES" : "NO"
            });
        }

        return model;
    }

    // ---------- Layout ----------

    private void initLayout() {
        // Left side: Students
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));
        leftPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel selectedPanel = new JPanel(new BorderLayout(5, 5));
        selectedPanel.setBorder(BorderFactory.createTitledBorder("Selected Student"));
        selectedPanel.add(txtSelectedStudent, BorderLayout.CENTER);
        leftPanel.add(selectedPanel, BorderLayout.SOUTH);

        // Right side: Courses + weight check
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

    // ---------- Listeners ----------

    private void initListeners() {
        studentTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                onStudentSelected();
            }
        });

        btnShowInvalidWeights.addActionListener(e -> onShowInvalidWeights());
    }

    private void onStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= studentList.size()) {
            txtSelectedStudent.setText("");
            return;
        }

        Student s = studentList.get(row);
        txtSelectedStudent.setText(s.toShortString());
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
        sb.append("Courses with invalid total weights (≠ 100%):\n\n");

        for (Course c : invalid) {
            sb.append(String.format("%s - %s (Exam: %d, Assignment: %d, Total: %d)\n",
                    c.getCourseId(),
                    c.getCourseName(),
                    c.getExamWeight(),
                    c.getAssignmentWeight(),
                    c.getTotalWeight()));
        }

        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "Invalid Course Weights",
                JOptionPane.WARNING_MESSAGE);
    }
}
