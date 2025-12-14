package ui;

import model.Course;
import model.Grade;
import repository.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradeEntryFrame extends JFrame {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final GradeFileHandler gradeFile;

    private JTable studentTable;
    private DefaultTableModel studentModel;

    private JComboBox<CourseItem> c1, c2, c3;
    private JComboBox<String> g1, g2, g3;
    private JComboBox<Integer> attemptBox;

    private JLabel selectedStudentLabel;

    public GradeEntryFrame(StudentRepository studentRepo,
                           CourseRepository courseRepo,
                           GradeFileHandler gradeFile) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.gradeFile = gradeFile;

        setTitle("Grade Entry - CRS");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // load data from files
        List<StudentRepository.StudentMini> students = studentRepo.loadStudents();
        List<Course> courses = courseRepo.loadAllCourses();

        // Left side: students list (table)
        studentModel = new DefaultTableModel(new Object[]{"StudentID", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (StudentRepository.StudentMini s : students) {
            studentModel.addRow(new Object[]{s.studentId, s.name});
        }

        studentTable = new JTable(studentModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(studentTable);

        // Build combo items from courses
        CourseItem[] items = buildCourseItems(courses);

        // grade entry form
        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        selectedStudentLabel = new JLabel("Selected Student: (none)");
        selectedStudentLabel.setFont(selectedStudentLabel.getFont().deriveFont(Font.BOLD));
        right.add(selectedStudentLabel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 3, 10, 10));

        form.add(new JLabel("Attempt (1-3):"));
        attemptBox = new JComboBox<>(new Integer[]{1, 2, 3});
        form.add(attemptBox);
        form.add(new JLabel(""));

        form.add(new JLabel("Course 1:"));
        c1 = new JComboBox<>(items);
        form.add(c1);
        g1 = gradeCombo();
        form.add(g1);

        form.add(new JLabel("Course 2:"));
        c2 = new JComboBox<>(items);
        form.add(c2);
        g2 = gradeCombo();
        form.add(g2);

        form.add(new JLabel("Course 3:"));
        c3 = new JComboBox<>(items);
        form.add(c3);
        g3 = gradeCombo();
        form.add(g3);

        JButton saveBtn = new JButton("Save 3 Course Grades");
        JButton clearBtn = new JButton("Clear Selection");

        form.add(saveBtn);
        form.add(clearBtn);
        form.add(new JLabel(""));

        right.add(form, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, right);
        split.setDividerLocation(380);
        add(split);

        // Events
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                String sid = String.valueOf(studentModel.getValueAt(row, 0));
                String name = String.valueOf(studentModel.getValueAt(row, 1));
                selectedStudentLabel.setText("Selected Student: " + sid + (name.isEmpty() ? "" : (" - " + name)));
            }
        });

        saveBtn.addActionListener(e -> saveThreeGrades());
        clearBtn.addActionListener(e -> {
            studentTable.clearSelection();
            selectedStudentLabel.setText("Selected Student: (none)");
        });
    }

    private CourseItem[] buildCourseItems(List<Course> courses) {
        List<CourseItem> list = new ArrayList<CourseItem>();

        for (Course c : courses) {
            if (c != null && c.getCourseId() != null && !c.getCourseId().trim().isEmpty()) {
                list.add(new CourseItem(c));
            }
        }

        return list.toArray(new CourseItem[0]);
    }

    private JComboBox<String> gradeCombo() {
        return new JComboBox<>(new String[]{"A", "A-", "B+", "B", "C+", "C", "D", "F"});
    }

    private void saveThreeGrades() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String studentId = String.valueOf(studentModel.getValueAt(row, 0)).trim();

        Integer attemptObj = (Integer) attemptBox.getSelectedItem();
        int attempt = (attemptObj == null) ? 1 : attemptObj;

        CourseItem item1 = (CourseItem) c1.getSelectedItem();
        CourseItem item2 = (CourseItem) c2.getSelectedItem();
        CourseItem item3 = (CourseItem) c3.getSelectedItem();

        if (item1 == null || item2 == null || item3 == null) {
            JOptionPane.showMessageDialog(this, "Courses must be selected.", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id1 = item1.course.getCourseId();
        String id2 = item2.course.getCourseId();
        String id3 = item3.course.getCourseId();

        // must be 3 different courses
        if (same(id1, id2) || same(id1, id3) || same(id2, id3)) {
            JOptionPane.showMessageDialog(this, "Choose 3 different courses (no duplicates).", "Validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String gl1 = (String) g1.getSelectedItem();
        String gl2 = (String) g2.getSelectedItem();
        String gl3 = (String) g3.getSelectedItem();

        try {
            // save/update in grades.txt
            gradeFile.upsert(new Grade(studentId, id1, attempt, gl1));
            gradeFile.upsert(new Grade(studentId, id2, attempt, gl2));
            gradeFile.upsert(new Grade(studentId, id3, attempt, gl3));

            JOptionPane.showMessageDialog(this,
                    "Saved 3 grades for " + studentId + " (Attempt " + attempt + ").\nWritten to data/grades.txt",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean same(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    // Wrapper so combo shows "ID - Name"
    private static class CourseItem {
        private final Course course;

        CourseItem(Course course) {
            this.course = course;
        }

        @Override
        public String toString() {
            String id = course.getCourseId();
            String name = course.getCourseName();

            if (name == null || name.trim().isEmpty()) return id;
            return id + " - " + name;
        }
    }
}
