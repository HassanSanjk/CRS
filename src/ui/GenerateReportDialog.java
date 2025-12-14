package ui;

import model.Course;
import model.Grade;
import model.Student;
import repository.*;
import services.AcademicReportPDFService;
import services.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class GenerateReportDialog extends JDialog {

    private final StudentRepository studentRepo = new StudentRepository();
    private final CourseRepository courseRepo = new CourseRepository();
    private final GradeFileHandler gradeFile = new GradeFileHandler("data/grades.txt");

    private final AcademicReportPDFService pdfService = new AcademicReportPDFService();
    private final EmailService emailService = new EmailService();

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterBox;

    // match table row -> Student object
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

        // top (semester)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        top.add(new JLabel("Semester:"));
        semesterBox = new JComboBox<>(loadSemesters());
        top.add(semesterBox);

        add(top, BorderLayout.NORTH);

        // center (table)
        tableModel = new DefaultTableModel(
                new Object[]{"Student ID", "Student Name", "Program", "Year", "Email"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        // bottom (buttons)
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

        java.util.List<Course> courses = courseRepo.loadAllCourses();
        for (Course c : courses) {
            if (c == null) continue;

            String s = safe(c.getSemester());
            if (!s.isEmpty()) {
                sems.add(s);
            }
        }

        return sems.toArray(new String[0]);
    }

    // only show students that already have grades
    private void loadStudentsWithGrades() {
        tableModel.setRowCount(0);
        gradedStudents.clear();

        java.util.List<Student> all = studentRepo.loadAllStudents();

        for (Student s : all) {
            if (s == null) continue;

            // latestByCourse now returns List<Grade>
            java.util.List<Grade> latest = gradeFile.latestByCourse(s.getStudentId());

            if (latest != null && !latest.isEmpty()) {
                gradedStudents.add(s);
                tableModel.addRow(new Object[]{
                        safe(s.getStudentId()),
                        safe(s.getFullName()),
                        safe(s.getMajor()),
                        safe(s.getYear()),
                        safe(s.getEmail())
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
                String email = safe(selectedStudent.getEmail());
                if (email.isEmpty()) {
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

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
