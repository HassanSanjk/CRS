package model;

public class Grade {
    private final String studentId;
    private final String courseId;
    private final int attempt;        // 1..3
    private final String letter;      // A, A-, B+, B, C+, C, D, F

    public Grade(String studentId, String courseId, int attempt, String letter) {
        if (studentId == null || studentId.trim().isEmpty())
            throw new IllegalArgumentException("StudentID cannot be empty.");
        if (courseId == null || courseId.trim().isEmpty())
            throw new IllegalArgumentException("CourseID cannot be empty.");
        if (attempt < 1 || attempt > 3)
            throw new IllegalArgumentException("Attempt must be 1..3.");
        if (!isValidLetter(letter))
            throw new IllegalArgumentException("Invalid grade letter.");

        this.studentId = studentId.trim();
        this.courseId = courseId.trim();
        this.attempt = attempt;
        this.letter = letter.trim().toUpperCase();
    }

    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public int getAttempt() { return attempt; }
    public String getLetter() { return letter; }

    public boolean isFailed() { return "F".equals(letter); }

    public double getGradePoint() {
        switch (letter) {
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "D":  return 1.0;
            case "F":  return 0.0;
            default:   return -1.0;
        }
    }

    public static boolean isValidLetter(String s) {
        if (s == null) return false;
        String x = s.trim().toUpperCase();
        return x.equals("A") || x.equals("A-") || x.equals("B+") || x.equals("B") ||
               x.equals("C+") || x.equals("C") || x.equals("D") || x.equals("F");
    }

    public String toTxtLine() {
        return studentId + "|" + courseId + "|" + attempt + "|" + letter;
    }

    public static Grade parseTxtLine(String line) {
        String[] p = line.split("\\|");
        if (p.length != 4) throw new IllegalArgumentException("Bad grades line: " + line);
        int attempt = Integer.parseInt(p[2].trim());
        return new Grade(p[0].trim(), p[1].trim(), attempt, p[3].trim());
    }
}
