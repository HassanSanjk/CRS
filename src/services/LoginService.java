package services;

import model.Admin;
import model.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginService (Singleton)
 * - login/logout
 * - admin-only user management
 * - saves users to file
 * - logs login/logout with binary timestamp
 */
public class LoginService {

    private static LoginService instance;

    private List<User> users;
    private User currentUser;

    private static final String USERS_FILE = "users.dat";
    private static final String AUTH_LOG_FILE = "auth_log.dat";

    private LoginService() {
        users = new ArrayList<>();
        loadUsers();
        ensureDefaultAdmin();
    }

    public static LoginService getInstance() {
        if (instance == null) instance = new LoginService();
        return instance;
    }

    // -------------------- Authentication --------------------

    public boolean login(String username, String password) {
        username = clean(username);
        password = clean(password);

        if (username.isEmpty() || password.isEmpty()) return false;

        User user = findUser(username);
        if (user == null) return false;

        if (!user.isActive()) return false;
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
        role = clean(role).toUpperCase();
        email = (email == null) ? "" : email.trim();

        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) return false;
        if (findUser(username) != null) return false; // prevent duplicate

        User newUser;
        if ("ADMIN".equals(role)) {
            newUser = new Admin(username, password, email);
        } else {
            newUser = new User(username, password, role, email);
        }

        users.add(newUser);
        saveUsers();
        return true;
    }

    // old calls still work
    public boolean addUser(String username, String password, String role) {
        return addUser(username, password, role, "");
    }

    public boolean updateUser(String username, String newPassword, String newRole, String newEmail) {
        if (!isAdmin()) return false;

        username = clean(username);
        newPassword = clean(newPassword);
        newRole = clean(newRole).toUpperCase();
        newEmail = (newEmail == null) ? "" : newEmail.trim();

        User user = findUser(username);
        if (user == null) return false;

        if (!newPassword.isEmpty()) user.setPassword(newPassword);
        if (!newRole.isEmpty()) user.setRole(newRole);
        if (!newEmail.isEmpty()) user.setEmail(newEmail);

        saveUsers();
        return true;
    }

    public boolean updateUser(String username, String newPassword, String newRole) {
        return updateUser(username, newPassword, newRole, "");
    }

    public boolean deactivateUser(String username) {
        if (!isAdmin()) return false;

        username = clean(username);
        User user = findUser(username);
        if (user == null) return false;

        // don't let admin deactivate themselves
        if (currentUser != null && username.equalsIgnoreCase(currentUser.getUsername())) {
            return false;
        }

        user.deactivate();
        saveUsers();
        return true;
    }

    public boolean activateUser(String username) {
        if (!isAdmin()) return false;

        username = clean(username);
        User user = findUser(username);
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

        User user = findUser(username);
        if (user == null) return false;

        user.setPassword(newPassword);
        saveUsers();
        return true;
    }

    public String recoverPassword(String username) {
        username = clean(username);

        User user = findUser(username);
        if (user == null || !user.isActive()) return null;

        // simple temp password
        String tempPass = "Temp" + (int) (Math.random() * 9000 + 1000);

        user.setPassword(tempPass);
        saveUsers();
        return tempPass;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // -------------------- Binary Auth Log --------------------

    private void logAuthEvent(String username, String event) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(AUTH_LOG_FILE, true))) {

            long timestamp = System.currentTimeMillis(); // binary time
            dos.writeLong(timestamp);
            dos.writeUTF(username);
            dos.writeUTF(event);

            // readable date for viewing
            String formattedDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            dos.writeUTF(formattedDate);

        } catch (IOException e) {
            System.err.println("Auth log error: " + e.getMessage());
        }
    }

    public List<String> readAuthLog() {
        List<String> logEntries = new ArrayList<>();
        File file = new File(AUTH_LOG_FILE);
        if (!file.exists()) return logEntries;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            while (true) {
                dis.readLong(); // timestamp saved (binary)
                String username = dis.readUTF();
                String event = dis.readUTF();
                String formattedDate = dis.readUTF();

                logEntries.add(formattedDate + " | " + username + " | " + event);
            }
        } catch (EOFException ignored) {
            // end of file
        } catch (IOException e) {
            System.err.println("Read log error: " + e.getMessage());
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

            // support older versions if needed
            if (obj instanceof List) {
                users = (List<User>) obj;
            }

        } catch (Exception e) {
            System.err.println("Load users error: " + e.getMessage());
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Save users error: " + e.getMessage());
        }
    }

    private void ensureDefaultAdmin() {
        if (findUser("admin") == null) {
            users.add(new Admin("admin", "admin123", "admin@crs.com"));
            saveUsers();
        }
    }

    // -------------------- Helpers --------------------

    public User findUser(String username) {
        username = clean(username);

        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }
}
