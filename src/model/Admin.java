package model;

/**
 * Admin class
 * Demonstrates inheritance from User
 * Represents an administrator account in the system
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    // Default constructor
    public Admin() {
        super();
        setRole("ADMIN");
    }

    // Constructor without email
    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }

    // Constructor with email (recommended)
    public Admin(String username, String password, String email) {
        super(username, password, "ADMIN", email);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "username='" + getUsername() + '\'' +
                ", role='" + getRole() + '\'' +
                ", active=" + isActive() +
                '}';
    }
}
