package repository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RegistrationRepository
 * File format:
 * StudentID|Registered|RegisteredAt
 */
public class RegistrationRepository {

    private static final String HEADER = "StudentID|Registered|RegisteredAt";
    private final File file;

    public RegistrationRepository(String path) {
        file = new File(path);
        ensureFileAndHeader();
    }

    // ---------- Public API ----------

    public boolean isRegistered(String studentId) {
        studentId = safe(studentId);
        if (studentId.isEmpty()) return false;

        List<String> lines = readAllLines();
        for (String line : lines) {
            if (line.trim().isEmpty() || line.equalsIgnoreCase(HEADER)) continue;

            RegistrationRow row = parseLine(line);
            if (row == null) continue;

            if (row.studentId.equalsIgnoreCase(studentId)) {
                return row.registered;
            }
        }
        return false;
    }

    public void setRegistered(String studentId, boolean registered) {
        studentId = safe(studentId);
        if (studentId.isEmpty()) return;

        List<String> lines = readAllLines();
        if (lines.isEmpty()) lines.add(HEADER);

        boolean updated = false;
        List<String> out = new ArrayList<>();
        out.add(HEADER);

        for (String line : lines) {
            if (line.trim().isEmpty() || line.equalsIgnoreCase(HEADER)) continue;

            RegistrationRow row = parseLine(line);
            if (row == null) continue;

            if (row.studentId.equalsIgnoreCase(studentId)) {
                String date = registered ? LocalDateTime.now().toString() : "";
                out.add(studentId + "|" + (registered ? "YES" : "NO") + "|" + date);
                updated = true;
            } else {
                out.add(row.studentId + "|" + (row.registered ? "YES" : "NO") + "|" + row.date);
            }
        }

        // if student not found in file, add new row
        if (!updated) {
            String date = registered ? LocalDateTime.now().toString() : "";
            out.add(studentId + "|" + (registered ? "YES" : "NO") + "|" + date);
        }

        writeAllLines(out);
    }

    // ---------- Internal ----------

    private void ensureFileAndHeader() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                file.createNewFile();
            }

            List<String> lines = readAllLines();
            if (lines.isEmpty() || !lines.get(0).equalsIgnoreCase(HEADER)) {
                List<String> fixed = new ArrayList<>();
                fixed.add(HEADER);

                // keep old content if any (but don't duplicate header)
                for (String s : lines) {
                    if (s == null) continue;
                    if (s.trim().equalsIgnoreCase(HEADER)) continue;
                    if (!s.trim().isEmpty()) fixed.add(s);
                }

                writeAllLines(fixed);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize registration file.", e);
        }
    }

    private RegistrationRow parseLine(String line) {
        if (line == null) return null;

        String[] parts = line.split("\\|", -1);
        if (parts.length < 3) return null;

        String id = safe(parts[0]);
        String regText = safe(parts[1]);
        String date = safe(parts[2]);

        if (id.isEmpty()) return null;

        boolean reg = regText.equalsIgnoreCase("YES");
        return new RegistrationRow(id, reg, date);
    }

    private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                lines.add(s);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading registration file.", e);
        }
        return lines;
    }

    private void writeAllLines(List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing registration file.", e);
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    // small data holder
    private static class RegistrationRow {
        String studentId;
        boolean registered;
        String date;

        RegistrationRow(String studentId, boolean registered, String date) {
            this.studentId = studentId;
            this.registered = registered;
            this.date = date;
        }
    }
}
