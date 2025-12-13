package repository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles student registration after eligibility check.
 * File format:
 * StudentID|Registered|RegisteredAt
 */
public class RegistrationRepository {

    private static final String HEADER = "StudentID|Registered|RegisteredAt";
    private final File file;

    public RegistrationRepository(String path) {
        this.file = new File(path);
        createFileIfMissing();
    }

    // ---------- Public API ----------

    public boolean isRegistered(String studentId) {
        Map<String, RegistrationRow> data = loadAll();
        RegistrationRow row = data.get(studentId);
        return row != null && row.registered;
    }

    public void setRegistered(String studentId, boolean registered) {
        Map<String, RegistrationRow> data = loadAll();

        if (registered) {
            data.put(studentId,
                    new RegistrationRow(true, LocalDateTime.now().toString()));
        } else {
            data.put(studentId,
                    new RegistrationRow(false, ""));
        }

        saveAll(data);
    }

    // ---------- Internal Helpers ----------

    private void createFileIfMissing() {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                file.createNewFile();
            }

            List<String> lines = readAllLines();
            if (lines.isEmpty() || !lines.get(0).equalsIgnoreCase(HEADER)) {
                lines.add(0, HEADER);
                writeAllLines(lines);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize registration file.", e);
        }
    }

    private Map<String, RegistrationRow> loadAll() {
        Map<String, RegistrationRow> map = new HashMap<>();

        for (String line : readAllLines()) {
            if (line.trim().isEmpty() || line.equalsIgnoreCase(HEADER)) continue;

            String[] parts = line.split("\\|");
            if (parts.length < 3) continue;

            String id = parts[0].trim();
            boolean registered = "YES".equalsIgnoreCase(parts[1].trim());
            String date = parts[2].trim();

            map.put(id, new RegistrationRow(registered, date));
        }
        return map;
    }

    private void saveAll(Map<String, RegistrationRow> data) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        List<String> ids = new ArrayList<>(data.keySet());
        Collections.sort(ids);

        for (String id : ids) {
            RegistrationRow r = data.get(id);
            lines.add(id + "|" + (r.registered ? "YES" : "NO") + "|" + r.date);
        }

        writeAllLines(lines);
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

    // ---------- Simple Data Holder ----------
    private static class RegistrationRow {
        boolean registered;
        String date;

        RegistrationRow(boolean registered, String date) {
            this.registered = registered;
            this.date = date;
        }
    }
}
