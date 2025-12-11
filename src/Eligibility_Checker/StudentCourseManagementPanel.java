// File: src/crs/studentcourse/StudentCourseManagementPanel.java
package Eligibility_Checker;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentCourseManagementPanel extends JPanel {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EligibilityChecker eligibilityChecker;

    private List<Student> studentList;
    private List<Course> courseList;

    private JTable studentTable;
    private JTable courseTable;

    private JTextField txtSelectedStudent;
    private JTextField txtCgpa;
    private JTextField txtFailedCourses;

    private JButton btnCheckEligibility;
    private JButton btnShowInvalidWeights;

    public StudentCourseManagementPanel() {
        this.studentRepository = new StudentRepository();
        this.courseRepository = new CourseRepository();
        this.eligibilityChecker = new EligibilityChecker();

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

        // Tables
        studentTable = new JTable(createStudentTableModel());
        courseTable = new JTable(createCourseTableModel());

        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Selection / input panel
        txtSelectedStudent = new JTextField();
        txtSelectedStudent.setEditable(false);

        txtCgpa = new JTextField();
        txtFailedCourses = new JTextField();

        btnCheckEligibility = new JButton("Check Eligibility");
        btnShowInvalidWeights = new JButton("Show Invalid Course Weights");
    }

    private DefaultTableModel createStudentTableModel() {
        String[] columns = {
                "Student ID", "First Name", "Last Name",
                "Major", "Year", "Email", "CGPA", "Failed Courses"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // All editing is done via right side fields, not directly in table
                return false;
            }
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
            @Override
            public boolean isCellEditable(int row, int column) {
                // This is read-only display
                return false;
            }
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
        // Left side: Students table
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));

        JScrollPane studentScroll = new JScrollPane(studentTable);
        leftPanel.add(studentScroll, BorderLayout.CENTER);

        // Right side: Courses table at top + eligibility panel at bottom
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        JPanel coursesPanel = new JPanel(new BorderLayout(5, 5));
        coursesPanel.setBorder(BorderFactory.createTitledBorder("Courses"));
        JScrollPane courseScroll = new JScrollPane(courseTable);
        coursesPanel.add(courseScroll, BorderLayout.CENTER);
        coursesPanel.add(btnShowInvalidWeights, BorderLayout.SOUTH);

        JPanel eligibilityPanel = createEligibilityPanel();

        rightPanel.add(coursesPanel, BorderLayout.CENTER);
        rightPanel.add(eligibilityPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setResizeWeight(0.5); // 50/50

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createEligibilityPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Eligibility Check"));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Selected student
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Selected Student:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        panel.add(txtSelectedStudent, gbc);

        row++;

        // CGPA
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("CGPA:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        panel.add(txtCgpa, gbc);

        row++;

        // Failed courses
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Failed Courses:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        panel.add(txtFailedCourses, gbc);

        row++;

        // Button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(btnCheckEligibility, gbc);

        return panel;
    }

    // ---------- Listeners & Actions ----------

    private void initListeners() {
        // When a student row is selected -> show in the right panel
        studentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    onStudentSelected();
                }
            }
        });

        // Eligibility button
        btnCheckEligibility.addActionListener(e -> onCheckEligibility());

        // Invalid weight button
        btnShowInvalidWeights.addActionListener(e -> onShowInvalidWeights());
    }

    private void onStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= studentList.size()) {
            txtSelectedStudent.setText("");
            txtCgpa.setText("");
            txtFailedCourses.setText("");
            return;
        }

        Student s = studentList.get(row);
        txtSelectedStudent.setText(s.toShortString());
        txtCgpa.setText(String.valueOf(s.getCgpa()));
        txtFailedCourses.setText(String.valueOf(s.getFailedCourses()));
    }

    private void onCheckEligibility() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= studentList.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student from the table first.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Student s = studentList.get(row);

        // Validate CGPA input
        double cgpa;
        try {
            if (txtCgpa.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter CGPA.",
                        "Missing CGPA",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            cgpa = Double.parseDouble(txtCgpa.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid CGPA. Please enter a numeric value (e.g. 2.75).",
                    "Invalid CGPA",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Validate failed courses input
        int failedCourses;
        try {
            if (txtFailedCourses.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter the number of failed courses.",
                        "Missing Failed Courses",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            failedCourses = Integer.parseInt(txtFailedCourses.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid number of failed courses. Please enter an integer (e.g. 2).",
                    "Invalid Failed Courses",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Update student object
        s.setCgpa(cgpa);
        s.setFailedCourses(failedCourses);

        // Update table view for CGPA & failed courses
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        model.setValueAt(s.getCgpa(), row, 6);         // CGPA column index
        model.setValueAt(s.getFailedCourses(), row, 7); // Failed courses column index

        // Run eligibility check
        EligibilityChecker.EligibilityResult result =
                eligibilityChecker.checkEligibility(s);

        JOptionPane.showMessageDialog(
                this,
                result.getMessage(),
                "Eligibility Result",
                result.isEligible()
                        ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.WARNING_MESSAGE
        );
    }

    private void onShowInvalidWeights() {
        List<Course> invalid = courseRepository.getCoursesWithInvalidWeights();

        if (invalid.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "All courses have a valid total weight of 100%.",
                    "Course Weight Check",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("The following courses have invalid total weights (≠ 100%):\n\n");
            for (Course c : invalid) {
                sb.append(String.format(
                        "%s - %s (Exam: %d, Assignment: %d, Total: %d)\n",
                        c.getCourseId(),
                        c.getCourseName(),
                        c.getExamWeight(),
                        c.getAssignmentWeight(),
                        c.getTotalWeight()
                ));
            }

            JOptionPane.showMessageDialog(
                    this,
                    sb.toString(),
                    "Invalid Course Weights",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
