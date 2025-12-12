package User_Management;

import java.io.Serializable;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Private fields - Encapsulation
    private String username;
    private String password;
    private String role; // "ADMIN" or "OFFICER"
    private boolean active;
    
    // Default constructor
    public User() {
        this.active = true; // Active by default
    }
    
    // Parameterized constructor
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = true;
    }
    
    // Getters and Setters - Encapsulation
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
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