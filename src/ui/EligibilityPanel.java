package ui;

import repository.*;
import services.EligibilityService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class EligibilityPanel extends JPanel {

    private final EligibilityService service;

    private JTextField searchField;
    private JComboBox<String> filterBox;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private JButton refreshBtn;
    private JButton registerBtn;
    private JButton enterGradesBtn;

    public EligibilityPanel(EligibilityService service) {
        this.service = service;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Search (ID/Name):"));
        searchField = new JTextField(18);
        top.add(searchField);

        top.add(new JLabel("Filter:"));
        filterBox = new JComboBox<>(new String[]{"All", "Eligible", "Not Eligible", "Pending"});
        top.add(filterBox);

        refreshBtn = new JButton("Refresh / Recalculate");
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);

        // table
        model = new DefaultTableModel(
                new Object[]{"StudentID", "Name", "CGPA", "Failed", "Status", "Reason", "Registered"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        enterGradesBtn = new JButton("Enter Grades");
        enterGradesBtn.setEnabled(false);

        registerBtn = new JButton("Register Next Level");
        registerBtn.setEnabled(false);

        bottom.add(enterGradesBtn);
        bottom.add(registerBtn);

        add(bottom, BorderLayout.SOUTH);

        // events
        refreshBtn.addActionListener(e -> loadData());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        filterBox.addActionListener(e -> applyFilters());

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        registerBtn.addActionListener(e -> doRegister());
        enterGradesBtn.addActionListener(e -> openGradeEntry());
    }

    private void loadData() {
        model.setRowCount(0);

        List<EligibilityService.EligibilityRow> rows = service.computeAll();
        for (EligibilityService.EligibilityRow r : rows) {
            String cgpaStr = (r.cgpa == null) ? "N/A" : String.valueOf(r.cgpa);

            model.addRow(new Object[]{
                    r.studentId,
                    r.name,
                    cgpaStr,
                    r.failedCourses,
                    r.status.toString(),   // ELIGIBLE / NOT_ELIGIBLE / PENDING_RESULTS
                    r.reason,
                    r.registered ? "YES" : "NO"
            });
        }

        applyFilters();
        updateButtons();
    }

    private void applyFilters() {
        final String q = searchField.getText().trim().toLowerCase();
        final String filter = (filterBox.getSelectedItem() == null)
                ? "All"
                : String.valueOf(filterBox.getSelectedItem());

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                String id = e.getStringValue(0).toLowerCase();
                String name = e.getStringValue(1).toLowerCase();
                String status = e.getStringValue(4); // already ELIGIBLE / NOT_ELIGIBLE / PENDING_RESULTS

                boolean matchesSearch = q.isEmpty() || id.contains(q) || name.contains(q);

                boolean matchesFilter = true;
                if ("Eligible".equals(filter)) matchesFilter = "ELIGIBLE".equals(status);
                else if ("Not Eligible".equals(filter)) matchesFilter = "NOT_ELIGIBLE".equals(status);
                else if ("Pending".equals(filter)) matchesFilter = "PENDING_RESULTS".equals(status);

                return matchesSearch && matchesFilter;
            }
        });
    }

    private void updateButtons() {
        int viewRow = table.getSelectedRow();
        boolean hasSelection = viewRow >= 0;

        enterGradesBtn.setEnabled(hasSelection);

        if (!hasSelection) {
            registerBtn.setEnabled(false);
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        String status = String.valueOf(model.getValueAt(row, 4));
        String registered = String.valueOf(model.getValueAt(row, 6));

        boolean canRegister = "ELIGIBLE".equals(status) && "NO".equalsIgnoreCase(registered);
        registerBtn.setEnabled(canRegister);
    }

    private void openGradeEntry() {
        // simple repos for grade entry
        StudentRepository studentRepo = new StudentRepository("data/student_information.csv");
        CourseRepository courseRepo = new CourseRepository("data/course_assessment_information.csv");
        GradeFileHandler gradeFile = new GradeFileHandler("data/grades.txt");

        GradeEntryFrame frame = new GradeEntryFrame(studentRepo, courseRepo, gradeFile);

        // refresh eligibility after closing grade entry
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { loadData(); }
            @Override public void windowClosing(WindowEvent e) { loadData(); }
        });

        frame.setVisible(true);
    }

    private void doRegister() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;

        int row = table.convertRowIndexToModel(viewRow);
        String studentId = String.valueOf(model.getValueAt(row, 0));
        String name = String.valueOf(model.getValueAt(row, 1));
        String status = String.valueOf(model.getValueAt(row, 4));
        String reason = String.valueOf(model.getValueAt(row, 5));

        if (!"ELIGIBLE".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot register.\nReason: " + reason,
                    "Not Allowed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Register " + name + " (" + studentId + ") for next level?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = service.registerIfEligible(studentId);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful for " + studentId,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Student is not eligible. Registration denied.\nReason: " + reason,
                    "Not Allowed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
