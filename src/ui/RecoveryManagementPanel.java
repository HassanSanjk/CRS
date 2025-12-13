package ui;


import model.Milestone;
import model.ProgressTracker;
import model.RecoveryPlan;
import model.Student;
import repository.StudentRepository;
import services.EmailService;
import services.FileService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RecoveryManagementPanel
 * Beginner-friendly UI to:
 * - Create a recovery plan (student + course)
 * - Add/Update/Remove milestones
 * - Mark milestone completed
 * - Track progress %
 * - Save/Load plans from recovery_plans.csv
 */
public class RecoveryManagementPanel extends JPanel {

    private final StudentRepository studentRepo;
    private final FileService fileService;
    private final EmailService emailService;

    private List<Student> students = new ArrayList<>();
    private RecoveryPlan currentPlan; // plan currently being edited

    // UI
    private JTable studentTable;
    private JTable milestoneTable;

    private JTextField txtStudentSelected;
    private JTextField txtCourseId;

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

    public RecoveryManagementPanel() {
        studentRepo = new StudentRepository();
        fileService = new FileService();
        emailService = new EmailService();

        students = studentRepo.getAllStudents();

        initComponents();
        initLayout();
        initListeners();

        refreshStudentTable();
        refreshMilestoneTable();
        updateProgressLabel();
    }

    // ---------------- UI setup ----------------

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        studentTable = new JTable(createStudentModel());
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        milestoneTable = new JTable(createMilestoneModel());
        milestoneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtStudentSelected = new JTextField();
        txtStudentSelected.setEditable(false);

        txtCourseId = new JTextField();

        txtTitle = new JTextField();
        txtDeadline = new JTextField();
        chkCompleted = new JCheckBox("Completed");

        lblProgress = new JLabel("Progress: 0%");

        btnNewPlan = new JButton("New Plan");
        btnLoadPlan = new JButton("Load Plan");
        btnSavePlan = new JButton("Save Plan");

        btnAddMilestone = new JButton("Add");
        btnUpdateMilestone = new JButton("Update");
        btnRemoveMilestone = new JButton("Remove");
        btnMarkCompleted = new JButton("Apply Completed");

