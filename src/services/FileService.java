package services;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileService - basic file operations for CRS.
 * Supports:
 * - text file read/write (CSV, simple logs)
 * - binary object save/load (optional)
 *
 * Note: Authentication logging (binary) is handled in LoginService (auth_log.dat).
 */
public class FileService {

    // Optional standard file names (you may or may not use all of them)
    private static final String USER_FILE = "users.dat";
    private static final String STUDENT_FILE = "students.dat";
    private static final String COURSE_FILE = "courses.dat";
    private static final String RECOVERY_FILE = "recovery_plans.dat";

    // ----------------- Binary Object Save/Load -----------------

    public boolean saveObjectToBinary(Object obj, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving to binary file: " + e.getMessage());
            return false;
        }
    }

    public Object loadObjectFromBinary(String filename) {
        File file = new File(filename);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading from binary file: " + e.getMessage());
            return null;
        }
    }

    // ----------------- Text File Read/Write -----------------

    /**
     * Write one line into a text file.
     * If append=true, it adds to the end; otherwise it overwrites the file.
     */
    public boolean writeTextFile(String line, String filename, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append))) {
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing text file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Read all lines from a text file.
     */
    public List<String> readTextFile(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading text file: " + e.getMessage());
        }
        return lines;
    }

    // ----------------- Utilities -----------------

    public boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public boolean deleteFile(String filename) {
        File file = new File(filename);
        return file.exists() && file.delete();
    }

    // ----------------- Getters for standard file names -----------------

    public static String getUserFile() { return USER_FILE; }
    public static String getStudentFile() { return STUDENT_FILE; }
    public static String getCourseFile() { return COURSE_FILE; }
    public static String getRecoveryFile() { return RECOVERY_FILE; }
}
