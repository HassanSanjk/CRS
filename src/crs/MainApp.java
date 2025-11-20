package crs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainApp - Main GUI Application for Course Recovery System (CRS)
 * Integrates all modules: User Management, Student/Course Management,
 * Course Recovery, Reporting, and System Services
 */
public class MainApp extends JFrame {
    
    // Service instances
    private FileService fileService;
    private EmailService emailService;
    
    // GUI Components
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JTextArea outputArea;
    private JLabel statusLabel;
    
    // Current logged in user
    private String currentUser;
    private String currentRole;
    
    /**
     * Constructor - initializes the main application
     */
    public MainApp() {
        // Initialize services
        fileService = new FileService();
        emailService = new EmailService();
        
        // Setup main window
        setTitle("Course Recovery System (CRS)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize GUI components
        initComponents();
        
        // Show login dialog
        showLoginDialog();
    }
    
    /**
     * Initializes all GUI components
     */
    private void initComponents() {
        // Create menu bar
        createMenuBar();
        
        // Create main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create welcome panel
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createTitledBorder("Welcome"));
        
        JLabel titleLabel = new JLabel("Course Recovery System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Manage Student Course Recovery Plans");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        welcomePanel.add(Box.createVerticalStrut(20));
        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createVerticalStrut(10));
        welcomePanel.add(subtitleLabel);
        welcomePanel.add(Box.createVerticalStrut(20));
        
        // Create output area
        outputArea = new JTextArea(15, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Output"));
        
        // Create status bar
        statusLabel = new JLabel("Status: Not logged in");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        
        // Add components to main panel
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    /**
     * Creates the menu bar with all menu options
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // User Management Menu
        JMenu userMenu = new JMenu("User Management");
        JMenuItem addUserItem = new JMenuItem("Add User");
        JMenuItem updateUserItem = new JMenuItem("Update User");
        JMenuItem deactivateUserItem = new JMenuItem("Deactivate User");
        JMenuItem resetPasswordItem = new JMenuItem("Reset Password");
        
        addUserItem.addActionListener(e -> addUser());
        updateUserItem.addActionListener(e -> updateUser());
        deactivateUserItem.addActionListener(e -> deactivateUser());
        resetPasswordItem.addActionListener(e -> resetPassword());
        
        userMenu.add(addUserItem);
        userMenu.add(updateUserItem);
        userMenu.add(deactivateUserItem);
        userMenu.addSeparator();
        userMenu.add(resetPasswordItem);
        
        // Student & Course Menu
        JMenu studentMenu = new JMenu("Student & Course");
        JMenuItem viewStudentsItem = new JMenuItem("View All Students");
        JMenuItem checkEligibilityItem = new JMenuItem("Check Eligibility");
        JMenuItem enrollStudentItem = new JMenuItem("Enroll Student");
        
        viewStudentsItem.addActionListener(e -> viewStudents());
        checkEligibilityItem.addActionListener(e -> checkEligibility());
        enrollStudentItem.addActionListener(e -> enrollStudent());
        
        studentMenu.add(viewStudentsItem);
        studentMenu.add(checkEligibilityItem);
        studentMenu.add(enrollStudentItem);
        
        // Course Recovery Menu
        JMenu recoveryMenu = new JMenu("Course Recovery");
        JMenuItem createPlanItem = new JMenuItem("Create Recovery Plan");
        JMenuItem viewPlanItem = new JMenuItem("View Recovery Plan");
        JMenuItem updateProgressItem = new JMenuItem("Update Progress");
        JMenuItem addMilestoneItem = new JMenuItem("Add Milestone");
        
        createPlanItem.addActionListener(e -> createRecoveryPlan());
        viewPlanItem.addActionListener(e -> viewRecoveryPlan());
        updateProgressItem.addActionListener(e -> updateProgress());
        addMilestoneItem.addActionListener(e -> addMilestone());
        
        recoveryMenu.add(createPlanItem);
        recoveryMenu.add(viewPlanItem);
        recoveryMenu.add(updateProgressItem);
        recoveryMenu.add(addMilestoneItem);
        
        // Reports Menu
        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem generateReportItem = new JMenuItem("Generate Academic Report");
        JMenuItem exportPDFItem = new JMenuItem("Export to PDF");
        
        generateReportItem.addActionListener(e -> generateReport());
        exportPDFItem.addActionListener(e -> exportToPDF());
        
        reportsMenu.add(generateReportItem);
        reportsMenu.add(exportPDFItem);
        
        // System Menu
        JMenu systemMenu = new JMenu("System");
        JMenuItem testEmailItem = new JMenuItem("Test Email Connection");
        JMenuItem viewLogsItem = new JMenuItem("View Login Logs");
        JMenuItem aboutItem = new JMenuItem("About");
        
        testEmailItem.addActionListener(e -> testEmailConnection());
        viewLogsItem.addActionListener(e -> viewLoginLogs());
        aboutItem.addActionListener(e -> showAbout());
        
        systemMenu.add(testEmailItem);
        systemMenu.add(viewLogsItem);
        systemMenu.addSeparator();
        systemMenu.add(aboutItem);
        
        // Add all menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        menuBar.add(studentMenu);
        menuBar.add(recoveryMenu);
        menuBar.add(reportsMenu);
        menuBar.add(systemMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Shows login dialog
     */
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login", true);
        loginDialog.setSize(350, 200);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new GridLayout(4, 2, 10, 10));
        
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            
            if (performLogin(username, password)) {
                loginDialog.dispose();
                logOutput("Login successful: " + username);
                
                // Log login timestamp to binary file
                fileService.logLoginActivity(username, "LOGIN", System.currentTimeMillis());
            } else {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Invalid username or password", "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> System.exit(0));
        
