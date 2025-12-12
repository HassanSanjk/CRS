package User_Management;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - GUI for user authentication
 * Demonstrates GUI implementation with Swing
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    
    public LoginFrame() {
        initComponents();
    }
    
    private void initComponents() {
        // Frame settings
        setTitle("Course Recovery System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Course Recovery System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        titlePanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);
        
        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(loginButton, gbc);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Event listeners
        loginButton.addActionListener(e -> performLogin());
        
        // Allow Enter key to login
        passwordField.addActionListener(e -> performLogin());
        
        // Default credentials hint
        JLabel hintLabel = new JLabel("Default: admin / admin123");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(hintLabel, BorderLayout.PAGE_END);
    }
    
    /**
     * Perform login authentication
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        // Attempt login
        LoginManager loginManager = LoginManager.getInstance();
        boolean success = loginManager.login(username, password);
        
        if (success) {
            statusLabel.setText("Login successful!");
            statusLabel.setForeground(new Color(0, 153, 0));
            
            // Check role and open appropriate window
            User currentUser = loginManager.getCurrentUser();
            
            if ("ADMIN".equals(currentUser.getRole())) {
                // Open User Management Frame for admin
                SwingUtilities.invokeLater(() -> {
                    UserManagementFrame umf = new UserManagementFrame();
                    umf.setVisible(true);
                    dispose(); // Close login frame
                });
            } else {
                // For other roles (OFFICER), show success message
                JOptionPane.showMessageDialog(this,
                        "Welcome " + currentUser.getUsername() + "!\n" +
                        "Role: " + currentUser.getRole() + "\n\n" +
                        "Other modules will be accessible here.",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                // In full system, open appropriate frame based on role
            }
        } else {
            statusLabel.setText("Invalid username or password, or account is deactivated");
            statusLabel.setForeground(Color.RED);
            passwordField.setText(""); // Clear password field
        }
    }
    
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}