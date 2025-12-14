package services;

import model.Course;
import model.Grade;
import model.Student;
import repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * EligibilityService
 * Rules:
 * - CGPA >= 2.0
 * - failed courses (latest attempt) <= 3
 * - registration allowed only if eligible
 */
public class EligibilityService {

    public enum Status { ELIGIBLE, NOT_ELIGIBLE, PENDING_RESULTS }

    public static class EligibilityRow {
        public final String studentId;
        public final String name;
        public final Double cgpa;
        public final int failedCourses;
        public final Status status;
        public final String reason;
        public final boolean registered;

        public EligibilityRow(String studentId, String name, Double cgpa, int failedCourses,
                              Status status, String reason, boolean registered) {
            this.studentId = studentId;
            this.name = name;
            this.cgpa = cgpa;
            this.failedCourses = failedCourses;
            this.status = status;
            this.reason = reason;
            this.registered = registered;
        }
    }

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final GradeFileHandler gradeFile;
    private final RegistrationRepository registrationRepo;

    public EligibilityService(StudentRepository studentRepo,
                              CourseRepository courseRepo,
                              GradeFileHandler gradeFile,
                              RegistrationRepository registrationRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.gradeFile = gradeFile;
        this.registrationRepo = registrationRepo;
    }

    public List<EligibilityRow> computeAll() {
        List<Student> students = studentRepo.loadAllStudents();
        List<Course> courses = courseRepo.loadAllCourses();

        List<EligibilityRow> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(computeForStudent(s, courses));
        }
        return rows;
    }

    public boolean registerIfEligible(String studentId) {
        studentId = safe(studentId);
        if (studentId.isEmpty()) return false;

        Student s = studentRepo.findById(studentId);
        if (s == null) return false;

        List<Course> courses = courseRepo.loadAllCourses();
        EligibilityRow row = computeForStudent(s, courses);

        if (row.status == Status.ELIGIBLE) {
            registrationRepo.setRegistered(studentId, true);
            return true;
        }
        return false;
    }

    // ---------------- Internal ----------------

    private EligibilityRow computeForStudent(Student s, List<Course> courses) {
        String studentId = s.getStudentId();
        String name = s.getFullName();

        boolean reg = registrationRepo.isRegistered(studentId);

        // latest grades per course from file handler
        List<Grade> latest = gradeFile.latestByCourse(studentId);

        if (latest == null || latest.isEmpty()) {
            return new EligibilityRow(studentId, name, null, 0,
                    Status.PENDING_RESULTS, "Pending results (no grades entered)", reg);
        }

        double totalPoints = 0.0;
        int totalCredits = 0;
        int failed = 0;

        for (Grade g : latest) {
            if (g == null) continue;

            int credits = findCredits(courses, g.getCourseId());
            if (credits <= 0) continue;

            totalCredits += credits;
            totalPoints += g.getGradePoint() * credits;

            if (g.isFailed()) failed++;
        }

        if (totalCredits == 0) {
            return new EligibilityRow(studentId, name, null, failed,
                    Status.PENDING_RESULTS, "Missing credit hours for courses", reg);
        }

        double cgpa = totalPoints / totalCredits;

        boolean okCgpa = cgpa >= 2.0;
        boolean okFails = failed <= 3;

        Status status;
        String reason;

        if (okCgpa && okFails) {
            status = Status.ELIGIBLE;
            reason = "Eligible";
        } else {
            status = Status.NOT_ELIGIBLE;
            if (!okCgpa && !okFails) reason = "CGPA < 2.0 and fails > 3";
            else if (!okCgpa) reason = "CGPA < 2.0";
            else reason = "Fails > 3";
        }

        return new EligibilityRow(studentId, name, round2(cgpa), failed, status, reason, reg);
    }

    private int findCredits(List<Course> courses, String courseId) {
        courseId = safe(courseId);
        if (courseId.isEmpty()) return 0;

        for (Course c : courses) {
            if (c.getCourseId().equalsIgnoreCase(courseId)) {
                return c.getCredits();
            }
        }
        return 0;
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
