package ui;


import app.MainApp;
import services.LoginService;
import services.EmailService;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame - GUI for user authentication
 * Opens MainApp after successful login.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JButton forgotButton;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Course Recovery System - Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Course Recovery System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));
        titlePanel.add(titleLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204)); // Blue - consistent color
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // FIXED: Force color rendering
        loginButton.setOpaque(true);
        loginButton.setContentAreaFilled(true);
        loginButton.setBorderPainted(false);
        formPanel.add(loginButton, gbc);
        
        
        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBorderPainted(false);
        forgotButton.setContentAreaFilled(false);
        forgotButton.setForeground(new Color(0, 102, 204));
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // Bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Default: admin / admin123");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(hintLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(forgotButton);

        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Events
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());
        forgotButton.addActionListener(e -> forgotPassword());

        // Nice UX: clear status when typing
        usernameField.addCaretListener(e -> showStatus(" ", false));
        passwordField.addCaretListener(e -> showStatus(" ", false));
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password", false);
            return;
        }

        // Prevent double-click creating 2 windows
        loginButton.setEnabled(false);

        LoginService loginService = LoginService.getInstance();
        boolean success = loginService.login(username, password);

        if (!success) {
            showStatus("Invalid credentials or account deactivated", false);
            passwordField.setText("");
            loginButton.setEnabled(true);
            return;
        }

        showStatus("Login successful!", true);

        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
            dispose();
        });
    }
    
    
    private void forgotPassword() {
    String username = JOptionPane.showInputDialog(this, "Enter your username:");
    if (username == null || username.trim().isEmpty()) return;

    String email = JOptionPane.showInputDialog(this, "Enter your email:");
    if (email == null || email.trim().isEmpty()) return;

    LoginService loginService = LoginService.getInstance();
    String tempPassword = loginService.recoverPassword(username.trim());

    if (tempPassword == null) {
        JOptionPane.showMessageDialog(this,
                "User not found or deactivated.",
                "Recovery Failed",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    EmailService emailService = new EmailService();
    boolean sent = emailService.sendPasswordResetEmail(email.trim(), username.trim(), tempPassword);

    JOptionPane.showMessageDialog(this,
            sent
                    ? "A temporary password was sent to your email."
                    : "Email failed. Check EmailService setup/libraries.",
            "Password Recovery",
            sent ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
}

    

    private void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ok ? new Color(0, 153, 0) : Color.RED);
    }

}
