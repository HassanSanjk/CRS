package test;

import ui.StudentCourseManagementPanel;
import javax.swing.*;

public class TestStudentModule {

    public static void main(String[] args) {
        // Run Swing on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student & Course Management - Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Your panel
            frame.setContentPane(new StudentCourseManagementPanel());

            frame.pack();                 // size to fit content
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);
        });
    }
}