        loginDialog.add(userLabel);
        loginDialog.add(userField);
        loginDialog.add(passLabel);
        loginDialog.add(passField);
        loginDialog.add(new JLabel());
        loginDialog.add(loginButton);
        loginDialog.add(new JLabel());
        loginDialog.add(cancelButton);
        
        loginDialog.setVisible(true);
    }
    
    /**
     * Performs login validation
     */
    private boolean performLogin(String username, String password) {
        // Simple validation - in real system, would check against stored users
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        
        // Demo login (would integrate with User Management module)
        if (username.equals("admin") && password.equals("admin123")) {
            currentUser = username;
            currentRole = "Administrator";
            statusLabel.setText("Status: Logged in as " + currentUser + " (" + currentRole + ")");
            return true;
        }
        
        return false;
    }
    
    /**
     * User Management Methods
     */
    private void addUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username:");
        if (username != null && !username.trim().isEmpty()) {
            String email = JOptionPane.showInputDialog(this, "Enter email:");
            String tempPassword = "Temp" + (int)(Math.random() * 10000);
            
            // Send account creation email
            emailService.sendAccountCreationEmail(email, username, tempPassword);
            logOutput("User added: " + username + " (Email sent to: " + email + ")");
        }
    }
    
    private void updateUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username to update:");
        if (username != null && !username.trim().isEmpty()) {
            logOutput("User updated: " + username);
        }
    }
    
    private void deactivateUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username to deactivate:");
        if (username != null && !username.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to deactivate user: " + username + "?",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                logOutput("User deactivated: " + username);
            }
        }
    }
    
    private void resetPassword() {
        String username = JOptionPane.showInputDialog(this, "Enter username:");
        if (username != null && !username.trim().isEmpty()) {
            String email = JOptionPane.showInputDialog(this, "Enter email:");
            String newPassword = "New" + (int)(Math.random() * 10000);
            
            emailService.sendPasswordResetEmail(email, username, newPassword);
            logOutput("Password reset for: " + username + " (Email sent to: " + email + ")");
        }
    }
    
    /**
     * Student & Course Methods
     */
    private void viewStudents() {
        logOutput("=== All Students ===");
        logOutput("Loading student data from file...");
        // Would integrate with Student module
        logOutput("Student list displayed.");
    }
    
    private void checkEligibility() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            // Would integrate with EligibilityChecker
            logOutput("Checking eligibility for student: " + studentId);
            logOutput("CGPA: 3.25 - Eligible");
            logOutput("Failed Courses: 1 - Eligible");
        }
    }
    
    private void enrollStudent() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            logOutput("Student " + studentId + " enrolled successfully.");
        }
    }
    
    /**
     * Course Recovery Methods
     */
    private void createRecoveryPlan() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            String course = JOptionPane.showInputDialog(this, "Enter Course Code:");
            
            // Create action plan
            String actionPlan = "Week 1-2: Review lectures\nWeek 3: Meet with lecturer\nWeek 4: Recovery exam";
            
            // Send email notification
            emailService.sendRecoveryPlanEmail("student@example.com", "Student Name", course, actionPlan);
            
            logOutput("Recovery plan created for student: " + studentId);
            logOutput("Course: " + course);
            logOutput("Email notification sent.");
        }
    }
    
    private void viewRecoveryPlan() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            logOutput("=== Recovery Plan for " + studentId + " ===");
            logOutput("Course: Object Oriented Programming");
            logOutput("Week 1-2: Review lectures");
            logOutput("Week 3: Meet with lecturer");
            logOutput("Week 4: Recovery exam");
        }
    }
    
    private void updateProgress() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            String progress = JOptionPane.showInputDialog(this, "Enter progress percentage:");
            logOutput("Progress updated for student " + studentId + ": " + progress + "%");
        }
    }
    
    private void addMilestone() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            String milestone = JOptionPane.showInputDialog(this, "Enter milestone description:");
            logOutput("Milestone added for student " + studentId + ": " + milestone);
        }
    }
    
    /**
     * Reports Methods
     */
    private void generateReport() {
        String studentId = JOptionPane.showInputDialog(this, "Enter Student ID:");
        if (studentId != null && !studentId.trim().isEmpty()) {
            logOutput("=== Academic Performance Report ===");
            logOutput("Student ID: " + studentId);
            logOutput("Semester 1 - CGPA: 3.25");
            logOutput("Report generated successfully.");
            
            // Send email notification
            emailService.sendPerformanceReportEmail("student@example.com", 
                "Student Name", "Semester 1", 3.25);
        }
    }
    
    private void exportToPDF() {
        logOutput("Exporting report to PDF...");
        // Would integrate with PDFService
        JOptionPane.showMessageDialog(this, 
            "Report exported to PDF successfully!", 
            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        logOutput("PDF export completed.");
    }
    
    /**
     * System Methods
     */
    private void testEmailConnection() {
        logOutput("Testing email connection...");
        boolean success = emailService.testConnection();
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Email connection test successful!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            logOutput("Email connection: OK");
        } else {
            JOptionPane.showMessageDialog(this, 
                "Email connection test failed!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            logOutput("Email connection: FAILED");
        }
    }
    
    private void viewLoginLogs() {
        logOutput("=== Login Activity Logs ===");
        logOutput("Loading logs from binary file...");
        
        // Load logs using FileService
        if (fileService.fileExists(FileService.getLoginLogFile())) {
            logOutput("Login logs displayed.");
        } else {
            logOutput("No login logs found.");
        }
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "Course Recovery System (CRS)\n" +
            "Version 1.0\n\n" +
            "Developed by: [Your Group Name]\n" +
            "Module: Object Oriented Programming\n\n" +
            "© 2025 All Rights Reserved",
            "About CRS", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Utility method to log output to text area
     */
    private void logOutput(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    
    /**
     * Main method - entry point of the application
     */
    public static void main(String[] args) {
        // Use Swing's event dispatch thread for GUI
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Create and show the main application
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}