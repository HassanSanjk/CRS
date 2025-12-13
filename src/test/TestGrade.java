package test;

import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.StudentRepository;
import ui.GradeEntryFrame;

import javax.swing.*;

public class TestGrade {

    public static void main(String[] args) {

        // Put your lecturer CSVs in /data exactly with these names:
        String STUDENTS_CSV = "data/student_information.csv";
        String COURSES_CSV  = "data/course_assessment_information.csv";

        // Our grade storage:
        String GRADES_TXT   = "data/grades.txt";

        StudentRepository studentRepo = new StudentRepository(STUDENTS_CSV);
        CourseRepository courseRepo   = new CourseRepository(COURSES_CSV);
        GradeFileHandler gradeFile    = new GradeFileHandler(GRADES_TXT);

        SwingUtilities.invokeLater(() -> {
            GradeEntryFrame f = new GradeEntryFrame(studentRepo, courseRepo, gradeFile);
            f.setVisible(true);
        });
    }
}
