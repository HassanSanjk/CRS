package ui;


import model.Course;
import model.Student;
import repository.CourseRepository;
import repository.StudentRepository;
import services.EligibilityService;
import services.FileService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

/**
 * StudentCourseManagementPanel
 * - View students and courses
 * - Check eligibility (CGPA >= 2.0 AND failed courses <= 3)
 * - Allow registration/enrolment once eligible
 */
public class StudentCourseManagementPanel extends JPanel {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EligibilityService eligibilityService;
    private final FileService fileService;

    private java.util.List<Student> studentList;
    private java.util.List<Course> courseList;

    private JTable studentTable;
    private JTable courseTable;

    private JTextField txtSelectedStudent;
    private JTextField txtCgpa;
    private JTextField txtFailedCourses;

    private JButton btnCheckEligibility;
    private JButton btnRegister;                // NEW: enrolment
    private JButton btnShowInvalidWeights;
    private JButton btnShowNotEligible;         // NEW: list not eligible students

    // Track last eligibility result so we can enforce registration rule
    private boolean lastEligibilityEligible = false;

    public StudentCourseManagementPanel() {
        this.studentRepository = new StudentRepository();
        this.courseRepository = new CourseRepository();
        this.eligibilityService = new EligibilityService();
        this.fileService = new FileService();

        initData();
        initComponents();
        initLayout();
        initListeners();
    }

    // ---------- Data Loading ----------

    private void initData() {
        // Repository is cached in our refactor
        studentList = studentRepository.getAllStudents();
        courseList = courseRepository.getAllCourses();
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

        txtCgpa = new JTextField();
        txtFailedCourses = new JTextField();

        btnCheckEligibility = new JButton("Check Eligibility");
        btnRegister = new JButton("Register / Enrol");          // NEW
        btnRegister.setEnabled(false);                          // only after eligible
        btnShowInvalidWeights = new JButton("Show Invalid Course Weights");
        btnShowNotEligible = new JButton("Show Not Eligible Students"); // NEW
    }

    private DefaultTableModel createStudentTableModel() {
        String[] columns = {
                "Student ID", "First Name", "Last Name",
                "Major", "Year", "Email", "CGPA", "Failed Courses"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Student s : studentList) {
            Object[] row = {
                    s.getStudentId(),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getMajor(),
                    s.getYear(),
                    s.getEmail(),
                    s.getCgpa(),
                    s.getFailedCourses()
            };
            model.addRow(row);
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
            Object[] row = {
                    c.getCourseId(),
                    c.getCourseName(),
                    c.getCredits(),
                    c.getSemester(),
                    c.getInstructor(),
                    c.getExamWeight(),
                    c.getAssignmentWeight(),
                    c.getTotalWeight(),
                    c.isWeightValid() ? "YES" : "NO"
            };
            model.addRow(row);
        }

        return model;
    }

    // ---------- Layout ----------

