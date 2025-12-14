package repository;

import model.Course;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseRepository
 * Reads course data from CSV.
 *
 * CSV format:
 * CourseID,CourseName,Credits,Semester,Instructor,ExamWeight,AssignmentWeight
 */
public class CourseRepository {

    private final String filePath;

    public CourseRepository() {
        this.filePath = "data/course_assessment_information.csv";
    }

    public CourseRepository(String filePath) {
        this.filePath = filePath;
    }

    public List<Course> loadAllCourses() {
        List<Course> courses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // skip header row
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;

                String courseId = parts[0].trim();
                String courseName = parts[1].trim();
                String semester = parts[3].trim();
                String instructor = parts[4].trim();

                int credits;
                int examWeight;
                int assignmentWeight;

                try {
                    credits = Integer.parseInt(parts[2].trim());
                    examWeight = Integer.parseInt(parts[5].trim());
                    assignmentWeight = Integer.parseInt(parts[6].trim());
                } catch (NumberFormatException ex) {
                    // skip bad numeric values
                    continue;
                }

                if (courseId.isEmpty()) continue;

                courses.add(new Course(courseId, courseName, credits, semester, instructor, examWeight, assignmentWeight));
            }

        } catch (IOException e) {
            System.out.println("Error reading course file: " + filePath);
        }

        return courses;
    }

    public Course findById(String courseId) {
        courseId = safe(courseId);
        if (courseId.isEmpty()) return null;

        for (Course c : loadAllCourses()) {
            if (c.getCourseId().equalsIgnoreCase(courseId)) {
                return c;
            }
        }
        return null;
    }

    public List<Course> getCoursesWithInvalidWeights() {
        List<Course> invalid = new ArrayList<>();
        for (Course c : loadAllCourses()) {
            if (!c.isWeightValid()) {
                invalid.add(c);
            }
        }
        return invalid;
    }

    // Used for dropdowns
    public List<CourseMini> loadCourses() {
        List<CourseMini> list = new ArrayList<>();
        for (Course c : loadAllCourses()) {
            list.add(new CourseMini(c.getCourseId(), c.getCourseName(), c.getCredits()));
        }
        return list;
    }

    // helper
    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    // Small helper class for UI lists.
    public static class CourseMini {
        public final String courseId;
        public final String courseName;
        public final int credits;

        public CourseMini(String courseId, String courseName, int credits) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.credits = credits;
        }

        @Override
        public String toString() {
            return courseId + " - " + courseName;
        }
    }
}
