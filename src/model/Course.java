package model;


import java.util.Objects;

public class Course {

    private String courseId;
    private String courseName;
    private int credits;
    private String semester;
    private String instructor;
    private int examWeight;        // e.g. 60
    private int assignmentWeight;  // e.g. 40

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

    // ---------- Getters & Setters ----------

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be empty.");
        }
        this.courseId = courseId.trim();
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }
        this.courseName = courseName.trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be a positive number.");
        }
        this.credits = credits;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = (semester == null) ? "" : semester.trim();
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = (instructor == null) ? "" : instructor.trim();
    }

    public int getExamWeight() {
        return examWeight;
    }

    public void setExamWeight(int examWeight) {
        if (examWeight < 0 || examWeight > 100) {
            throw new IllegalArgumentException("Exam weight must be between 0 and 100.");
        }
        this.examWeight = examWeight;
    }

    public int getAssignmentWeight() {
        return assignmentWeight;
    }

    public void setAssignmentWeight(int assignmentWeight) {
        if (assignmentWeight < 0 || assignmentWeight > 100) {
            throw new IllegalArgumentException("Assignment weight must be between 0 and 100.");
        }
        this.assignmentWeight = assignmentWeight;
    }

    // ---------- Helper Methods ----------

    public int getTotalWeight() {
        return examWeight + assignmentWeight;
    }

    public boolean isWeightValid() {
        // A+ check: report if exam + assignment != 100
        return getTotalWeight() == 100;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                ", semester='" + semester + '\'' +
                ", instructor='" + instructor + '\'' +
                ", examWeight=" + examWeight +
                ", assignmentWeight=" + assignmentWeight +
                ", totalWeight=" + getTotalWeight() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return Objects.equals(courseId, course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }
}