        btnSendEmail = new JButton("Send Plan Email");
    }

    private void initLayout() {
        // LEFT: students
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Students"));
        left.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // RIGHT: plan + milestones
        JPanel right = new JPanel(new BorderLayout(10, 10));

        JPanel planPanel = createPlanPanel();
        JPanel milestonePanel = createMilestonePanel();

        right.add(planPanel, BorderLayout.NORTH);
        right.add(milestonePanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.40);
        add(split, BorderLayout.CENTER);
    }

    private JPanel createPlanPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recovery Plan (Student + Course)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Selected Student
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Selected Student:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtStudentSelected, gbc);
        row++;

        // Course ID
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(new JLabel("Course ID:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        panel.add(txtCourseId, gbc);
        row++;

        // Buttons row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnNewPlan);
        buttons.add(btnLoadPlan);
        buttons.add(btnSavePlan);
        buttons.add(btnSendEmail);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttons, gbc);
        row++;

        // Progress
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
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
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Title
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Task Title:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        editor.add(txtTitle, gbc);
        row++;

        // Deadline
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        editor.add(new JLabel("Deadline:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        editor.add(txtDeadline, gbc);
        row++;

        // Completed
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(chkCompleted, gbc);
        row++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(btnAddMilestone);
        buttons.add(btnUpdateMilestone);
        buttons.add(btnRemoveMilestone);
        buttons.add(btnMarkCompleted);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        editor.add(buttons, gbc);

        return editor;
    }

    // ---------------- Models / table refresh ----------------

    private DefaultTableModel createStudentModel() {
        String[] cols = {"Student ID", "Name", "Email", "Major", "Year"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private DefaultTableModel createMilestoneModel() {
        String[] cols = {"#", "Title", "Deadline", "Completed"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private void refreshStudentTable() {
        DefaultTableModel m = (DefaultTableModel) studentTable.getModel();
        m.setRowCount(0);
        for (Student s : students) {
            m.addRow(new Object[]{
                    s.getStudentId(),
                    s.getFullName(),
                    s.getEmail(),
                    s.getMajor(),
                    s.getYear()
            });
        }
    }

    private void refreshMilestoneTable() {
        DefaultTableModel m = (DefaultTableModel) milestoneTable.getModel();
        m.setRowCount(0);

        if (currentPlan == null || currentPlan.getMilestones() == null) return;

        List<Milestone> list = currentPlan.getMilestones();
        for (int i = 0; i < list.size(); i++) {
            Milestone ms = list.get(i);
            m.addRow(new Object[]{ i, ms.getTitle(), ms.getDeadline(), ms.isCompleted() ? "YES" : "NO" });
        }
    }

    private void updateProgressLabel() {
        int progress = 0;
        if (currentPlan != null) progress = ProgressTracker.calculateProgress(currentPlan);
        lblProgress.setText("Progress: " + progress + "%");
    }

    // ---------------- Listeners ----------------

    private void initListeners() {
        studentTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onStudentSelected();
        });

        milestoneTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) onMilestoneSelected();
        });

        btnNewPlan.addActionListener(e -> onNewPlan());
        btnLoadPlan.addActionListener(e -> onLoadPlan());
        btnSavePlan.addActionListener(e -> onSavePlan());

        btnAddMilestone.addActionListener(e -> onAddMilestone());
        btnUpdateMilestone.addActionListener(e -> onUpdateMilestone());
        btnRemoveMilestone.addActionListener(e -> onRemoveMilestone());
        btnMarkCompleted.addActionListener(e -> onApplyCompleted());

        btnSendEmail.addActionListener(e -> onSendEmail());
    }

    // ---------------- Actions ----------------

    private void onStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= students.size()) {
            txtStudentSelected.setText("");
            return;
        }
        Student s = students.get(row);
        txtStudentSelected.setText(s.toShortString());
    }

    private void onMilestoneSelected() {
        int row = milestoneTable.getSelectedRow();
        if (currentPlan == null) return;
        if (row < 0 || row >= currentPlan.getMilestones().size()) return;

        Milestone ms = currentPlan.getMilestones().get(row);
        txtTitle.setText(ms.getTitle());
        txtDeadline.setText(ms.getDeadline());
        chkCompleted.setSelected(ms.isCompleted());
    }

    private void onNewPlan() {
        Student s = getSelectedStudent();
        if (s == null) return;

        String courseId = txtCourseId.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Course ID first.", "Missing Course", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan = new RecoveryPlan(s.getStudentId(), courseId);
        refreshMilestoneTable();
        updateProgressLabel();

        JOptionPane.showMessageDialog(this, "New recovery plan created (not saved yet).", "New Plan",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoadPlan() {
        Student s = getSelectedStudent();
        if (s == null) return;

        String courseId = txtCourseId.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Course ID to load plan.", "Missing Course", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RecoveryPlan loaded = loadPlanFromFile(s.getStudentId(), courseId);
        if (loaded == null) {
            JOptionPane.showMessageDialog(this, "No plan found for this student + course.", "Not Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentPlan = loaded;
        refreshMilestoneTable();
        updateProgressLabel();

        JOptionPane.showMessageDialog(this, "Plan loaded successfully.", "Loaded",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onSavePlan() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create or load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Remove old lines for this plan then append current plan milestones
        overwritePlanInFile(currentPlan);

        JOptionPane.showMessageDialog(this, "Plan saved to recovery_plans.csv", "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAddMilestone() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create/load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String deadline = txtDeadline.getText().trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter task title and deadline.", "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
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
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create/load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, "Select a milestone to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String deadline = txtDeadline.getText().trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter task title and deadline.", "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
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
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create/load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = milestoneTable.getSelectedRow();
        if (row < 0 || row >= currentPlan.getMilestones().size()) {
            JOptionPane.showMessageDialog(this, "Select a milestone to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Remove selected milestone?", "Confirm Remove",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            currentPlan.removeMilestone(row);
            refreshMilestoneTable();
            updateProgressLabel();
            clearMilestoneEditor();
        }
    }

    private void onApplyCompleted() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "Create/load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
            JOptionPane.showMessageDialog(this, "Create/load a plan first.", "No Plan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = getSelectedStudent();
        if (s == null) return;

        // Build action plan text
        StringBuilder actionPlan = new StringBuilder();
        for (Milestone m : currentPlan.getMilestones()) {
            actionPlan.append("- ").append(m.getTitle())
                    .append(" (").append(m.getDeadline()).append(")")
                    .append(m.isCompleted() ? " [DONE]" : "")
                    .append("\n");
        }

        boolean ok = emailService.sendRecoveryPlanEmail(
                s.getEmail(),
                s.getFullName(),
                currentPlan.getCourseId(),
                actionPlan.toString()
        );

        JOptionPane.showMessageDialog(this,
                ok ? "Email sent (if email config is correct)." : "Email failed (check EmailService config).",
                "Email",
                ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    // ---------------- Helpers ----------------

    private Student getSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0 || row >= students.size()) {
            JOptionPane.showMessageDialog(this, "Select a student first.", "No Student", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return students.get(row);
    }

    private void clearMilestoneEditor() {
        txtTitle.setText("");
        txtDeadline.setText("");
        chkCompleted.setSelected(false);
    }

    // ---------------- File persistence (simple CSV) ----------------

    private RecoveryPlan loadPlanFromFile(String studentId, String courseId) {
        List<String> lines = fileService.readTextFile("recovery_plans.csv");
        if (lines == null || lines.isEmpty()) return null;

        RecoveryPlan plan = new RecoveryPlan(studentId, courseId);
        boolean foundAny = false;

        for (String line : lines) {
            // studentId,courseId,title,deadline,completed
            String[] p = line.split(",", -1);
            if (p.length < 5) continue;

            String sid = p[0].trim();
            String cid = p[1].trim();

            if (sid.equalsIgnoreCase(studentId) && cid.equalsIgnoreCase(courseId)) {
                String title = p[2].trim();
                String deadline = p[3].trim();
                boolean completed = "true".equalsIgnoreCase(p[4].trim()) || "yes".equalsIgnoreCase(p[4].trim());

                Milestone ms = new Milestone(title, deadline);
                ms.setCompleted(completed);
                plan.addMilestone(ms);
                foundAny = true;
            }
        }

        return foundAny ? plan : null;
    }

    private void overwritePlanInFile(RecoveryPlan plan) {
        List<String> lines = fileService.readTextFile("recovery_plans.csv");
        List<String> kept = new ArrayList<>();

        // Keep all lines NOT belonging to this student+course
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length < 2) continue;

            String sid = p[0].trim();
            String cid = p[1].trim();

            if (!(sid.equalsIgnoreCase(plan.getStudentId()) && cid.equalsIgnoreCase(plan.getCourseId()))) {
                kept.add(line);
            }
        }

        // Rewrite file from scratch (overwrite)
        // We can do this by deleting file, then writing kept lines, then writing new plan lines.
        fileService.deleteFile("recovery_plans.csv");

        for (String k : kept) {
            fileService.writeTextFile(k, "recovery_plans.csv", true);
        }

        // Append current plan milestones
        for (Milestone m : plan.getMilestones()) {
            String line = plan.getStudentId() + "," +
                    plan.getCourseId() + "," +
                    cleanCsv(m.getTitle()) + "," +
                    cleanCsv(m.getDeadline()) + "," +
                    (m.isCompleted() ? "true" : "false");
            fileService.writeTextFile(line, "recovery_plans.csv", true);
        }
    }

    private String cleanCsv(String s) {
        // Basic beginner-safe cleanup: remove commas so our split() stays simple
        if (s == null) return "";
        return s.replace(",", " ").trim();
    }
}
