package services;

import model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * EligibilityService
 * Checks if a student can progress to next level based on:
 * 1) CGPA >= 2.0
 * 2) Failed courses <= 3
 */
public class EligibilityService {

    public static final double MIN_CGPA = 2.0;
    public static final int MAX_FAILED_COURSES = 3;

    /**
     * Check whether a student is eligible to progress.
     */
    public EligibilityResult checkEligibility(Student student) {
        if (student == null) {
            return new EligibilityResult(false, 0.0, 0,
                    "No student selected. Please select a student first.");
        }

        double cgpa = student.getCgpa();
        int failed = student.getFailedCourses();

        // Basic validation message if values look missing
        if (cgpa < 0 || failed < 0) {
            return new EligibilityResult(false, cgpa, failed,
                    "Invalid academic data (negative values). Please check the student's CGPA/failed courses.");
        }

        boolean eligible = true;
        StringBuilder reason = new StringBuilder();

        // CGPA rule
        if (cgpa >= MIN_CGPA) {
            reason.append(String.format("CGPA requirement met (%.2f ≥ %.2f). ", cgpa, MIN_CGPA));
        } else {
            eligible = false;
            reason.append(String.format("CGPA is below %.2f (current CGPA: %.2f). ", MIN_CGPA, cgpa));
        }

        // Failed courses rule
        if (failed <= MAX_FAILED_COURSES) {
            reason.append(String.format("Failed courses requirement met (current: %d, allowed: %d). ",
                    failed, MAX_FAILED_COURSES));
        } else {
            eligible = false;
            reason.append(String.format("Too many failed courses (allowed: %d, current: %d). ",
                    MAX_FAILED_COURSES, failed));
        }

        if (eligible) {
            reason.append("Student is eligible to progress to the next level.");
        } else {
            reason.append("Student is NOT eligible to progress. Please create a recovery plan.");
        }

        return new EligibilityResult(eligible, cgpa, failed, reason.toString());
    }

    /**
     * Requirement support:
     * List out all students who are NOT eligible.
     */
    public List<Student> getNotEligibleStudents(List<Student> students) {
        List<Student> result = new ArrayList<>();
        if (students == null) return result;

        for (Student s : students) {
            EligibilityResult r = checkEligibility(s);
            if (!r.isEligible()) {
                result.add(s);
            }
        }
        return result;
    }

    // ---------- Inner Result Class ----------

    public static class EligibilityResult {
        private final boolean eligible;
        private final double cgpa;
        private final int failedCourses;
        private final String message;

        public EligibilityResult(boolean eligible, double cgpa, int failedCourses, String message) {
            this.eligible = eligible;
            this.cgpa = cgpa;
            this.failedCourses = failedCourses;
            this.message = message;
        }

        public boolean isEligible() { return eligible; }
        public double getCgpa() { return cgpa; }
        public int getFailedCourses() { return failedCourses; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "EligibilityResult{" +
                    "eligible=" + eligible +
                    ", cgpa=" + cgpa +
                    ", failedCourses=" + failedCourses +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
