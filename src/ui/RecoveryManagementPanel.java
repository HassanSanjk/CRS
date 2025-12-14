package ui;

import model.*;
import repository.CourseRepository;
import repository.StudentRepository;
import services.EmailService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RecoveryManagementPanel
 * Manage recovery plans + milestones (saved in recovery_plans.txt)
 */
public class RecoveryManagementPanel extends JPanel {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EmailService emailService;

    private List<Student> students = new ArrayList<Student>();
    private List<Course> courses = new ArrayList<Course>();
    private RecoveryPlan currentPlan;

    // UI
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

    // ---------------- load data ----------------

    private void loadInitialData() {
        try {
            students = studentRepo.loadAllStudents();
            courses = courseRepo.loadAllCourses();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage(),
                    "Data Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- UI setup ----------------

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // student table
        studentTable = new JTable(createStudentModel());
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(22);

        // course list
        courseListModel = new DefaultListModel<String>();
        courseList = new JList<String>(courseListModel);
        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setVisibleRowCount(8);

        // milestone table
        milestoneTable = new JTable(createMilestoneModel());
        milestoneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        milestoneTable.setRowHeight(22);

        // fields
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

        // buttons
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
        // LEFT: students
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Students"));
        leftPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // CENTER: courses
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        centerPanel.add(new JScrollPane(courseList), BorderLayout.CENTER);

        JLabel courseHint = new JLabel("Select a course to create/load recovery plan");
        courseHint.setFont(new Font("Arial", Font.ITALIC, 10));
        centerPanel.add(courseHint, BorderLayout.SOUTH);

        // RIGHT: plan + milestones
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.add(createPlanPanel(), BorderLayout.NORTH);
        rightPanel.add(createMilestonePanel(), BorderLayout.CENTER);

        JSplitPane leftCenterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
        leftCenterSplit.setResizeWeight(0.5);
        leftCenterSplit.setDividerLocation(300);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCenterSplit, rightPanel);
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

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Student:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtStudentSelected, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Course:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtCourseSelected, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(btnNewPlan);
        buttonPanel.add(btnLoadPlan);
        buttonPanel.add(btnSavePlan);
        buttonPanel.add(btnSendEmail);
        buttonPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        row++;

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

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Task Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        editor.add(txtTitle, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Deadline (e.g., 2025-02-15):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        editor.add(txtDeadline, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(chkCompleted, gbc);
        row++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttons.add(btnAddMilestone);
        buttons.add(btnUpdateMilestone);
        buttons.add(btnRemoveMilestone);
        buttons.add(btnMarkCompleted);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(buttons, gbc);

        return editor;
    }

    // ---------------- models ----------------

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

    // ---------------- refresh ----------------

    private void refreshStudentTable() {
        DefaultTableModel m = (DefaultTableModel) studentTable.getModel();
        m.setRowCount(0);

        for (Student s : students) {
            m.addRow(new Object[]{
                    s.getStudentId(),
                    s.getFullName(),
                    safe(s.getEmail(), "N/A"),
                    safe(s.getMajor(), "N/A"),
                    safe(s.getYear(), "N/A")
            });
        }
    }

    private void refreshCourseList() {
        courseListModel.clear();
        for (Course c : courses) {
            String display = c.getCourseId() + " - " + safe(c.getCourseName(), "Unnamed Course");
            courseListModel.addElement(display);
        }
    }

    private void refreshMilestoneTable() {
        DefaultTableModel m = (DefaultTableModel) milestoneTable.getModel();
        m.setRowCount(0);

        if (currentPlan == null) return;

        List<Milestone> list = currentPlan.getMilestones();
        for (int i = 0; i < list.size(); i++) {
            Milestone ms = list.get(i);
            String status = ms.isCompleted() ? "✓ DONE" : "○ Pending";
            m.addRow(new Object[]{i + 1, ms.getTitle(), ms.getDeadline(), status});
        }
    }

    private void updateProgressLabel() {
        int progress = 0;
        if (currentPlan != null) {
            progress = ProgressTracker.calculateProgress(currentPlan);
        }

        lblProgress.setText("Progress: " + progress + "%");

        if (progress == 100) lblProgress.setForeground(new Color(0, 153, 0));
        else if (progress >= 50) lblProgress.setForeground(new Color(0, 102, 204));
        else lblProgress.setForeground(new Color(204, 102, 0));
    }

    // ---------------- listeners ----------------

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

    // ---------------- actions ----------------

    private void onStudentSelected() {
        Student s = getSelectedStudent();
        txtStudentSelected.setText(s == null ? "" : (s.getStudentId() + " - " + s.getFullName()));
    }

    private void onCourseSelected() {
        Course c = getSelectedCourse();
        txtCourseSelected.setText(c == null ? "" : (c.getCourseId() + " - " + safe(c.getCourseName(), "")));
    }

    private void onMilestoneSelected() {
        if (currentPlan == null) return;

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) return;

        Milestone ms = currentPlan.getMilestones().get(row);
        txtTitle.setText(ms.getTitle());
        txtDeadline.setText(ms.getDeadline());
        chkCompleted.setSelected(ms.isCompleted());
    }

    private void onNewPlan() {
        Student s = getSelectedStudent();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first.", "No Student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Course c = getSelectedCourse();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan = new RecoveryPlan(s.getStudentId(), c.getCourseId());
        refreshMilestoneTable();
        updateProgressLabel();
        clearMilestoneEditor();

        JOptionPane.showMessageDialog(this,
                "New recovery plan created.\nNow add milestones and save.",
                "New Plan",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoadPlan() {
        Student s = getSelectedStudent();
        Course c = getSelectedCourse();
        if (s == null || c == null) {
            JOptionPane.showMessageDialog(this, "Select student and course first.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RecoveryPlan loaded = loadPlanFromFile(s.getStudentId(), c.getCourseId());
        if (loaded == null) {
            JOptionPane.showMessageDialog(this, "No saved plan found for this student + course.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        currentPlan = loaded;
        refreshMilestoneTable();
        updateProgressLabel();

        JOptionPane.showMessageDialog(this, "Plan loaded.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSavePlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create or load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            savePlanToFile(currentPlan);
            JOptionPane.showMessageDialog(this, "Plan saved to: " + PLANS_FILE, "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRefresh() {
        loadInitialData();
        refreshStudentTable();
        refreshCourseList();

        JOptionPane.showMessageDialog(this,
                "Data refreshed.\nStudents: " + students.size() + "\nCourses: " + courses.size(),
                "Refresh",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAddMilestone() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create or load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = safe(txtTitle.getText()).trim();
        String deadline = safe(txtDeadline.getText()).trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter task title and deadline.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Milestone ms = new Milestone(title, deadline);
        ms.setCompleted(chkCompleted.isSelected());

        currentPlan.addMilestone(ms);
        refreshMilestoneTable();
        updateProgressLabel();
        clearMilestoneEditor();
    }

    private void onUpdateMilestone() {
        if (currentPlan == null) return;

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, "Select a milestone first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = safe(txtTitle.getText());
        String deadline = safe(txtDeadline.getText());

        if (title.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter task title and deadline.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Milestone ms = currentPlan.getMilestones().get(row);
        ms.setTitle(title);
        ms.setDeadline(deadline);
        ms.setCompleted(chkCompleted.isSelected());

        refreshMilestoneTable();
        updateProgressLabel();
    }

    private void onRemoveMilestone() {
        if (currentPlan == null) return;

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, "Select a milestone first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Remove this milestone?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        currentPlan.removeMilestone(row);
        refreshMilestoneTable();
        updateProgressLabel();
        clearMilestoneEditor();
    }

    private void onApplyCompleted() {
        if (currentPlan == null) return;

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, "Select a milestone first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan.updateMilestoneStatus(row, chkCompleted.isSelected());
        refreshMilestoneTable();
        updateProgressLabel();
    }

    private void onSendEmail() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create or load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = getSelectedStudent();
        Course c = getSelectedCourse();
        if (s == null || c == null) return;

        if (safe(s.getEmail()).isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student has no email saved.", "No Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder actionPlan = new StringBuilder();
        actionPlan.append("Recovery Plan for ").append(c.getCourseId()).append(" - ").append(safe(c.getCourseName(), "")).append("\n\n");

        for (Milestone m : currentPlan.getMilestones()) {
            actionPlan.append("- ").append(m.getTitle())
                    .append(" (Due: ").append(m.getDeadline()).append(") ")
                    .append(m.isCompleted() ? "[DONE]" : "[PENDING]")
                    .append("\n");
        }

        boolean ok = emailService.sendRecoveryPlanEmail(
                s.getEmail(),
                s.getFullName(),
                currentPlan.getCourseId(),
                actionPlan.toString()
        );

        JOptionPane.showMessageDialog(this,
                ok ? "Email sent to: " + s.getEmail() : "Email failed (check EmailService).",
                "Email",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    // ---------------- helpers ----------------

    private Student getSelectedStudent() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow < 0) return null;

        // IMPORTANT: read ID from table (safer than students.get(row))
        String studentId = String.valueOf(studentTable.getValueAt(viewRow, 0));
        return findStudentById(studentId);
    }

    private Course getSelectedCourse() {
        int idx = courseList.getSelectedIndex();
        if (idx < 0 || idx >= courses.size()) return null;

        // list index matches the same order we loaded into courseListModel
        return courses.get(idx);
    }

    private Student findStudentById(String studentId) {
        studentId = safe(studentId).toLowerCase();
        for (Student s : students) {
            if (s.getStudentId().toLowerCase().equals(studentId)) return s;
        }
        return null;
    }

    private void clearMilestoneEditor() {
        txtTitle.setText("");
        txtDeadline.setText("");
        chkCompleted.setSelected(false);
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String safe(String s, String def) {
        String x = safe(s);
        return x.isEmpty() ? def : x;
    }

    // ---------------- file save/load ----------------
    // Format:
    // PLAN|studentId|courseId
    // MILESTONE|title|deadline|completed

    private RecoveryPlan loadPlanFromFile(String studentId, String courseId) {
        File file = new File(PLANS_FILE);
        if (!file.exists()) return null;

        RecoveryPlan plan = null;
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 1) continue;

                if ("PLAN".equals(parts[0]) && parts.length >= 3) {
                    String sid = safe(parts[1]);
                    String cid = safe(parts[2]);

                    if (sid.equalsIgnoreCase(studentId) && cid.equalsIgnoreCase(courseId)) {
                        plan = new RecoveryPlan(studentId, courseId);
                        found = true;
                    } else {
                        if (found) break; // finished reading our plan
                        found = false;
                    }
                } else if ("MILESTONE".equals(parts[0]) && parts.length >= 4 && found) {
                    String title = safe(parts[1]);
                    String deadline = safe(parts[2]);
                    boolean completed = "true".equalsIgnoreCase(safe(parts[3]));

                    Milestone ms = new Milestone(title, deadline);
                    ms.setCompleted(completed);
                    plan.addMilestone(ms);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Read error: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }

        return plan;
    }

    private void savePlanToFile(RecoveryPlan plan) throws IOException {
        File file = new File(PLANS_FILE);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        // keep all old plans except the one we are updating
        List<String> keepLines = new ArrayList<String>();

        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean skipMode = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");

                if (parts.length >= 3 && "PLAN".equals(parts[0])) {
                    String sid = safe(parts[1]);
                    String cid = safe(parts[2]);

                    if (sid.equalsIgnoreCase(plan.getStudentId()) && cid.equalsIgnoreCase(plan.getCourseId())) {
                        skipMode = true;
                        continue;
                    } else {
                        skipMode = false;
                    }
                }

                if (!skipMode) keepLines.add(line);
            }
            reader.close();
        }

        PrintWriter writer = new PrintWriter(new FileWriter(file));

        for (String l : keepLines) writer.println(l);

        writer.println("PLAN|" + plan.getStudentId() + "|" + plan.getCourseId());

        for (Milestone m : plan.getMilestones()) {
            String title = safe(m.getTitle()).replace("|", " ");
            String deadline = safe(m.getDeadline()).replace("|", " ");
            writer.println("MILESTONE|" + title + "|" + deadline + "|" + m.isCompleted());
        }

        writer.close();
    }
}
