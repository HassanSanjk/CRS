package User_Management;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoginManager - Singleton Pattern
 * Handles authentication, authorization, and user management
 * Demonstrates Modularity and Encapsulation
 */
public class LoginManager {
    private static LoginManager instance;
    private Map<String, User> users; // Username -> User mapping
    private User currentUser;
    
    private static final String USERS_FILE = "users.dat";
    private static final String AUTH_LOG_FILE = "auth_log.dat";
    
    // Private constructor - Singleton pattern
    private LoginManager() {
        users = new HashMap<>();
        loadUsers();
    }
    
    // Singleton getInstance method
    public static LoginManager getInstance() {
        if (instance == null) {
            instance = new LoginManager();
        }
        return instance;
    }
    
    /**
     * Load users from file
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // Create default admin user if file doesn't exist
            Admin defaultAdmin = new Admin("admin", "admin123");
            users.put(defaultAdmin.getUsername(), defaultAdmin);
            saveUsers();
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
            System.out.println("Users loaded successfully. Total users: " + users.size());
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            // Create default admin if loading fails
            Admin defaultAdmin = new Admin("admin", "admin123");
            users.put(defaultAdmin.getUsername(), defaultAdmin);
        }
    }
    
    /**
     * Save users to file
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Write login/logout timestamp to binary file
     * MUST use binary format as per requirements
     */
    private void logAuthEvent(String username, String event) {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(AUTH_LOG_FILE, true))) { // Append mode
            
            // Write timestamp and event in binary format
            long timestamp = System.currentTimeMillis();
            dos.writeLong(timestamp); // Binary timestamp
            dos.writeUTF(username);   // Username
            dos.writeUTF(event);      // "LOGIN" or "LOGOUT"
            
            // For readability, also write formatted date
            String formattedDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            dos.writeUTF(formattedDate);
            
            System.out.println("Auth event logged: " + event + " for " + username);
        } catch (Exception e) {
            System.err.println("Error logging auth event: " + e.getMessage());
        }
    }
    
    /**
     * Login method - Authentication and Authorization
     */
    public boolean login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Check if user exists
        User user = users.get(username);
        if (user == null) {
            return false;
        }
        
        // Check if user is active
        if (!user.isActive()) {
            System.out.println("Account is deactivated.");
            return false;
        }
        
        // Verify password
        if (!user.getPassword().equals(password)) {
            return false;
        }
        
        // Successful login
        currentUser = user;
        logAuthEvent(username, "LOGIN");
        return true;
    }
    
    /**
     * Logout method
     */
    public void logout() {
        if (currentUser != null) {
            logAuthEvent(currentUser.getUsername(), "LOGOUT");
            currentUser = null;
        }
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if current user is admin - Role-based access control
     */
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
    
    /**
     * Add a new user (Admin only)
     */
    public boolean addUser(String username, String password, String role) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Check if user already exists
        if (users.containsKey(username)) {
            System.out.println("User already exists.");
            return false;
        }
        
        // Create user based on role
        User newUser;
        if ("ADMIN".equals(role)) {
            newUser = new Admin(username, password);
        } else {
            newUser = new User(username, password, role);
        }
        
        users.put(username, newUser);
        saveUsers();
        return true;
    }
    
    /**
     * Update user information (Admin only)
     */
    public boolean updateUser(String username, String newPassword, String newRole) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(newPassword);
        }
        if (newRole != null && !newRole.trim().isEmpty()) {
            user.setRole(newRole);
        }
        
        saveUsers();
        return true;
    }
    
    /**
     * Deactivate a user (Admin only)
     */
    public boolean deactivateUser(String username) {
        User user = users.get(username);
        if (user == null) {
            return false;
        }
        
        user.setActive(false);
        saveUsers();
        return true;
    }
    
    /**
     * Reset user password (Admin only)
     */
    public boolean resetPassword(String username, String newPassword) {
        User user = users.get(username);
        if (user == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        
        user.setPassword(newPassword);
        saveUsers();
        return true;
    }
    
    /**
     * Get all users (for display in table)
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Read auth log for display/audit purposes
     */
    public List<String> readAuthLog() {
        List<String> logEntries = new ArrayList<>();
        File file = new File(AUTH_LOG_FILE);
        
        if (!file.exists()) {
            return logEntries;
        }
        
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                long timestamp = dis.readLong();
                String username = dis.readUTF();
                String event = dis.readUTF();
                String formattedDate = dis.readUTF();
                
                logEntries.add(formattedDate + " | " + username + " | " + event);
            }
        } catch (EOFException e) {
            // End of file reached
        } catch (Exception e) {
            System.err.println("Error reading auth log: " + e.getMessage());
        }
        
        return logEntries;
    }
}