package repository;

import model.Grade;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GradeFileHandler
 * Handles reading and writing grades.txt
 *
 * Format:
 * StudentID|CourseID|Attempt|Grade
 */
public class GradeFileHandler {

    public static final String HEADER = "StudentID|CourseID|Attempt|Grade";
    private final File file;

    public GradeFileHandler(String path) {
        file = new File(path);
        ensureFileAndHeader();
    }

    // make sure file exists and has header
    private void ensureFileAndHeader() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                file.createNewFile();
            }

            List<String> lines = readAllLinesRaw();

            if (lines.isEmpty()) {
                writeAllLinesRaw(Collections.singletonList(HEADER));
                return;
            }

            if (!lines.get(0).trim().equalsIgnoreCase(HEADER)) {
                List<String> fixed = new ArrayList<>();
                fixed.add(HEADER);
                fixed.addAll(lines);
                writeAllLinesRaw(fixed);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize grades file.", e);
        }
    }

    public List<Grade> readAll() {
        List<Grade> grades = new ArrayList<>();

        for (String line : readAllLinesRaw()) {
            String text = safe(line);
            if (text.isEmpty() || text.equalsIgnoreCase(HEADER)) continue;

            try {
                Grade g = Grade.parseTxtLine(text);
                if (g != null) grades.add(g);
            } catch (Exception ignored) {
                // skip invalid line
            }
        }
        return grades;
    }

    //Update if same student + course + attempt exists, otherwise add new.
    public void upsert(Grade grade) {
        if (grade == null) return;

        List<String> lines = readAllLinesRaw();
        if (lines.isEmpty()) lines.add(HEADER);

        boolean replaced = false;

        for (int i = 0; i < lines.size(); i++) {
            String text = safe(lines.get(i));
            if (text.isEmpty() || text.equalsIgnoreCase(HEADER)) continue;

            try {
                Grade existing = Grade.parseTxtLine(text);

                if (same(existing.getStudentId(), grade.getStudentId()) &&
                    same(existing.getCourseId(), grade.getCourseId()) &&
                    existing.getAttempt() == grade.getAttempt()) {

                    lines.set(i, grade.toTxtLine());
                    replaced = true;
                    break;
                }

            } catch (Exception ignored) {
            }
        }

        if (!replaced) {
            lines.add(grade.toTxtLine());
        }

        writeAllLinesRaw(lines);
    }

    public List<Grade> getByStudent(String studentId) {
        studentId = safe(studentId);

        List<Grade> list = new ArrayList<>();
        for (Grade g : readAll()) {
            if (g != null && same(g.getStudentId(), studentId)) {
                list.add(g);
            }
        }
        return list;
    }

    //Returns only the latest attempt per course (no HashMap used).
    public List<Grade> latestByCourse(String studentId) {
        List<Grade> all = getByStudent(studentId);
        List<Grade> latest = new ArrayList<>();

        for (Grade g : all) {
            int index = findCourseIndex(latest, g.getCourseId());

            if (index == -1) {
                latest.add(g);
            } else if (g.getAttempt() > latest.get(index).getAttempt()) {
                latest.set(index, g);
            }
        }
        return latest;
    }

    private int findCourseIndex(List<Grade> list, String courseId) {
        courseId = safe(courseId);

        for (int i = 0; i < list.size(); i++) {
            if (same(list.get(i).getCourseId(), courseId)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> readAllLinesRaw() {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                lines.add(s);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading grades file.", e);
        }

        return lines;
    }

    private void writeAllLinesRaw(List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing grades file.", e);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private boolean same(String a, String b) {
        return safe(a).equalsIgnoreCase(safe(b));
    }
}