    private void initLayout() {
        // Left side: Students
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));
        leftPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Right side: Courses + Eligibility
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        JPanel coursesPanel = new JPanel(new BorderLayout(5, 5));
        coursesPanel.setBorder(BorderFactory.createTitledBorder("Courses"));
        coursesPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel courseButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        courseButtons.add(btnShowInvalidWeights);
        coursesPanel.add(courseButtons, BorderLayout.SOUTH);

        JPanel eligibilityPanel = createEligibilityPanel();

        rightPanel.add(coursesPanel, BorderLayout.CENTER);
        rightPanel.add(eligibilityPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createEligibilityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Eligibility Check & Enrolment"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Selected student
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Student:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtSelectedStudent, gbc);
        row++;

        // CGPA
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("CGPA:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtCgpa, gbc);
        row++;

        // Failed courses
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Failed Courses:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtFailedCourses, gbc);
        row++;

        // Buttons row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnCheckEligibility);
        buttons.add(btnRegister);
        buttons.add(btnShowNotEligible);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(buttons, gbc);

        return panel;
    }

    // ---------- Listeners & Actions ----------

    private void initListeners() {
        studentTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                onStudentSelected();
            }
        });

        btnCheckEligibility.addActionListener(e -> onCheckEligibility());
        btnRegister.addActionListener(e -> onRegister());
        btnShowInvalidWeights.addActionListener(e -> onShowInvalidWeights());
        btnShowNotEligible.addActionListener(e -> onShowNotEligibleStudents());
    }

    private void onStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= studentList.size()) {
            txtSelectedStudent.setText("");
            txtCgpa.setText("");
            txtFailedCourses.setText("");
            lastEligibilityEligible = false;
            btnRegister.setEnabled(false);
            return;
        }

        Student s = studentList.get(row);
        txtSelectedStudent.setText(s.toShortString());
        txtCgpa.setText(String.valueOf(s.getCgpa()));
        txtFailedCourses.setText(String.valueOf(s.getFailedCourses()));

        // New selection => must re-check eligibility before registration
        lastEligibilityEligible = false;
        btnRegister.setEnabled(false);
    }

    private void onCheckEligibility() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= studentList.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table first.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = studentList.get(row);

        Double cgpa = parseDouble(txtCgpa.getText(), "CGPA", "2.75");
        if (cgpa == null) return;

        Integer failedCourses = parseInt(txtFailedCourses.getText(), "Failed Courses", "2");
        if (failedCourses == null) return;

        // Update student object
        s.setCgpa(cgpa);
        s.setFailedCourses(failedCourses);

        // Update table view
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        model.setValueAt(s.getCgpa(), row, 6);
        model.setValueAt(s.getFailedCourses(), row, 7);

        // Check eligibility
        EligibilityService.EligibilityResult result = eligibilityService.checkEligibility(s);
        lastEligibilityEligible = result.isEligible();
        btnRegister.setEnabled(lastEligibilityEligible);

        JOptionPane.showMessageDialog(this,
                result.getMessage() + (result.isEligible() ? "\n\nYou may now register/enrol." : ""),
                "Eligibility Result",
                result.isEligible() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Enrolment requirement:
     * Only allow registration if eligibility is confirmed true.
     */
    private void onRegister() {
        int studentRow = studentTable.getSelectedRow();
        int courseRow = courseTable.getSelectedRow();

        if (studentRow < 0 || studentRow >= studentList.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student first.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (courseRow < 0 || courseRow >= courseList.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to register/enrol.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!lastEligibilityEligible) {
            JOptionPane.showMessageDialog(this,
                    "Registration is not allowed until eligibility is confirmed.\n\nClick 'Check Eligibility' first.",
                    "Not Eligible Yet",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = studentList.get(studentRow);
        Course course = courseList.get(courseRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Register " + student.getFullName() + " (" + student.getStudentId() + ")\n" +
                        "for course: " + course.getCourseId() + " - " + course.getCourseName() + "?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Save as simple CSV line (beginner-friendly)
        // Format: date,studentId,courseId
        String line = LocalDate.now() + "," + student.getStudentId() + "," + course.getCourseId();
        boolean saved = fileService.writeTextFile(line, "enrollments.csv", true);

        if (saved) {
            JOptionPane.showMessageDialog(this,
                    "Registration saved!\n\n(enrollments.csv updated)",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save registration.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowInvalidWeights() {
        java.util.List<Course> invalid = courseRepository.getCoursesWithInvalidWeights();

        if (invalid.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All courses have a valid total weight of 100%.",
                    "Course Weight Check",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Courses with invalid total weights (≠ 100%):\n\n");
            for (Course c : invalid) {
                sb.append(String.format("%s - %s (Exam: %d, Assignment: %d, Total: %d)\n",
                        c.getCourseId(), c.getCourseName(),
                        c.getExamWeight(), c.getAssignmentWeight(), c.getTotalWeight()));
            }

            JOptionPane.showMessageDialog(this,
                    sb.toString(),
                    "Invalid Course Weights",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onShowNotEligibleStudents() {
        java.util.List<Student> notEligible = eligibilityService.getNotEligibleStudents(studentList);

        if (notEligible.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All students are eligible based on current CGPA/failed course values.",
                    "Not Eligible List",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Students NOT eligible to progress:\n\n");
        for (Student s : notEligible) {
            sb.append(s.getStudentId())
              .append(" - ")
              .append(s.getFullName())
              .append(" | CGPA: ")
              .append(String.format("%.2f", s.getCgpa()))
              .append(" | Failed: ")
              .append(s.getFailedCourses())
              .append("\n");
        }

        JOptionPane.showMessageDialog(this,
                sb.toString(),
                "Not Eligible Students",
                JOptionPane.WARNING_MESSAGE);
    }

    // ---------- Small input helpers (beginner-friendly) ----------

    private Double parseDouble(String text, String fieldName, String example) {
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter " + fieldName + ". (Example: " + example + ")",
                    "Missing " + fieldName,
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid " + fieldName + ". Please enter a number. (Example: " + example + ")",
                    "Invalid " + fieldName,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private Integer parseInt(String text, String fieldName, String example) {
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter " + fieldName + ". (Example: " + example + ")",
                    "Missing " + fieldName,
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid " + fieldName + ". Please enter an integer. (Example: " + example + ")",
                    "Invalid " + fieldName,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
