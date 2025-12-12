package course_recovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the recovery plan for a student.
 * Contains milestones and tracking of completion.
 */
public class RecoveryPlan {

    private String studentId;
    private String courseId;
    private List<Milestone> milestones;
    private boolean isCompleted;

    public RecoveryPlan(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.milestones = new ArrayList<>();
        this.isCompleted = false;
    }

    // Add milestone to the plan
    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
    }

    // Update milestone completion status
    public void updateMilestoneStatus(int index, boolean completed) {
        if (index >= 0 && index < milestones.size()) {
            milestones.get(index).setCompleted(completed);
        }
    }

    // Check if ALL milestones are completed
    public void checkCompletion() {
        isCompleted = milestones.stream()
                .allMatch(Milestone::isCompleted);
    }

    // MAIN METHOD USED BY ProgressTracker
    public List<Milestone> getMilestones() {
        return milestones;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
