package repository;

import model.Course;
import java.io.*;
import java.util.*;

public class CourseRepository {

    private final String filePath;

    public CourseRepository() {
        this("data/course_assessment_information.csv");
    }

    public CourseRepository(String filePath) {
        this.filePath = filePath;
    }

    // ---------- YOUR ORIGINAL API ----------

    public List<Course> loadAllCourses() {
        List<Course> courses = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] p = line.split(",", -1);
                if (p.length < 7) continue;

                try {
                    String courseId = p[0].trim();
                    String courseName = p[1].trim();
                    int credits = Integer.parseInt(p[2].trim());
                    String semester = p[3].trim();
                    String instructor = p[4].trim();
                    int examWeight = Integer.parseInt(p[5].trim());
                    int assignmentWeight = Integer.parseInt(p[6].trim());

                    courses.add(new Course(courseId, courseName, credits, semester, instructor, examWeight, assignmentWeight));
                } catch (NumberFormatException ex) {
                    System.err.println("Skipping invalid course row: " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to load courses: " + filePath);
            System.err.println("Reason: " + e.getMessage());
        }

        return courses;
    }

    public Course findById(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) return null;

        String target = courseId.trim().toLowerCase();
        for (Course c : loadAllCourses()) {
            if (c.getCourseId().toLowerCase().equals(target)) return c;
        }
        return null;
    }

    public List<Course> getCoursesWithInvalidWeights() {
        List<Course> invalid = new ArrayList<>();
        for (Course c : loadAllCourses()) {
            if (!c.isWeightValid()) invalid.add(c);
        }
        return invalid;
    }

    public Map<String, Integer> loadCreditsMap() {
        Map<String, Integer> map = new HashMap<>();
        for (Course c : loadAllCourses()) {
            map.put(c.getCourseId(), c.getCredits());
        }
        return map;
    }

    // ---------- FRIEND'S UI SUPPORT (for GradeEntryFrame) ----------

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

    public List<CourseMini> loadCourses() {
        List<CourseMini> list = new ArrayList<>();
        for (Course c : loadAllCourses()) {
            list.add(new CourseMini(c.getCourseId(), c.getCourseName(), c.getCredits()));
        }
        return list;
    }
}
