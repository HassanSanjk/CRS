package model;


/*
  * Admin
  - Special type of User 
  - Always has role = ADMIN 
  - Inherits everything from User
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

    // Constructor with email
    public Admin(String username, String password, String email) {
        super(username, password, "ADMIN", email);
    }

    @Override
    public String toString() {
        return getUsername() + " (ADMIN) - " + (isActive() ? "Active" : "Deactivated");
    }
}
