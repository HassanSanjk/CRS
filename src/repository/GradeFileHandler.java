package repository;

import model.Grade;

import java.io.*;
import java.util.*;

public class GradeFileHandler {

    public static final String HEADER = "StudentID|CourseID|Attempt|Grade";

    private final File file;

    public GradeFileHandler(String path) {
        this.file = new File(path);
        ensureFileAndHeader();
    }

    private void ensureFileAndHeader() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                file.createNewFile();
            }
            // ensure header exists as first line
            List<String> lines = readAllLinesRaw();
            if (lines.isEmpty()) {
                writeAllLinesRaw(Collections.singletonList(HEADER));
            } else {
                String first = lines.get(0).trim();
                if (!first.equalsIgnoreCase(HEADER)) {
                    // prepend header if missing
                    List<String> fixed = new ArrayList<>();
                    fixed.add(HEADER);
                    fixed.addAll(lines);
                    writeAllLinesRaw(fixed);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot init grades file.", e);
        }
    }

    public List<Grade> readAll() {
        List<Grade> out = new ArrayList<>();
        for (String line : readAllLinesRaw()) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            if (t.equalsIgnoreCase(HEADER)) continue;
            out.add(Grade.parseTxtLine(t));
        }
        return out;
    }

    // Upsert = if same StudentID+CourseID+Attempt exists, replace it; else add new
    public void upsert(Grade g) {
        List<String> lines = readAllLinesRaw();
        if (lines.isEmpty()) lines.add(HEADER);

        boolean replaced = false;
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (t.equalsIgnoreCase(HEADER) || t.isEmpty()) continue;

            Grade existing = Grade.parseTxtLine(t);
            if (existing.getStudentId().equals(g.getStudentId()) &&
                existing.getCourseId().equals(g.getCourseId()) &&
                existing.getAttempt() == g.getAttempt()) {

                lines.set(i, g.toTxtLine());
                replaced = true;
                break;
            }
        }
        if (!replaced) lines.add(g.toTxtLine());
        writeAllLinesRaw(lines);
    }

    public List<Grade> getByStudent(String studentId) {
        List<Grade> out = new ArrayList<>();
        for (Grade g : readAll()) if (g.getStudentId().equals(studentId)) out.add(g);
        return out;
    }

    // Latest attempt per course for eligibility/reporting use
    public Map<String, Grade> latestByCourse(String studentId) {
        Map<String, Grade> map = new HashMap<>();
        for (Grade g : getByStudent(studentId)) {
            Grade cur = map.get(g.getCourseId());
            if (cur == null || g.getAttempt() > cur.getAttempt()) map.put(g.getCourseId(), g);
        }
        return map;
    }

    private List<String> readAllLinesRaw() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) lines.add(s);
        } catch (IOException e) {
            throw new RuntimeException("Error reading grades file.", e);
        }
        return lines;
    }

    private void writeAllLinesRaw(List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing grades file.", e);
        }
    }
}
