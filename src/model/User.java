package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // Encapsulation
    private String username;
    private String password;
    private String role;      // "ADMIN" or "OFFICER" (or "STUDENT" later)
    private String email;     // for email notifications + password recovery
    private boolean active;   // for deactivate feature

    // Default constructor
    public User() {
        this.active = true;
    }

    // Constructor (without email) - keeps your old calls working
    public User(String username, String password, String role) {
        this(username, password, role, "");
    }

    // Constructor (with email) - recommended
    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = (email == null) ? "" : email.trim();
        this.active = true;
    }

    // Getters/Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    // Keep simple: allow updating password directly
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = (email == null) ? "" : email.trim(); }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Convenience methods (beginner-friendly)
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public boolean hasRole(String role) {
        return role != null && this.role != null && this.role.equalsIgnoreCase(role.trim());
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
