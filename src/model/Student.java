package model;

/**
 * Student
 * Stores student profile data (from CSV) + optional eligibility info (CGPA / failed courses).
 */
public class Student {

    // Basic info from CSV
    private String studentId;
    private String firstName;
    private String lastName;
    private String major;
    private String year;  
    private String email;

    // Eligibility info
    private double cgpa;        
    private int failedCourses;  

    public Student(String studentId,
                   String firstName,
                   String lastName,
                   String major,
                   String year,
                   String email) {

        this.studentId = safe(studentId);
        this.firstName = safe(firstName);
        this.lastName  = safe(lastName);
        this.major     = safe(major);
        this.year      = safe(year);
        this.email     = safe(email);

        // default eligibility values
        this.cgpa = 0.0;
        this.failedCourses = 0;
    }

    // constructor if you want to store eligibility values in the object
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

    // ---------------- Getters & Setters ----------------

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = safe(studentId); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = safe(firstName); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = safe(lastName); }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = safe(major); }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = safe(year); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = safe(email); }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) {
        if (cgpa < 0.0) cgpa = 0.0;
        if (cgpa > 4.0) cgpa = 4.0;
        this.cgpa = cgpa;
    }

    public int getFailedCourses() { return failedCourses; }
    public void setFailedCourses(int failedCourses) {
        if (failedCourses < 0) failedCourses = 0;
        this.failedCourses = failedCourses;
    }

    // ---------------- Convenience ----------------

    public String getFullName() {
        // Keep display friendly even if CSV is missing names
        String fn = firstName.isEmpty() ? "(No First Name)" : firstName;
        String ln = lastName.isEmpty() ? "(No Last Name)" : lastName;
        return fn + " " + ln;
    }

    public String toShortString() {
        return safe(studentId) + " - " + getFullName();
    }

    // Quick checks
    public boolean hasEmail() {
        return !safe(email).isEmpty();
    }

    // ---------------- Object methods ----------------

    @Override
    public String toString() {
        return studentId + " - " + getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student other = (Student) o;
        return safe(studentId).equalsIgnoreCase(safe(other.studentId));
    }

    @Override
    public int hashCode() {
        return safe(studentId).toLowerCase().hashCode();
    }

    // ---------------- Helper ----------------

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
