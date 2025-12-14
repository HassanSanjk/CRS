package ui;

import model.User;
import services.LoginService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * UserManagementFrame (Admin only)
 * Add/Update/Activate/Deactivate/Reset + view auth log.
 */
public class UserManagementFrame extends JFrame {

    private static final Color MAIN_BLUE = new Color(0, 102, 204);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);

    private JTable userTable;
    private DefaultTableModel tableModel;

    private JButton addButton, updateButton, deactivateButton, activateButton, resetPasswordButton;
    private JButton logoutButton, viewLogButton;

    private final LoginService loginService;

    public UserManagementFrame() {
        loginService = LoginService.getInstance();

        // simple admin check
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

        // top
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(MAIN_BLUE);

        User currentUser = loginService.getCurrentUser();
        JLabel userInfoLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userInfoLabel, BorderLayout.EAST);

        // table
        String[] columns = {"Username", "Role", "Email", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setRowHeight(25);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // header renderer (keep same style)
        userTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setBackground(MAIN_BLUE);
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("User Accounts"));

        // buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        addButton = createBlueButton("Add User");
        updateButton = createBlueButton("Update User");
        deactivateButton = createBlueButton("Deactivate");
        activateButton = createBlueButton("Activate");
        resetPasswordButton = createBlueButton("Reset Password");
        viewLogButton = createBlueButton("View Auth Log");
        logoutButton = createBlueButton("Logout");

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

        // events
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deactivateButton.addActionListener(e -> setUserActive(false));
        activateButton.addActionListener(e -> setUserActive(true));
        resetPasswordButton.addActionListener(e -> resetPassword());
        viewLogButton.addActionListener(e -> viewAuthLog());
        logoutButton.addActionListener(e -> logout());
    }

    private JButton createBlueButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(MAIN_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));

        // force color
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        return button;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);

        List<User> users = loginService.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getRole(),
                    u.getEmail(),
                    u.isActive() ? "Active" : "Deactivated"
            });
        }
    }

    // ---------- actions ----------

    private void addUser() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"OFFICER", "ADMIN"});

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String role = String.valueOf(roleCombo.getSelectedItem());

        if (username.isEmpty() || password.isEmpty()) {
            err("Username and password cannot be empty!");
            return;
        }

        boolean ok = loginService.addUser(username, password, role, email);
        if (ok) {
            msg("User added successfully!");
            loadUsers();
        } else {
            err("Failed to add user.\n(Username may already exist OR you are not admin.)");
        }
    }

    private void updateUser() {
        String username = getSelectedUsername();
        if (username == null) return;

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"OFFICER", "ADMIN"});

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
        String role = String.valueOf(roleCombo.getSelectedItem());

        boolean ok = loginService.updateUser(username, password, role, email);
        if (ok) {
            msg("User updated successfully!");
            loadUsers();
        } else {
            err("Failed to update user.");
        }
    }

    private void setUserActive(boolean active) {
        int viewRow = userTable.getSelectedRow();
        if (viewRow < 0) {
            warn("Please select a user first.");
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(viewRow);
        String username = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String status = String.valueOf(tableModel.getValueAt(modelRow, 3));

        if (active && "Active".equals(status)) {
            msg("User is already active.");
            return;
        }
        if (!active && "Deactivated".equals(status)) {
            msg("User is already deactivated.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                (active ? "Activate" : "Deactivate") + " user: " + username + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = active ? loginService.activateUser(username) : loginService.deactivateUser(username);

        if (ok) {
            msg("User " + (active ? "activated" : "deactivated") + " successfully!");
            loadUsers();
        } else {
            err("Operation failed.\n(You may be trying to deactivate your own account.)");
        }
    }

    private void resetPassword() {
        String username = getSelectedUsername();
        if (username == null) return;

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
            err("Password cannot be empty!");
            return;
        }
        if (!password.equals(confirm)) {
            err("Passwords do not match!");
            return;
        }

        boolean ok = loginService.resetPassword(username, password);
        if (ok) msg("Password reset successfully!");
        else err("Failed to reset password.");
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

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
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

    // ---------- small helpers ----------

    private String getSelectedUsername() {
        int viewRow = userTable.getSelectedRow();
        if (viewRow < 0) {
            warn("Please select a user first.");
            return null;
        }
        int modelRow = userTable.convertRowIndexToModel(viewRow);
        return String.valueOf(tableModel.getValueAt(modelRow, 0));
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
