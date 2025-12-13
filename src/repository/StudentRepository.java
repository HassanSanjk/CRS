package repository;

import model.Student;

import java.io.*;
import java.util.*;

/**
 * Merged StudentRepository:
 * - Keeps your old API: loadAllStudents(), findById()
 * - Also supports friend's API: loadStudents() -> StudentMini list
 *
 * CSV expected (your file):
 * StudentID,FirstName,LastName,Major,Year,Email
 */
public class StudentRepository {

    private final String filePath;

    public StudentRepository() {
        this("data/student_information.csv");
    }

    public StudentRepository(String filePath) {
        this.filePath = filePath;
    }

    // ---------- YOUR ORIGINAL API ----------

    public List<Student> loadAllStudents() {
        List<Student> students = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] p = line.split(",", -1);
                if (p.length < 6) continue;

                String id = p[0].trim();
                String first = p[1].trim();
                String last = p[2].trim();
                String major = p[3].trim();
                String year = p[4].trim();
                String email = p[5].trim();

                if (id.isEmpty()) continue;

                students.add(new Student(id, first, last, major, year, email));
            }
        } catch (IOException e) {
            System.err.println("Failed to load students: " + filePath);
            System.err.println("Reason: " + e.getMessage());
        }

        return students;
    }

    public Student findById(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return null;

        String target = studentId.trim().toLowerCase();
        for (Student s : loadAllStudents()) {
            if (s.getStudentId().toLowerCase().equals(target)) return s;
        }
        return null;
    }

    // ---------- FRIEND'S API ----------

    public static class StudentMini {
        public final String studentId;
        public final String name;

        public StudentMini(String studentId, String name) {
            this.studentId = studentId;
            this.name = name;
        }
    }

    /** Friend-style list: StudentID + FullName */
    public List<StudentMini> loadStudents() {
        List<StudentMini> out = new ArrayList<>();
        for (Student s : loadAllStudents()) {
            out.add(new StudentMini(s.getStudentId(), s.getFullName()));
        }
        return out;
    }
}
