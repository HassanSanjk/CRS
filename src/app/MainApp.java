package app;

import model.User;
import repository.*;
import services.*;
import ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainApp - Main Menu / Launcher for Course Recovery System (CRS)
 * Opens only AFTER successful login.
 */
public class MainApp extends JFrame {

    // UI constants
    private static final Color BG = new Color(240, 248, 255);
    private static final Color BLUE = new Color(0, 102, 204);
    private static final Color HOVER_BLUE = new Color(30, 144, 255);
    private static final Color PRESS_BLUE = new Color(0, 82, 164);
    private static final Color RED = new Color(220, 20, 60);
    private static final Color HOVER_RED = new Color(200, 20, 55);
    private static final Color PRESS_RED = new Color(160, 15, 45);

    // Services used here
    private final EmailService emailService = new EmailService();
    private final LoginService loginService = LoginService.getInstance();

    // Logged-in user
    private final User currentUser;

    public MainApp() {
        currentUser = loginService.getCurrentUser();

        if (currentUser == null) {
            JOptionPane.showMessageDialog(null,
                    "No user session found.\nPlease login first.",
                    "Not Logged In",
                    JOptionPane.WARNING_MESSAGE);

            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            dispose();
            return;
        }

        setTitle("Course Recovery System - Main Menu");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainApp.this,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    loginService.logout(); // <-- log out properly
                    dispose();
                    System.exit(0);
                }
            }
        });

        
        
        setLocationRelativeTo(null);
        
        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(BG);

        main.add(createHeaderPanel(), BorderLayout.NORTH);
        main.add(createModulePanel(), BorderLayout.CENTER);
        main.add(createFooterPanel(), BorderLayout.SOUTH);

        add(main);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BLUE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Course Recovery System");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Main Menu - Select a Module");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(200, 230, 255));

        JLabel user = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        user.setFont(new Font("Arial", Font.PLAIN, 12));
        user.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BLUE);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setBackground(BLUE);
        userPanel.add(user);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(userPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createModulePanel() {
    JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
    panel.setBackground(BG);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JButton userMgmtButton = createModuleButton(
            "User Management",
            "Manage users, roles, and authentication",
            BLUE,
            false,
            e -> {
                if (!isAdmin()) {
                    showAdminOnlyMessage();
                    return;
                }
                openUserManagement();
            }
    );

    JButton eligibilityButton = createModuleButton(
            "Eligibility Check",
            "Check CGPA & progression eligibility",
            BLUE,
            false,
            e -> openEligibility()
    );

    JButton recoveryButton = createModuleButton(
            "Course Recovery",
            "Create recovery plans + track progress",
            BLUE,
            false,
            e -> openCourseRecovery()
    );

    JButton reportButton = createModuleButton(
            "Reports & PDF",
            "Generate academic performance report",
            BLUE,
            false,
            e -> openGenerateReport()
    );

    JButton systemButton = createModuleButton(
            "System Services",
            "Email + logs (admin only)",
            BLUE,
            false,
            e -> {
                if (!isAdmin()) {
                    showAdminOnlyMessage();
                    return;
                }
                openSystemServices();
            }
    );

    JButton logoutButton = createModuleButton(
            "Logout",
            "Exit the system",
            RED,
            true,
            e -> logout()
    );

    panel.add(userMgmtButton);
    panel.add(eligibilityButton);
    panel.add(recoveryButton);
    panel.add(reportButton);
    panel.add(systemButton);
    panel.add(logoutButton);

    return panel;
}


    private JButton createModuleButton(String title, String description, Color baseColor,
                                       boolean isDanger, ActionListener action) {

        JButton button = new JButton();
        button.setLayout(new BorderLayout(5, 5));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // keep your rendering behavior
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseColor.darker(), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(new Color(255, 255, 255, 200));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        button.add(titleLabel, BorderLayout.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);

        button.addActionListener(action);

        // hover effects
        Color hover = isDanger ? HOVER_RED : HOVER_BLUE;
        Color press = isDanger ? PRESS_RED : PRESS_BLUE;

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(baseColor); }
            @Override public void mousePressed(MouseEvent e) { button.setBackground(press); }
            @Override public void mouseReleased(MouseEvent e) { button.setBackground(hover); }
        });

        return button;
    }

    private JButton createDialogButton(String title, Color color, boolean isDanger, ActionListener action) {
        JButton button = new JButton(title);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addActionListener(action);

        Color hover = isDanger ? HOVER_RED : HOVER_BLUE;
        Color press = isDanger ? PRESS_RED : PRESS_BLUE;

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(color); }
            @Override public void mousePressed(MouseEvent e) { button.setBackground(press); }
            @Override public void mouseReleased(MouseEvent e) { button.setBackground(hover); }
        });

        return button;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));

        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);

        JLabel versionLabel = new JLabel("CRS v1.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        versionLabel.setForeground(Color.GRAY);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(versionLabel, BorderLayout.EAST);
        return panel;
    }

    private void showAdminOnlyMessage() {
    JOptionPane.showMessageDialog(this,
            "Access denied. Admin only.",
            "Not Allowed",
            JOptionPane.WARNING_MESSAGE);
}

    // ---------- Launchers ----------

    private void openUserManagement() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Access Denied. Admin privileges required.",
                    "Authorization Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new UserManagementFrame().setVisible(true));
    }

    private void openEligibility() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Eligibility Module");
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(this);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            StudentRepository studentRepo = new StudentRepository();
            CourseRepository courseRepo = new CourseRepository();
            GradeFileHandler gradeFile = new GradeFileHandler("data/grades.txt");
            RegistrationRepository regRepo = new RegistrationRepository("data/registrations.txt");

            EligibilityService eligibilityService = new EligibilityService(studentRepo, courseRepo, gradeFile, regRepo);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Eligibility & Registration", new EligibilityPanel(eligibilityService));
            tabs.addTab("Students & Courses", new StudentCourseManagementPanel());

            frame.add(tabs);
            frame.setVisible(true);
        });
    }

    private void openGenerateReport() {
        SwingUtilities.invokeLater(() -> new GenerateReportDialog(this).setVisible(true));
    }

    private void openCourseRecovery() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Course Recovery Management");
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(this);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new RecoveryManagementPanel());
            frame.setVisible(true);
        });
    }

    private void openSystemServices() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this,
                    "Access denied. Admin only.",
                    "Authorization Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "System Services", true);
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(BG);

        JLabel titleLabel = new JLabel("System Services");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton testEmailButton = createDialogButton(
                "Test Email", BLUE, false,
                e -> {
                    boolean ok = emailService.testConnection();
                    JOptionPane.showMessageDialog(dialog,
                            ok ? "Email test successful." : "Email test failed.",
                            "Email Test",
                            ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                }
        );

        JButton viewLogsButton = createDialogButton(
                "View Auth Log", BLUE, false,
                e -> viewLoginLogs()
        );


        testEmailButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewLogsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(25));
        panel.add(testEmailButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(viewLogsButton);
        panel.add(Box.createVerticalStrut(25));

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void viewLoginLogs() {
        java.util.List<String> logs = loginService.readAuthLog();

        JTextArea textArea = new JTextArea(20, 50);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        if (logs.isEmpty()) {
            textArea.setText("No login logs found.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("AUTHENTICATION LOG\n");
            sb.append("==========================================\n\n");
            for (String entry : logs) sb.append(entry).append("\n");
            textArea.setText(sb.toString());
        }

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Authentication Log (Binary)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            loginService.logout();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() != null
                && currentUser.getRole().equalsIgnoreCase("ADMIN");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            new LoginFrame().setVisible(true);
        });
    }
}
