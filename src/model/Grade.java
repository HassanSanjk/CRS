package model;

public class Grade {

    private final String studentId;
    private final String courseId;
    private final int attempt;
    private final String letter;  

    public Grade(String studentId, String courseId, int attempt, String letter) {

        String sid = safe(studentId);
        String cid = safe(courseId);
        String let = safe(letter).toUpperCase();

        if (sid.isEmpty())
            throw new IllegalArgumentException("StudentID cannot be empty.");
        if (cid.isEmpty())
            throw new IllegalArgumentException("CourseID cannot be empty.");
        if (attempt < 1 || attempt > 3)
            throw new IllegalArgumentException("Attempt must be between 1 and 3.");
        if (!isValidLetter(let))
            throw new IllegalArgumentException("Invalid grade letter.");

        this.studentId = sid;
        this.courseId = cid;
        this.attempt = attempt;
        this.letter = let;
    }

    // ---------- Getters ----------

    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public int getAttempt() { return attempt; }
    public String getLetter() { return letter; }

    // ---------- Logic ----------

    // Fail ONLY if grade is F
    public boolean isFailed() {
        return "F".equals(letter);
    }

    public double getGradePoint() {
        if ("A".equals(letter))  return 4.0;
        if ("A-".equals(letter)) return 3.7;
        if ("B+".equals(letter)) return 3.3;
        if ("B".equals(letter))  return 3.0;
        if ("C+".equals(letter)) return 2.3;
        if ("C".equals(letter))  return 2.0;
        if ("D".equals(letter))  return 1.0;
        if ("F".equals(letter))  return 0.0;
        return 0.0;
    }

    public static boolean isValidLetter(String s) {
        String x = safe(s).toUpperCase();
        return x.equals("A") || x.equals("A-") || x.equals("B+") || x.equals("B") ||
               x.equals("C+") || x.equals("C") || x.equals("D") || x.equals("F");
    }

    // ---------- File IO Helpers ----------

    //Format: studentId|courseId|attempt|grade
    public String toTxtLine() {
        return studentId + "|" + courseId + "|" + attempt + "|" + letter;
    }

    public static Grade parseTxtLine(String line) {
        String raw = safe(line);
        if (raw.isEmpty())
            throw new IllegalArgumentException("Empty grade line.");

        String[] p = raw.split("\\|");
        if (p.length != 4)
            throw new IllegalArgumentException("Invalid grade record: " + raw);

        int attempt;
        try {
            attempt = Integer.parseInt(p[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid attempt number: " + raw);
        }

        return new Grade(
                p[0].trim(),
                p[1].trim(),
                attempt,
                p[3].trim()
        );
    }

    // ---------- Helper ----------

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
