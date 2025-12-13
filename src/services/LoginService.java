package services;


import model.Admin;
import model.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * LoginService (Singleton)
 * - Authentication (login/logout)
 * - Authorization (admin-only operations)
 * - User management (add/update/deactivate/reset)
 * - Logs login/logout timestamps in binary form (DataOutputStream)
 */
public class LoginService {

    private static LoginService instance;

    private Map<String, User> users;   // username -> user
    private User currentUser;

    private static final String USERS_FILE = "users.dat";
    private static final String AUTH_LOG_FILE = "auth_log.dat";

    private LoginService() {
        users = new HashMap<>();
        loadUsers();
        ensureDefaultAdmin();
    }

    public static LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    // -------------------- Authentication --------------------

    public boolean login(String username, String password) {
        username = clean(username);
        password = clean(password);

        if (username.isEmpty() || password.isEmpty()) return false;

        User user = users.get(username);
        if (user == null) return false;

        if (!user.isActive()) {
            System.out.println("Account is deactivated.");
            return false;
        }

        if (!password.equals(user.getPassword())) return false;

        currentUser = user;
        logAuthEvent(username, "LOGIN");
        return true;
    }

    public void logout() {
        if (currentUser != null) {
            logAuthEvent(currentUser.getUsername(), "LOGOUT");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.hasRole("ADMIN");
    }

    // -------------------- User Management (Admin only) --------------------

    public boolean addUser(String username, String password, String role, String email) {
        if (!isAdmin()) return false;

        username = clean(username);
        password = clean(password);
        role = clean(role);
        email = (email == null) ? "" : email.trim();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) return false;
        if (users.containsKey(username)) return false;

        User newUser;
        if ("ADMIN".equalsIgnoreCase(role)) {
            newUser = new Admin(username, password, email);
        } else {
            newUser = new User(username, password, role.toUpperCase(), email);
        }

        users.put(username, newUser);
        saveUsers();
        return true;
    }

    // Backward compatible (your old calls still work)
    public boolean addUser(String username, String password, String role) {
        return addUser(username, password, role, "");
    }

    public boolean updateUser(String username, String newPassword, String newRole, String newEmail) {
        if (!isAdmin()) return false;

        username = clean(username);
        newPassword = clean(newPassword);
        newRole = clean(newRole);
        newEmail = (newEmail == null) ? "" : newEmail.trim();

        User user = users.get(username);
        if (user == null) return false;

        if (!newPassword.isEmpty()) user.setPassword(newPassword);
        if (!newRole.isEmpty()) user.setRole(newRole.toUpperCase());
        if (!newEmail.isEmpty()) user.setEmail(newEmail);

        saveUsers();
        return true;
    }

    // Backward compatible
    public boolean updateUser(String username, String newPassword, String newRole) {
        return updateUser(username, newPassword, newRole, "");
    }

    public boolean deactivateUser(String username) {
        if (!isAdmin()) return false;

        username = clean(username);
        User user = users.get(username);
        if (user == null) return false;

        // Prevent locking yourself out by deactivating your own account (optional safety)
        if (currentUser != null && username.equals(currentUser.getUsername())) {
            return false;
        }

        user.deactivate();
        saveUsers();
        return true;
    }

    public boolean activateUser(String username) {
        if (!isAdmin()) return false;

        username = clean(username);
        User user = users.get(username);
        if (user == null) return false;

        user.activate();
        saveUsers();
        return true;
    }

    public boolean resetPassword(String username, String newPassword) {
        if (!isAdmin()) return false;

        username = clean(username);
        newPassword = clean(newPassword);

        if (username.isEmpty() || newPassword.isEmpty()) return false;

        User user = users.get(username);
        if (user == null) return false;

        user.setPassword(newPassword);
        saveUsers();
        return true;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // Helpful for login as “other user”
    public User findUser(String username) {
        username = clean(username);
        return users.get(username);
    }

    // -------------------- Binary Auth Log --------------------

    private void logAuthEvent(String username, String event) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(AUTH_LOG_FILE, true))) {
            long timestamp = System.currentTimeMillis(); // binary timestamp
            dos.writeLong(timestamp);
            dos.writeUTF(username);
            dos.writeUTF(event);

            // keep readable date (optional but helpful for demo)
            String formattedDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            dos.writeUTF(formattedDate);

        } catch (Exception e) {
            System.err.println("Error logging auth event: " + e.getMessage());
        }
    }

    public List<String> readAuthLog() {
        List<String> logEntries = new ArrayList<>();
        File file = new File(AUTH_LOG_FILE);
        if (!file.exists()) return logEntries;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (dis.available() > 0) {
                dis.readLong(); // timestamp (binary) - not displayed
                String username = dis.readUTF();
                String event = dis.readUTF();
                String formattedDate = dis.readUTF();
                logEntries.add(formattedDate + " | " + username + " | " + event);
            }
        } catch (EOFException ignored) {
        } catch (Exception e) {
            System.err.println("Error reading auth log: " + e.getMessage());
        }
        return logEntries;
    }

    // -------------------- Persistence --------------------

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                users = (Map<String, User>) obj;
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            users = new HashMap<>();
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void ensureDefaultAdmin() {
        if (!users.containsKey("admin")) {
            Admin defaultAdmin = new Admin("admin", "admin123", "admin@crs.com");
            users.put(defaultAdmin.getUsername(), defaultAdmin);
            saveUsers();
        }
    }
    
    public String recoverPassword(String username) {
    User user = users.get(username);
    if (user == null || !user.isActive()) return null;

    // Generate a simple temporary password
    String tempPass = "Temp" + (int)(Math.random() * 9000 + 1000);

    user.setPassword(tempPass);
    saveUsers(); // save to users.dat
    return tempPass;
}
    
    // -------------------- Helpers --------------------

    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }
}
