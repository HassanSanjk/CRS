// File: src/crs/studentcourse/CourseRepository.java
package Eligibility_Checker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private final String filePath;

    // You can change this to "course_assessment_information.csv" path as needed
    public CourseRepository() {
        this("src/Database/course_assessment_information.csv");
    }

    public CourseRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Load all courses from CSV file.
     * CSV columns (fixed):
     * CourseID,CourseName,Credits,Semester,Instructor,ExamWeight,AssignmentWeight
     */
    public List<Course> loadAllCourses() {
        List<Course> courses = new ArrayList<>();

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
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    System.err.println(
                            "Skipping invalid course row (expected 7 columns): " + line);
                    continue;
                }

                try {
                    String courseId = parts[0].trim();
                    String courseName = parts[1].trim();
                    int credits = Integer.parseInt(parts[2].trim());
                    String semester = parts[3].trim();
                    String instructor = parts[4].trim();
                    int examWeight = Integer.parseInt(parts[5].trim());
                    int assignmentWeight = Integer.parseInt(parts[6].trim());

                    Course c = new Course(
                            courseId,
                            courseName,
                            credits,
                            semester,
                            instructor,
                            examWeight,
                            assignmentWeight
                    );
                    courses.add(c);
                } catch (NumberFormatException nfe) {
                    System.err.println("Skipping course row due to invalid number: " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to load courses from file: " + filePath);
            System.err.println("Reason: " + e.getMessage());
        }

        return courses;
    }

    /**
     * Find course by ID (case-insensitive).
     */
    public Course findById(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return null;
        }
        String target = courseId.trim().toLowerCase();

        for (Course c : loadAllCourses()) {
            if (c.getCourseId().toLowerCase().equals(target)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Helper: list courses where exam + assignment != 100 (invalid weighting).
     */
    public List<Course> getCoursesWithInvalidWeights() {
        List<Course> invalid = new ArrayList<>();
        for (Course c : loadAllCourses()) {
            if (!c.isWeightValid()) {
                invalid.add(c);
            }
        }
        return invalid;
    }
}
