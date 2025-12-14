package repository;

import model.Student;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentRepository
 * Reads student data from CSV.
 *
 * Format:
 * StudentID,FirstName,LastName,Major,Year,Email
 */
public class StudentRepository {

    private final String filePath;

    public StudentRepository() {
        this.filePath = "data/student_information.csv";
    }

    public StudentRepository(String filePath) {
        this.filePath = filePath;
    }

    public List<Student> loadAllStudents() {
        List<Student> students = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                String id = safe(parts[0]);
                if (id.isEmpty()) continue;

                String firstName = safe(parts[1]);
                String lastName  = safe(parts[2]);
                String major     = safe(parts[3]);
                String year      = safe(parts[4]);
                String email     = safe(parts[5]);

                students.add(new Student(id, firstName, lastName, major, year, email));
            }

        } catch (IOException e) {
            System.out.println("Student file read error: " + filePath);
        }

        return students;
    }

    public Student findById(String studentId) {
        studentId = safe(studentId);
        if (studentId.isEmpty()) return null;

        for (Student s : loadAllStudents()) {
            if (s.getStudentId().equalsIgnoreCase(studentId)) {
                return s;
            }
        }
        return null;
    }

    // show ID + full name
    public List<StudentMini> loadStudents() {
        List<StudentMini> list = new ArrayList<>();
        for (Student s : loadAllStudents()) {
            list.add(new StudentMini(s.getStudentId(), s.getFullName()));
        }
        return list;
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    public static class StudentMini {
        public final String studentId;
        public final String name;

        public StudentMini(String studentId, String name) {
            this.studentId = studentId;
            this.name = name;
        }
    }
}
