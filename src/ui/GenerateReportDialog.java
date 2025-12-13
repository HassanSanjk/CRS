package ui;

import model.Student;
import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.StudentRepository;
import services.AcademicReportPDFService;
import services.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;

public class GenerateReportDialog extends JDialog {

    private final StudentRepository studentRepo = new StudentRepository();
    private final CourseRepository courseRepo = new CourseRepository();
    private final GradeFileHandler gradeFile = new GradeFileHandler("data/grades.txt");

    private final AcademicReportPDFService pdfService = new AcademicReportPDFService();
    private final EmailService emailService = new EmailService();

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterBox;

    // keeps the full Student objects aligned with table rows
    private java.util.List<Student> gradedStudents = new java.util.ArrayList<Student>();

    public GenerateReportDialog(Frame owner) {
        super(owner, "Generate Academic Report (PDF)", true);
        setSize(800, 450);
        setLocationRelativeTo(owner);
        initUI();
        loadStudentsWithGrades();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Top panel: semester selector
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        top.add(new JLabel("Semester:"));
        semesterBox = new JComboBox<>(loadSemesters());
        top.add(semesterBox);

        add(top, BorderLayout.NORTH);

        // Center: student table
        tableModel = new DefaultTableModel(new Object[]{"Student ID", "Student Name", "Program", "Year", "Email"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        // Bottom buttons
        JButton genBtn = new JButton("Generate PDF");
        JButton cancelBtn = new JButton("Cancel");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(cancelBtn);
        bottom.add(genBtn);

        add(bottom, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dispose());
        genBtn.addActionListener(e -> onGenerate());
    }

    private String[] loadSemesters() {
        Set<String> sems = new LinkedHashSet<String>();
        sems.add("(All)");

        courseRepo.loadAllCourses().forEach(c -> {
            String s = c.getSemester();
            if (s != null && !s.trim().isEmpty()) sems.add(s.trim());
        });

        return sems.toArray(new String[0]);
    }

    /**
     * Loads ONLY students who have at least one grade in grades.txt.
     * Beginner approach:
     * 1) read all students
     * 2) for each, check if latestByCourse(studentId) is empty or not
     * 3) fill table with the ones that have grades
     */
    private void loadStudentsWithGrades() {
        tableModel.setRowCount(0);
        gradedStudents.clear();

        java.util.List<Student> all = studentRepo.loadAllStudents();

        for (Student s : all) {
            Map<String, ?> latest = gradeFile.latestByCourse(s.getStudentId());
            if (latest != null && !latest.isEmpty()) {
                gradedStudents.add(s);
                tableModel.addRow(new Object[]{
                        s.getStudentId(),
                        s.getFullName(),
                        s.getMajor(),
                        s.getYear(),
                        s.getEmail()
                });
            }
        }

        if (gradedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No students with grades found in grades.txt.\nCannot generate reports yet.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onGenerate() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the list.",
                    "Validation",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student selectedStudent = gradedStudents.get(row);

        String sem = (String) semesterBox.getSelectedItem();
        if (sem == null) sem = "(All)";
        String semFilter = "(All)".equals(sem) ? "" : sem;

        try {
            AcademicReportPDFService.ReportResult result =
                    pdfService.generateAcademicReportPdf(selectedStudent.getStudentId(), semFilter);

            String pdfPath = result.pdfPath;

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "PDF generated:\n" + pdfPath + "\n\nSend to student's email now?",
                    "Send Email",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String email = selectedStudent.getEmail();
                if (email == null || email.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Student has no email on record.",
                            "Cannot Send Email",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String semesterLabel = semFilter.isEmpty() ? "All Semesters" : semFilter;

                boolean sent = emailService.sendPerformanceReportWithAttachment(
                        email,
                        selectedStudent.getFullName(),
                        semesterLabel,
                        result.cgpa,
                        new File(pdfPath)
                );

                JOptionPane.showMessageDialog(this,
                        sent ? "Email sent to: " + email : "Email failed to send (check SMTP/App Password).",
                        sent ? "Success" : "Email Error",
                        sent ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this,
                        "Done. PDF is saved in /reports folder.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
