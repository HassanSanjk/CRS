package model;

import java.io.Serializable;

/*
 * User class:
 * - Stores login info (username/password)
 * - Stores role (ADMIN/OFFICER)
 * - Stores email for password recovery
 * - Can be activated/deactivated
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String role;
    private String email;
    private boolean active;

    // Default constructor (needed for some Java file loading)
    public User() {
        active = true;
        email = "";
    }

    // Old constructor (keeps older code working)
    public User(String username, String password, String role) {
        this(username, password, role, "");
    }

    // Main constructor
    public User(String username, String password, String role, String email) {
        this.username = clean(username);
        this.password = clean(password);
        this.role = clean(role);
        this.email = clean(email);
        this.active = true;
    }

    // ---------- Getters / Setters ----------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = clean(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = clean(password);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = clean(role);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = clean(email);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ---------- Simple helpers ----------

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    public boolean hasRole(String checkRole) {
        if (checkRole == null || role == null) return false;
        return role.equalsIgnoreCase(checkRole.trim());
    }

    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }

    @Override
    public String toString() {
        return username + " (" + role + ") - " + (active ? "Active" : "Deactivated");
    }
}
