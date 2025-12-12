package User_Management;

/**
 * Admin class - Demonstrates Inheritance
 * Extends User class and adds administrative capabilities
 */

public class Admin extends User {
    private static final long serialVersionUID = 1L;
    
    // Default constructor
    public Admin() {
        super();
        setRole("ADMIN"); // Admins always have ADMIN role
    }
    
    // Parameterized constructor
    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }
    
    /**
     * Add a new user to the system
     * Demonstrates admin-specific functionality
     */
    public boolean addUser(String username, String password, String role) {
        try {
            LoginManager lm = LoginManager.getInstance();
            return lm.addUser(username, password, role);
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update an existing user's information
     */
    public boolean updateUser(String username, String newPassword, String newRole) {
        try {
            LoginManager lm = LoginManager.getInstance();
            return lm.updateUser(username, newPassword, newRole);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deactivate a user account
     */
    public boolean deactivateUser(String username) {
        try {
            LoginManager lm = LoginManager.getInstance();
            return lm.deactivateUser(username);
        } catch (Exception e) {
            System.err.println("Error deactivating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset a user's password
     */
    public boolean resetPassword(String username, String newPassword) {
        try {
            LoginManager lm = LoginManager.getInstance();
            return lm.resetPassword(username, newPassword);
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
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