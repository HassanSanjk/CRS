package repository;

import model.Course;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseRepository
 * Loads course data from CSV file.
 *
 * CSV columns:
 * CourseID,CourseName,Credits,Semester,Instructor,ExamWeight,AssignmentWeight
 */
public class CourseRepository {

    private final String filePath;
    private List<Course> cachedCourses = new ArrayList<>();

    public CourseRepository() {
        this("data" + File.separator + "course_assessment_information.csv");
    }

    public CourseRepository(String filePath) {
        this.filePath = filePath;
        reload();
    }

    public void reload() {
        cachedCourses = loadFromCsv();
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(cachedCourses);
    }

    public Course findById(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) return null;

        String target = courseId.trim().toLowerCase();
        for (Course c : cachedCourses) {
            if (c.getCourseId() != null && c.getCourseId().toLowerCase().equals(target)) {
                return c;
            }
        }
        return null;
    }

    public List<Course> getCoursesWithInvalidWeights() {
        List<Course> invalid = new ArrayList<>();
        for (Course c : cachedCourses) {
            if (!c.isWeightValid()) {
                invalid.add(c);
            }
        }
        return invalid;
    }

    // ----------------- internal loading -----------------

    private List<Course> loadFromCsv() {
        List<Course> courses = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Course CSV not found: " + file.getAbsolutePath());
            return courses;
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
                if (parts.length < 7) {
                    System.err.println("Skipping invalid course row (expected 7 columns): " + line);
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

                    Course c = new Course(courseId, courseName, credits, semester, instructor, examWeight, assignmentWeight);
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
}
