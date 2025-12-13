package ui;

import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.RegistrationRepository;
import repository.StudentRepository;
import services.EligibilityService;

import javax.swing.*;
import java.awt.*;

/**
 * StudentCourseModuleFrame
 * - Tab 1: Students & Courses view (your existing panel)
 * - Tab 2: Eligibility + Registration (friend's module)
 * - Grade Entry opens as a separate window (friend's GradeEntryFrame)
 */
public class StudentCourseModuleFrame extends JFrame {

    public StudentCourseModuleFrame() {
        setTitle("Student & Course Module - CRS");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- Shared repositories/services (same files for the whole module)
        StudentRepository studentRepo = new StudentRepository(); // uses data/student_information.csv
        CourseRepository courseRepo = new CourseRepository();    // uses data/course_assessment_information.csv
        GradeFileHandler gradeFile = new GradeFileHandler("data/grades.txt");
        RegistrationRepository regRepo = new RegistrationRepository("data/registrations.txt");

        EligibilityService eligibilityService =
                new EligibilityService(studentRepo, courseRepo, gradeFile, regRepo);

        // --- Tabs
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Your original students/courses panel (clean version)
        tabs.addTab("Students & Courses", new StudentCourseManagementPanel());

        // Tab 2: Eligibility panel + button to open grade entry window
        JPanel eligibilityTab = new JPanel(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOpenGradeEntry = new JButton("Open Grade Entry");
        topBar.add(btnOpenGradeEntry);

        EligibilityPanel eligibilityPanel = new EligibilityPanel(eligibilityService);

        eligibilityTab.add(topBar, BorderLayout.NORTH);
        eligibilityTab.add(eligibilityPanel, BorderLayout.CENTER);

        // Open Grade Entry window
        btnOpenGradeEntry.addActionListener(e -> {
            GradeEntryFrame frame = new GradeEntryFrame(studentRepo, courseRepo, gradeFile);
            frame.setVisible(true);
        });

        tabs.addTab("Eligibility & Registration", eligibilityTab);

        add(tabs);
    }
}
