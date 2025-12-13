package test;

import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.RegistrationRepository;
import repository.StudentRepository;
import services.EligibilityService;
import ui.EligibilityPanel;

import javax.swing.*;

public class TestEligibility {

    public static void main(String[] args) {
        String STUDENTS_CSV = "data/student_information.csv";
        String COURSES_CSV  = "data/course_assessment_information.csv";
        String GRADES_TXT   = "data/grades.txt";
        String REG_TXT      = "data/registration.txt";

        StudentRepository studentRepo = new StudentRepository(STUDENTS_CSV);
        CourseRepository courseRepo   = new CourseRepository(COURSES_CSV);
        GradeFileHandler gradeFile    = new GradeFileHandler(GRADES_TXT);
        RegistrationRepository regRepo = new RegistrationRepository(REG_TXT);

        EligibilityService service = new EligibilityService(studentRepo, courseRepo, gradeFile, regRepo);

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Eligibility Check & Enrolment - CRS");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1000, 520);
            f.setLocationRelativeTo(null);
            f.setContentPane(new EligibilityPanel(service));
            f.setVisible(true);
        });
    }
}
