// File: src/crs/studentcourse/EligibilityChecker.java
package Eligibility_Checker;

public class EligibilityChecker {

    public static final double MIN_CGPA = 2.0;
    public static final int MAX_FAILED_COURSES = 3;

    /**
     * Check whether a student is eligible to progress.
     *
     * @param student Student object with cgpa and failedCourses set.
     * @return EligibilityResult object with status and detailed message.
     */
    public EligibilityResult checkEligibility(Student student) {
        if (student == null) {
            return new EligibilityResult(false,
                    0.0,
                    0,
                    "No student selected. Please select a student first.");
        }

        double cgpa = student.getCgpa();
        int failed = student.getFailedCourses();

        // If CGPA is 0 and failedCourses is 0, assume not yet recorded
        if (cgpa == 0.0 && failed == 0) {
            return new EligibilityResult(false,
                    cgpa,
                    failed,
                    "Eligibility cannot be checked. Please enter CGPA and number of failed courses for "
                            + student.getFullName() + ".");
        }

        StringBuilder reason = new StringBuilder();
        boolean eligible = true;

        if (cgpa < MIN_CGPA) {
            eligible = false;
            reason.append(String.format(
                    "CGPA is below %.2f (current CGPA: %.2f). ",
                    MIN_CGPA, cgpa));
        } else {
            reason.append(String.format(
                    "CGPA requirement met (%.2f ≥ %.2f). ",
                    cgpa, MIN_CGPA));
        }

        if (failed > MAX_FAILED_COURSES) {
            eligible = false;
            reason.append(String.format(
                    "Too many failed courses (allowed: %d, current: %d). ",
                    MAX_FAILED_COURSES, failed));
        } else {
            reason.append(String.format(
                    "Failed courses requirement met (current: %d, allowed: %d). ",
                    failed, MAX_FAILED_COURSES));
        }

        if (eligible) {
            reason.append("Student is eligible to progress to the next level.");
        } else {
            reason.append("Student is NOT eligible to progress. Please create a recovery plan.");
        }

        return new EligibilityResult(eligible, cgpa, failed, reason.toString());
    }

    // ---------- Inner Result Class ----------

    public static class EligibilityResult {
        private final boolean eligible;
        private final double cgpa;
        private final int failedCourses;
        private final String message;

        public EligibilityResult(boolean eligible,
                                 double cgpa,
                                 int failedCourses,
                                 String message) {
            this.eligible = eligible;
            this.cgpa = cgpa;
            this.failedCourses = failedCourses;
            this.message = message;
        }

        public boolean isEligible() {
            return eligible;
        }

        public double getCgpa() {
            return cgpa;
        }

        public int getFailedCourses() {
            return failedCourses;
        }

        public String getMessage() {
            return message;
        }

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
