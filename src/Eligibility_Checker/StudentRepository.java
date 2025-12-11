// File: src/crs/studentcourse/StudentRepository.java
package Eligibility_Checker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    private final String filePath;

    // You can change this to "data/student_information.csv" if needed
    public StudentRepository() {
        this("src/Database/student_information.csv");
    }

    public StudentRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Load all students from CSV file.
     * CSV columns (fixed): StudentID,FirstName,LastName,Major,Year,Email
     */
    public List<Student> loadAllStudents() {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Skip header row
                if (firstLine) {
                    firstLine = false;
                    // Optional: you can validate header format here if you want
                    continue;
                }

                String[] parts = line.split(",", -1); // keep empty columns
                if (parts.length < 6) {
                    System.err.println(
                            "Skipping invalid student row (expected 6 columns): " + line);
                    continue;
                }

                String id = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                String major = parts[3].trim();
                String year = parts[4].trim();
                String email = parts[5].trim();

                // CGPA & failedCourses not stored in CSV -> default to 0
                Student s = new Student(id, firstName, lastName, major, year, email);
                students.add(s);
            }

        } catch (IOException e) {
            System.err.println("Failed to load students from file: " + filePath);
            System.err.println("Reason: " + e.getMessage());
            // Return empty list instead of crashing – good fallback behaviour
        }

        return students;
    }

    /**
     * Find a student by ID (case-insensitive).
     */
    public Student findById(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return null;
        }
        String target = studentId.trim().toLowerCase();

        for (Student s : loadAllStudents()) {
            if (s.getStudentId().toLowerCase().equals(target)) {
                return s;
            }
        }
        return null;
    }
}
