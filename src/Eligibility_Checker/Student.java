// File: src/crs/studentcourse/Student.java
package Eligibility_Checker;

import java.util.Objects;

public class Student {

    private String studentId;
    private String firstName;
    private String lastName;
    private String major;
    private String year;      // e.g. "Freshman", "Sophomore", etc.
    private String email;

    // Extra fields for eligibility
    private double cgpa;          // 0.0 – 4.0
    private int failedCourses;    // number of failed courses

    public Student(String studentId,
                   String firstName,
                   String lastName,
                   String major,
                   String year,
                   String email) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.major = major;
        this.year = year;
        this.email = email;
        this.cgpa = 0.0;
        this.failedCourses = 0;
    }

    // Convenience constructor including CGPA & failedCourses
    public Student(String studentId,
                   String firstName,
                   String lastName,
                   String major,
                   String year,
                   String email,
                   double cgpa,
                   int failedCourses) {
        this(studentId, firstName, lastName, major, year, email);
        setCgpa(cgpa);
        setFailedCourses(failedCourses);
    }

    // ---------- Getters & Setters ----------

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        this.studentId = studentId.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = lastName.trim();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = (major == null) ? "" : major.trim();
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = (year == null) ? "" : year.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email == null) ? "" : email.trim();
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 4.0) {
            // Fallback: clamp into valid range instead of crashing
            this.cgpa = Math.max(0.0, Math.min(cgpa, 4.0));
        } else {
            this.cgpa = cgpa;
        }
    }

    public int getFailedCourses() {
        return failedCourses;
    }

    public void setFailedCourses(int failedCourses) {
        if (failedCourses < 0) {
            // Fallback: don't allow negative failed courses
            this.failedCourses = 0;
        } else {
            this.failedCourses = failedCourses;
        }
    }

    // ---------- Convenience Methods ----------

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String toShortString() {
        return studentId + " - " + getFullName();
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", major='" + major + '\'' +
                ", year='" + year + '\'' +
                ", email='" + email + '\'' +
                ", cgpa=" + cgpa +
                ", failedCourses=" + failedCourses +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
