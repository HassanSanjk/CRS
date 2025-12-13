package ui;

import model.Course;
import model.Milestone;
import model.ProgressTracker;
import model.RecoveryPlan;
import model.Student;
import repository.CourseRepository;
import repository.StudentRepository;
import services.EmailService;
import services.FileService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced RecoveryManagementPanel with Course Selection
 * Saves to recovery_plans.txt format
 */
public class RecoveryManagementPanel extends JPanel {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EmailService emailService;

    private List<Student> students = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private RecoveryPlan currentPlan;

    // UI Components
    private JTable studentTable;
    private JTable milestoneTable;
    private JList<String> courseList;
    private DefaultListModel<String> courseListModel;

    private JTextField txtStudentSelected;
    private JTextField txtCourseSelected;

    private JTextField txtTitle;
    private JTextField txtDeadline;
    private JCheckBox chkCompleted;

    private JLabel lblProgress;

    private JButton btnNewPlan;
    private JButton btnLoadPlan;
    private JButton btnSavePlan;

    private JButton btnAddMilestone;
    private JButton btnUpdateMilestone;
    private JButton btnRemoveMilestone;
    private JButton btnMarkCompleted;

    private JButton btnSendEmail;
    private JButton btnRefresh;

    private static final String PLANS_FILE = "data/recovery_plans.txt";

    public RecoveryManagementPanel() {
        // Initialize repositories with proper file paths matching other classes
        studentRepo = new StudentRepository("data/student_information.csv");
        courseRepo = new CourseRepository("data/course_assessment_information.csv");
        emailService = new EmailService();

        loadInitialData();
        initComponents();
        initLayout();
        initListeners();

        refreshStudentTable();
        refreshCourseList();
        refreshMilestoneTable();
        updateProgressLabel();
    }

    // ---------------- Data Loading ----------------

