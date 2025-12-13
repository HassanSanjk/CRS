package ui;


import model.User;
import services.LoginService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * UserManagementFrame - GUI for managing users (Admin only)
 * Features:
 * - Add user
 * - Update user
 * - Deactivate / Activate user
 * - Reset password
 * - View auth log (binary file)
 */
public class UserManagementFrame extends JFrame {

    private JTable userTable;
    private DefaultTableModel tableModel;

    private JButton addButton, updateButton, deactivateButton, activateButton, resetPasswordButton;
    private JButton logoutButton, viewLogButton;

    private final LoginService loginService;

    public UserManagementFrame() {
        loginService = LoginService.getInstance();

        // Role-based access control
        if (!loginService.isAdmin()) {
            JOptionPane.showMessageDialog(null,
                    "Access Denied. Admin privileges required.",
                    "Authorization Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setTitle("User Management - Course Recovery System");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));

        User currentUser = loginService.getCurrentUser();
        JLabel userInfoLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userInfoLabel, BorderLayout.EAST);

        // Table
        String[] columns = {"Username", "Role", "Email", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        userTable.getTableHeader().setBackground(new Color(0, 102, 204));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("User Accounts"));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        addButton = createStyledButton("Add User", new Color(34, 139, 34));
        updateButton = createStyledButton("Update User", new Color(255, 140, 0));
        deactivateButton = createStyledButton("Deactivate", new Color(220, 20, 60));
        activateButton = createStyledButton("Activate", new Color(46, 139, 87));
        resetPasswordButton = createStyledButton("Reset Password", new Color(70, 130, 180));
        viewLogButton = createStyledButton("View Auth Log", new Color(128, 0, 128));
        logoutButton = createStyledButton("Logout", new Color(105, 105, 105));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deactivateButton);
        buttonPanel.add(activateButton);
        buttonPanel.add(resetPasswordButton);
        buttonPanel.add(viewLogButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Events
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deactivateButton.addActionListener(e -> setUserActive(false));
        activateButton.addActionListener(e -> setUserActive(true));
        resetPasswordButton.addActionListener(e -> resetPassword());
        viewLogButton.addActionListener(e -> viewAuthLog());
        logoutButton.addActionListener(e -> logout());
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = loginService.getAllUsers();

        for (User user : users) {
            String status = user.isActive() ? "Active" : "Deactivated";
            tableModel.addRow(new Object[]{
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail(),
                    status
            });
        }
    }

    private void addUser() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        String[] roles = {"OFFICER", "ADMIN"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add New User", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = loginService.addUser(username, password, role, email);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "User added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to add user.\n(Username may already exist OR you are not admin.)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to update.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        String[] roles = {"OFFICER", "ADMIN"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        panel.add(new JLabel("New Password (leave blank to keep):"));
        panel.add(passwordField);
        panel.add(new JLabel("New Email (leave blank to keep):"));
        panel.add(emailField);
        panel.add(new JLabel("New Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Update User: " + username, JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        boolean success = loginService.updateUser(username, password, role, email);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "User updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update user.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setUserActive(boolean active) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);

        if (active && "Active".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "User is already active.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!active && "Deactivated".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "User is already deactivated.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                (active ? "Activate" : "Deactivate") + " user: " + username + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = active
                ? loginService.activateUser(username)
                : loginService.deactivateUser(username);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "User " + (active ? "activated" : "deactivated") + " successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Operation failed.\n(You may be trying to deactivate your own account.)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to reset password.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 0);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Reset Password for: " + username, JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String password = new String(newPasswordField.getPassword()).trim();
        String confirm = new String(confirmPasswordField.getPassword()).trim();

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Password cannot be empty!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = loginService.resetPassword(username, password);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Password reset successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to reset password.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAuthLog() {
        List<String> logEntries = loginService.readAuthLog();

        JTextArea textArea = new JTextArea(20, 55);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        if (logEntries.isEmpty()) {
            textArea.setText("No authentication log entries found.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("AUTHENTICATION LOG\n");
            sb.append("==========================================\n\n");
            for (String entry : logEntries) {
                sb.append(entry).append("\n");
            }
            textArea.setText(sb.toString());
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane,
                "Authentication Log (Binary File)",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        loginService.logout();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        });
    }
}
