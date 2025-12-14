package ui;

import app.MainApp;
import model.User;
import services.EmailService;
import services.LoginService;

import javax.swing.*;
import java.awt.*;

/**
 * LoginFrame
 * Simple login window for CRS.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JButton forgotButton;

    private final LoginService loginService;   // reuse singleton
    private final EmailService emailService;   // reuse service

    public LoginFrame() {
        loginService = LoginService.getInstance();
        emailService = new EmailService();
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
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // force color on some Look and Feels
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

        // Clear status when typing
        usernameField.addCaretListener(e -> showStatus(" ", false));
        passwordField.addCaretListener(e -> showStatus(" ", false));
    }

    private void performLogin() {
        String username = safe(usernameField.getText());
        String password = safe(new String(passwordField.getPassword()));

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password", false);
            return;
        }

        loginButton.setEnabled(false);

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
        username = safe(username);
        if (username.isEmpty()) return;

        User u = loginService.findUser(username);
        if (u == null || !u.isActive()) {
            JOptionPane.showMessageDialog(this,
                    "User not found or deactivated.",
                    "Recovery Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String emailInput = JOptionPane.showInputDialog(this, "Enter your email:");
        emailInput = safe(emailInput);
        if (emailInput.isEmpty()) return;

        // small safety check: if system already has email, must match
        String savedEmail = safe(u.getEmail());
        if (!savedEmail.isEmpty() && !savedEmail.equalsIgnoreCase(emailInput)) {
            JOptionPane.showMessageDialog(this,
                    "Email does not match the email on record.",
                    "Recovery Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tempPassword = loginService.recoverPassword(username);
        if (tempPassword == null) {
            JOptionPane.showMessageDialog(this,
                    "Recovery failed. Please try again.",
                    "Recovery Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean sent = emailService.sendPasswordResetEmail(emailInput, username, tempPassword);

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

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