    private void loadInitialData() {
        try {
            students = studentRepo.loadAllStudents();
            courses = courseRepo.loadAllCourses();
            
            if (students.isEmpty()) {
                System.out.println("Warning: No students loaded. Check data/student_information.csv");
            }
            
            if (courses.isEmpty()) {
                System.out.println("Warning: No courses loaded. Check data/course_assessment_information.csv");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage(),
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ---------------- UI Initialization ----------------

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Student table
        studentTable = new JTable(createStudentModel());
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(22);

        // Course list
        courseListModel = new DefaultListModel<>();
        courseList = new JList<>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setVisibleRowCount(8);

        // Milestone table
        milestoneTable = new JTable(createMilestoneModel());
        milestoneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        milestoneTable.setRowHeight(22);

        // Text fields
        txtStudentSelected = new JTextField();
        txtStudentSelected.setEditable(false);
        txtStudentSelected.setBackground(Color.WHITE);

        txtCourseSelected = new JTextField();
        txtCourseSelected.setEditable(false);
        txtCourseSelected.setBackground(Color.WHITE);

        txtTitle = new JTextField();
        txtDeadline = new JTextField();
        chkCompleted = new JCheckBox("Completed");

        lblProgress = new JLabel("Progress: 0%");
        lblProgress.setFont(new Font("Arial", Font.BOLD, 14));
        lblProgress.setForeground(new Color(0, 102, 204));

        // Buttons
        btnNewPlan = createButton("New Plan");
        btnLoadPlan = createButton("Load Plan");
        btnSavePlan = createButton("Save Plan");
        btnRefresh = createButton("Refresh Data");

        btnAddMilestone = createButton("Add");
        btnUpdateMilestone = createButton("Update");
        btnRemoveMilestone = createButton("Remove");
        btnMarkCompleted = createButton("Apply Completed");

        btnSendEmail = createButton("Send Plan Email");
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        return btn;
    }

    private void initLayout() {
        // LEFT PANEL: Students
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));
        leftPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // CENTER PANEL: Courses
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        centerPanel.add(new JScrollPane(courseList), BorderLayout.CENTER);
        
        JLabel courseHint = new JLabel("Select a course to create/load recovery plan");
        courseHint.setFont(new Font("Arial", Font.ITALIC, 10));
        centerPanel.add(courseHint, BorderLayout.SOUTH);

        // RIGHT PANEL: Plan + Milestones
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        JPanel planPanel = createPlanPanel();
        JPanel milestonePanel = createMilestonePanel();

        rightPanel.add(planPanel, BorderLayout.NORTH);
        rightPanel.add(milestonePanel, BorderLayout.CENTER);

        // Split panes for responsive layout
        JSplitPane leftCenterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                     leftPanel, centerPanel);
        leftCenterSplit.setResizeWeight(0.5);
        leftCenterSplit.setDividerLocation(300);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                               leftCenterSplit, rightPanel);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerLocation(650);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createPlanPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recovery Plan (Student + Course)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Selected Student
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Student:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtStudentSelected, gbc);
        row++;

        // Selected Course
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Course:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtCourseSelected, gbc);
        row++;

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(btnNewPlan);
        buttonPanel.add(btnLoadPlan);
        buttonPanel.add(btnSavePlan);
        buttonPanel.add(btnSendEmail);
        buttonPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        row++;

        // Progress
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(lblProgress, gbc);

        return panel;
    }

    private JPanel createMilestonePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Milestones (Add / Update / Remove + Track)"));

        panel.add(new JScrollPane(milestoneTable), BorderLayout.CENTER);
        panel.add(createMilestoneEditor(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMilestoneEditor() {
        JPanel editor = new JPanel(new GridBagLayout());
        editor.setBorder(BorderFactory.createTitledBorder("Milestone Editor"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Task Title
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Task Title:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        editor.add(txtTitle, gbc);
        row++;

        // Deadline
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Deadline (e.g., 2025-02-15):"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        editor.add(txtDeadline, gbc);
        row++;

        // Completed checkbox
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(chkCompleted, gbc);
        row++;

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttons.add(btnAddMilestone);
        buttons.add(btnUpdateMilestone);
        buttons.add(btnRemoveMilestone);
        buttons.add(btnMarkCompleted);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(buttons, gbc);

        return editor;
    }

    // ---------------- Table/List Models ----------------

    private DefaultTableModel createStudentModel() {
        String[] cols = {"ID", "Name", "Email", "Major", "Year"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private DefaultTableModel createMilestoneModel() {
        String[] cols = {"#", "Task Title", "Deadline", "Status"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------------- Refresh Methods ----------------

    private void refreshStudentTable() {
        DefaultTableModel m = (DefaultTableModel) studentTable.getModel();
        m.setRowCount(0);
        
        for (Student s : students) {
            m.addRow(new Object[]{
                    s.getStudentId(),
                    s.getFullName(),
                    s.getEmail() != null ? s.getEmail() : "N/A",
                    s.getMajor() != null ? s.getMajor() : "N/A",
                    s.getYear() != null ? s.getYear() : "N/A"
            });
        }
    }

    private void refreshCourseList() {
        courseListModel.clear();
        
        for (Course c : courses) {
            String display = c.getCourseId() + " - " + 
                           (c.getCourseName() != null ? c.getCourseName() : "Unnamed Course");
            courseListModel.addElement(display);
        }
    }

    private void refreshMilestoneTable() {
        DefaultTableModel m = (DefaultTableModel) milestoneTable.getModel();
        m.setRowCount(0);

        if (currentPlan == null || currentPlan.getMilestones() == null) return;

        List<Milestone> list = currentPlan.getMilestones();
        for (int i = 0; i < list.size(); i++) {
            Milestone ms = list.get(i);
            String status = ms.isCompleted() ? "✓ DONE" : "○ Pending";
            m.addRow(new Object[]{ 
                i + 1, 
                ms.getTitle(), 
                ms.getDeadline(), 
                status 
            });
        }
    }

    private void updateProgressLabel() {
        int progress = 0;
        if (currentPlan != null) {
            progress = ProgressTracker.calculateProgress(currentPlan);
        }
        lblProgress.setText("Progress: " + progress + "%");
        
        // Color coding
        if (progress == 100) {
            lblProgress.setForeground(new Color(0, 153, 0));
        } else if (progress >= 50) {
            lblProgress.setForeground(new Color(0, 102, 204));
        } else {
            lblProgress.setForeground(new Color(204, 102, 0));
        }
    }

    // ---------------- Event Listeners ----------------

    private void initListeners() {
        studentTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onStudentSelected();
        });

        courseList.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onCourseSelected();
        });

        milestoneTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onMilestoneSelected();
        });

        btnNewPlan.addActionListener(e -> onNewPlan());
        btnLoadPlan.addActionListener(e -> onLoadPlan());
        btnSavePlan.addActionListener(e -> onSavePlan());
        btnRefresh.addActionListener(e -> onRefresh());

        btnAddMilestone.addActionListener(e -> onAddMilestone());
        btnUpdateMilestone.addActionListener(e -> onUpdateMilestone());
        btnRemoveMilestone.addActionListener(e -> onRemoveMilestone());
        btnMarkCompleted.addActionListener(e -> onApplyCompleted());

        btnSendEmail.addActionListener(e -> onSendEmail());
    }

    // ---------------- Action Handlers ----------------

    private void onStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= students.size()) {
            txtStudentSelected.setText("");
            return;
        }
        Student s = students.get(row);
        txtStudentSelected.setText(s.getStudentId() + " - " + s.getFullName());
    }

    private void onCourseSelected() {
        int idx = courseList.getSelectedIndex();
        if (idx < 0 || idx >= courses.size()) {
            txtCourseSelected.setText("");
            return;
        }
        Course c = courses.get(idx);
        txtCourseSelected.setText(c.getCourseId() + " - " + c.getCourseName());
    }

    private void onMilestoneSelected() {
        int row = milestoneTable.getSelectedRow();
        if (currentPlan == null || row < 0 || row >= currentPlan.getMilestones().size()) {
            return;
        }

        Milestone ms = currentPlan.getMilestones().get(row);
        txtTitle.setText(ms.getTitle());
        txtDeadline.setText(ms.getDeadline());
        chkCompleted.setSelected(ms.isCompleted());
    }

    private void onNewPlan() {
        Student s = getSelectedStudent();
        if (s == null) return;

        Course c = getSelectedCourse();
        if (c == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a course from the course list.", 
                    "No Course Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan = new RecoveryPlan(s.getStudentId(), c.getCourseId());
        refreshMilestoneTable();
        updateProgressLabel();
        clearMilestoneEditor();

        JOptionPane.showMessageDialog(this, 
                "New recovery plan created for:\n" +
                "Student: " + s.getFullName() + " (" + s.getStudentId() + ")\n" +
                "Course: " + c.getCourseId() + " - " + c.getCourseName() + "\n\n" +
                "Now add milestones and save the plan.", 
                "New Plan Created",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoadPlan() {
        Student s = getSelectedStudent();
        if (s == null) return;

        Course c = getSelectedCourse();
        if (c == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a course to load its recovery plan.", 
                    "No Course Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        RecoveryPlan loaded = loadPlanFromFile(s.getStudentId(), c.getCourseId());
        if (loaded == null) {
            JOptionPane.showMessageDialog(this, 
                    "No recovery plan found for:\n" +
                    "Student: " + s.getFullName() + " (" + s.getStudentId() + ")\n" +
                    "Course: " + c.getCourseId() + " - " + c.getCourseName() + "\n\n" +
                    "Create a new plan or check " + PLANS_FILE, 
                    "Plan Not Found",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        currentPlan = loaded;
        refreshMilestoneTable();
        updateProgressLabel();

        JOptionPane.showMessageDialog(this, 
                "Recovery plan loaded successfully!\n" +
                "Student: " + s.getFullName() + "\n" +
                "Course: " + c.getCourseId() + "\n" +
                "Milestones: " + loaded.getMilestones().size(), 
                "Plan Loaded",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSavePlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a recovery plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentPlan.getMilestones().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "This plan has no milestones. Save anyway?",
                    "Empty Plan",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        try {
            savePlanToFile(currentPlan);
            
            JOptionPane.showMessageDialog(this, 
                    "Recovery plan saved successfully!\n" +
                    "File: " + PLANS_FILE + "\n" +
                    "Milestones: " + currentPlan.getMilestones().size(), 
                    "Plan Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving plan: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void onRefresh() {
        loadInitialData();
        refreshStudentTable();
        refreshCourseList();
        
        JOptionPane.showMessageDialog(this,
                "Data refreshed successfully!\n" +
                "Students: " + students.size() + "\n" +
                "Courses: " + courses.size(),
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAddMilestone() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a recovery plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String deadline = txtDeadline.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a task title.", 
                    "Missing Title",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a deadline (e.g., 2025-02-15).", 
                    "Missing Deadline",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Milestone ms = new Milestone(title, deadline);
        ms.setCompleted(chkCompleted.isSelected());

        currentPlan.addMilestone(ms);
        refreshMilestoneTable();
        updateProgressLabel();
        clearMilestoneEditor();

        JOptionPane.showMessageDialog(this, 
                "Milestone added successfully!\n" +
                "Remember to save the plan.", 
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onUpdateMilestone() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, 
                    "Select a milestone to update.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String deadline = txtDeadline.getText().trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Enter task title and deadline.", 
                    "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Milestone ms = currentPlan.getMilestones().get(row);
        ms.setTitle(title);
        ms.setDeadline(deadline);
        ms.setCompleted(chkCompleted.isSelected());

        refreshMilestoneTable();
        updateProgressLabel();

        JOptionPane.showMessageDialog(this, 
                "Milestone updated successfully!\n" +
                "Remember to save the plan.", 
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onRemoveMilestone() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, 
                    "Select a milestone to remove.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Remove selected milestone?\n" +
                "Task: " + currentPlan.getMilestones().get(row).getTitle(), 
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            currentPlan.removeMilestone(row);
            refreshMilestoneTable();
            updateProgressLabel();
            clearMilestoneEditor();

            JOptionPane.showMessageDialog(this, 
                    "Milestone removed successfully!\n" +
                    "Remember to save the plan.", 
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onApplyCompleted() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, 
                    "Select a milestone first.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan.updateMilestoneStatus(row, chkCompleted.isSelected());
        refreshMilestoneTable();
        updateProgressLabel();
    }

    private void onSendEmail() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, 
                    "Create or load a plan first.", 
                    "No Plan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = getSelectedStudent();
        if (s == null) return;

        Course c = getSelectedCourse();
        if (c == null) return;

        if (s.getEmail() == null || s.getEmail().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Student has no email address on file.",
                    "No Email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build action plan text
        StringBuilder actionPlan = new StringBuilder();
        actionPlan.append("Recovery Plan for Course: ")
                  .append(c.getCourseId()).append(" - ")
                  .append(c.getCourseName()).append("\n\n");
        actionPlan.append("Milestones:\n");
        
        for (Milestone m : currentPlan.getMilestones()) {
            actionPlan.append("• ").append(m.getTitle())
                    .append(" (Due: ").append(m.getDeadline()).append(")")
                    .append(m.isCompleted() ? " [✓ COMPLETED]" : " [○ PENDING]")
                    .append("\n");
        }

        boolean ok = emailService.sendRecoveryPlanEmail(
                s.getEmail(),
                s.getFullName(),
                currentPlan.getCourseId(),
                actionPlan.toString()
        );

        JOptionPane.showMessageDialog(this,
                ok ? "Email sent to: " + s.getEmail() + "\n(Check EmailService configuration if using real SMTP)" 
                   : "Email failed. Please check EmailService configuration.",
                "Email Notification",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    // ---------------- Helper Methods ----------------

    private Student getSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= students.size()) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a student first.", 
                    "No Student Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return students.get(row);
    }

    private Course getSelectedCourse() {
        int idx = courseList.getSelectedIndex();
        if (idx < 0 || idx >= courses.size()) {
            return null;
        }
        return courses.get(idx);
    }

    private void clearMilestoneEditor() {
        txtTitle.setText("");
        txtDeadline.setText("");
        chkCompleted.setSelected(false);
    }

    // ---------------- File Persistence (TXT FORMAT) ----------------

    /**
     * Load plan from recovery_plans.txt
     * Format: PLAN|studentId|courseId
     *         MILESTONE|title|deadline|completed
     */
    private RecoveryPlan loadPlanFromFile(String studentId, String courseId) {
        File file = new File(PLANS_FILE);
        if (!file.exists()) {
            return null;
        }

        RecoveryPlan plan = null;
        boolean foundPlan = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                
                // Check if this is a plan header
                if (parts[0].equals("PLAN") && parts.length >= 3) {
                    String sid = parts[1].trim();
                    String cid = parts[2].trim();
                    
                    // If we find matching plan, create it
                    if (sid.equalsIgnoreCase(studentId) && cid.equalsIgnoreCase(courseId)) {
                        plan = new RecoveryPlan(studentId, courseId);
                        foundPlan = true;
                    } else {
                        // Different plan, stop loading milestones
                        if (foundPlan) break;
                    }
                }
                // Load milestone if we're in the right plan
                else if (parts[0].equals("MILESTONE") && parts.length >= 4 && foundPlan) {
                    String title = parts[1].trim();
                    String deadline = parts[2].trim();
                    boolean completed = "true".equalsIgnoreCase(parts[3].trim());
                    
                    Milestone ms = new Milestone(title, deadline);
                    ms.setCompleted(completed);
                    plan.addMilestone(ms);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error reading plan file: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return plan;
    }

    /**
     * Save plan to recovery_plans.txt
     * Format: PLAN|studentId|courseId
     *         MILESTONE|title|deadline|completed
     */
    private void savePlanToFile(RecoveryPlan plan) throws IOException {
        File file = new File(PLANS_FILE);
        
        // Create data directory if it doesn't exist
        File dataDir = file.getParentFile();
        if (dataDir != null && !dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Read existing plans
        List<String> allLines = new ArrayList<>();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean skipMode = false;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] parts = line.split("\\|");
                    
                    // Check if this is a plan header
                    if (parts[0].equals("PLAN") && parts.length >= 3) {
                        String sid = parts[1].trim();
                        String cid = parts[2].trim();
                        
                        // Skip this plan and its milestones if it matches current plan
                        if (sid.equalsIgnoreCase(plan.getStudentId()) && 
                            cid.equalsIgnoreCase(plan.getCourseId())) {
                            skipMode = true;
                            continue;
                        } else {
                            skipMode = false;
                        }
                    }
                    
                    // Only keep lines if not in skip mode
                    if (!skipMode) {
                        allLines.add(line);
                    }
                }
            }
        }

        // Write all plans back to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write existing plans (excluding the one we're updating)
            for (String line : allLines) {
                writer.println(line);
            }
            
            // Write current plan
            writer.println("PLAN|" + plan.getStudentId() + "|" + plan.getCourseId());
            
            // Write milestones
            for (Milestone m : plan.getMilestones()) {
                String title = m.getTitle().replace("|", " ");
                String deadline = m.getDeadline().replace("|", " ");
                writer.println("MILESTONE|" + title + "|" + deadline + "|" + m.isCompleted());
            }
        }
    }
}