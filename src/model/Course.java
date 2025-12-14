package model;

/**
 * Course
 * Stores course information (from CSV) + assessment weights.
 */
public class Course {

    private String courseId;
    private String courseName;
    private int credits;
    private String semester;
    private String instructor;
    private int examWeight;     
    private int assignmentWeight; 

    public Course(String courseId,
                  String courseName,
                  int credits,
                  String semester,
                  String instructor,
                  int examWeight,
                  int assignmentWeight) {

        setCourseId(courseId);
        setCourseName(courseName);
        setCredits(credits);
        setSemester(semester);
        setInstructor(instructor);
        setExamWeight(examWeight);
        setAssignmentWeight(assignmentWeight);
    }

    // ---------------- Getters and Setters ----------------

    public String getCourseId() { return courseId; }

    public void setCourseId(String courseId) {
        String id = safe(courseId);
        if (id.isEmpty()) throw new IllegalArgumentException("Course ID cannot be empty.");
        this.courseId = id;
    }

    public String getCourseName() { return courseName; }

    public void setCourseName(String courseName) {
        String name = safe(courseName);
        if (name.isEmpty()) throw new IllegalArgumentException("Course name cannot be empty.");
        this.courseName = name;
    }

    public int getCredits() { return credits; }

    public void setCredits(int credits) {
        if (credits <= 0) throw new IllegalArgumentException("Credits must be a positive number.");
        this.credits = credits;
    }

    public String getSemester() { return semester; }

    public void setSemester(String semester) {
        this.semester = safe(semester);
    }

    public String getInstructor() { return instructor; }

    public void setInstructor(String instructor) {
        this.instructor = safe(instructor);
    }

    public int getExamWeight() { return examWeight; }

    public void setExamWeight(int examWeight) {
        if (examWeight < 0 || examWeight > 100) {
            throw new IllegalArgumentException("Exam weight must be between 0 and 100.");
        }
        this.examWeight = examWeight;
    }

    public int getAssignmentWeight() { return assignmentWeight; }

    public void setAssignmentWeight(int assignmentWeight) {
        if (assignmentWeight < 0 || assignmentWeight > 100) {
            throw new IllegalArgumentException("Assignment weight must be between 0 and 100.");
        }
        this.assignmentWeight = assignmentWeight;
    }

    // ---------------- Helper Methods ----------------

    public int getTotalWeight() {
        return examWeight + assignmentWeight;
    }

    public boolean isWeightValid() {
        return getTotalWeight() == 100;
    }

    // for UI dropdowns
    public String toShortString() {
        if (courseName.isEmpty()) return courseId;
        return courseId + " - " + courseName;
    }

    // ---------------- Object methods ----------------

    @Override
    public String toString() {
        // Cleaner and more useful than printing a big object block
        return toShortString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course other = (Course) o;
        return safe(courseId).equalsIgnoreCase(safe(other.courseId));
    }

    @Override
    public int hashCode() {
        return safe(courseId).toLowerCase().hashCode();
    }

    // ---------------- Helper ----------------

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
