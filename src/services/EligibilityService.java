package services;

import model.Course;
import model.Grade;
import model.Student;
import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.RegistrationRepository;
import repository.StudentRepository;

import java.util.*;

/**
 * Eligibility Check & Enrolment:
 * - CGPA >= 2.0
 * - Failed courses (latest attempt per course) <= 3
 * - Registration allowed only if eligible
 *
 * This version is refactored to work with YOUR existing:
 * - StudentRepository.loadAllStudents() -> List<Student>
 * - CourseRepository.loadAllCourses() -> List<Course>
 */
public class EligibilityService {

    public enum Status { ELIGIBLE, NOT_ELIGIBLE, PENDING_RESULTS }

    public static class EligibilityRow {
        public final String studentId;
        public final String name;
        public final Double cgpa; // null => N/A
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

    /** Computes eligibility for ALL students (for "list out all not eligible"). */
    public List<EligibilityRow> computeAll() {
        List<Student> students = studentRepo.loadAllStudents();
        Map<String, Integer> creditsMap = buildCreditsMap();

        List<EligibilityRow> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(computeForStudent(s, creditsMap));
        }
        return rows;
    }

    /** Registration allowed only if eligible. */
    public boolean registerIfEligible(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return false;

        Student s = studentRepo.findById(studentId);
        if (s == null) return false;

        Map<String, Integer> creditsMap = buildCreditsMap();
        EligibilityRow r = computeForStudent(s, creditsMap);

        if (r.status == Status.ELIGIBLE) {
            registrationRepo.setRegistered(studentId, true);
            return true;
        }
        return false;
    }

    // ---------- Internal helpers ----------

    private EligibilityRow computeForStudent(Student s, Map<String, Integer> creditsMap) {
        String studentId = s.getStudentId();
        String name = s.getFullName();

        // Latest attempt per course (the rule your friend used)
        Map<String, Grade> latest = gradeFile.latestByCourse(studentId);

        boolean reg = registrationRepo.isRegistered(studentId);

        // No grades entered yet -> pending
        if (latest.isEmpty()) {
            return new EligibilityRow(studentId, name, null, 0,
                    Status.PENDING_RESULTS, "Pending results (no grades entered)", reg);
        }

        double totalGradePoints = 0.0;
        int totalCredits = 0;
        int failed = 0;

        for (Grade g : latest.values()) {
            int credits = creditsMap.getOrDefault(g.getCourseId(), 0);

            // If the course is unknown or credits missing, skip to avoid crashing
            if (credits <= 0) continue;

            totalCredits += credits;
            totalGradePoints += g.getGradePoint() * credits;

            if (g.isFailed()) failed++;
        }

        if (totalCredits == 0) {
            return new EligibilityRow(studentId, name, null, failed,
                    Status.PENDING_RESULTS, "Missing credit hours for courses", reg);
        }

        double cgpa = totalGradePoints / totalCredits;

        boolean meetsCgpa = cgpa >= 2.0;
        boolean meetsFails = failed <= 3;

        Status status;
        String reason;

        if (meetsCgpa && meetsFails) {
            status = Status.ELIGIBLE;
            reason = "Eligible";
        } else {
            status = Status.NOT_ELIGIBLE;
            if (!meetsCgpa && !meetsFails) reason = "CGPA < 2.0 & fails > 3";
            else if (!meetsCgpa) reason = "CGPA < 2.0";
            else reason = "Fails > 3";
        }

        return new EligibilityRow(studentId, name, round2(cgpa), failed, status, reason, reg);
    }

    private Map<String, Integer> buildCreditsMap() {
        Map<String, Integer> map = new HashMap<>();
        List<Course> courses = courseRepo.loadAllCourses();
        for (Course c : courses) {
            map.put(c.getCourseId(), c.getCredits());
        }
        return map;
    }

    private Double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
