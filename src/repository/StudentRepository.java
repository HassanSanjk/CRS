package repository;


import model.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentRepository
 * Loads student data from CSV file.
 *
 * CSV columns: StudentID,FirstName,LastName,Major,Year,Email
 */
public class StudentRepository {

    private final String filePath;

    // Cached list (so we don't read the file repeatedly)
    private List<Student> cachedStudents = new ArrayList<>();

    public StudentRepository() {
        this("data" + File.separator + "student_information.csv");
    }

    public StudentRepository(String filePath) {
        this.filePath = filePath;
        reload(); // load immediately
    }

    /**
     * Reload students from CSV into cache.
     */
    public void reload() {
        cachedStudents = loadFromCsv();
    }

    /**
     * Get all students currently loaded.
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(cachedStudents);
    }

    /**
     * Find a student by ID (case-insensitive).
     */
    public Student findById(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return null;

        String target = studentId.trim().toLowerCase();

        for (Student s : cachedStudents) {
            if (s.getStudentId() != null && s.getStudentId().toLowerCase().equals(target)) {
                return s;
            }
        }
        return null;
    }

    // ----------------- internal loading -----------------

    private List<Student> loadFromCsv() {
        List<Student> students = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Student CSV not found: " + file.getAbsolutePath());
            return students;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    System.err.println("Skipping invalid student row (expected 6 columns): " + line);
                    continue;
                }

                String id = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                String major = parts[3].trim();
                String year = parts[4].trim();
                String email = parts[5].trim();

                Student s = new Student(id, firstName, lastName, major, year, email);
                students.add(s);
            }

        } catch (IOException e) {
            System.err.println("Failed to load students from: " + filePath);
            System.err.println("Reason: " + e.getMessage());
        }

        return students;
    }
}
