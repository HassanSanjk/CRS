package crs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileService class handles all file operations for the CRS system
 * Supports both text and binary file reading/writing
 */
public class FileService {
    
    // File paths for different data types
    private static final String USER_FILE = "users.dat";
    private static final String STUDENT_FILE = "students.dat";
    private static final String COURSE_FILE = "courses.dat";
    private static final String RECOVERY_FILE = "recovery_plans.dat";
    private static final String LOGIN_LOG_FILE = "login_logs.dat";
    
    /**
     * Saves an object to a binary file
     * @param obj The object to save
     * @param filename The file to save to
     * @return true if successful, false otherwise
     */
    public boolean saveObjectToBinary(Object obj, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(obj);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving to binary file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads an object from a binary file
     * @param filename The file to load from
     * @return The loaded object, or null if error
     */
    public Object loadObjectFromBinary(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            return ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename + " (will be created on save)");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading from binary file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Saves a list of objects to binary file
     * @param list The list to save
     * @param filename The file to save to
     * @return true if successful
     */
    public boolean saveListToBinary(List<?> list, String filename) {
        return saveObjectToBinary(list, filename);
    }
    
    /**
     * Loads a list of objects from binary file
     * @param filename The file to load from
     * @return The list, or empty list if error
     */
    @SuppressWarnings("unchecked")
    public List<Object> loadListFromBinary(String filename) {
        Object obj = loadObjectFromBinary(filename);
        if (obj instanceof List) {
            return (List<Object>) obj;
        }
        return new ArrayList<>();
    }
    
    /**
     * Writes text data to a text file
     * @param data The text to write
     * @param filename The file to write to
     * @param append Whether to append or overwrite
     * @return true if successful
     */
    public boolean writeTextFile(String data, String filename, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filename, append))) {
            writer.write(data);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to text file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reads all lines from a text file
     * @param filename The file to read from
     * @return List of lines, or empty list if error
     */
    public List<String> readTextFile(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading text file: " + e.getMessage());
        }
        return lines;
    }
    
    /**
     * Logs login/logout timestamps to binary file
     * @param username The username
     * @param action "LOGIN" or "LOGOUT"
     * @param timestamp The timestamp
     * @return true if successful
     */
    public boolean logLoginActivity(String username, String action, long timestamp) {
        try {
            // Load existing logs
            List<String> logs = loadListFromBinary(LOGIN_LOG_FILE).stream()
                    .map(Object::toString)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            
            // Add new log entry
            String logEntry = username + "," + action + "," + timestamp;
            logs.add(logEntry);
            
            // Save back to file
            return saveListToBinary(new ArrayList<>(logs), LOGIN_LOG_FILE);
        } catch (Exception e) {
            System.err.println("Error logging activity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a file exists
     * @param filename The file to check
     * @return true if file exists
     */
    public boolean fileExists(String filename) {
        File file = new File(filename);
        return file.exists();
    }
    
    /**
     * Deletes a file
     * @param filename The file to delete
     * @return true if successful
     */
    public boolean deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * Gets the standard file paths used by the system
     */
    public static String getUserFile() {
        return USER_FILE;
    }
    
    public static String getStudentFile() {
        return STUDENT_FILE;
    }
    
    public static String getCourseFile() {
        return COURSE_FILE;
    }
    
    public static String getRecoveryFile() {
        return RECOVERY_FILE;
    }
    
    public static String getLoginLogFile() {
        return LOGIN_LOG_FILE;
    }
}