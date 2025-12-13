package model;

import java.util.ArrayList;
import java.util.List;


/**
 * RecoveryPlan
 * Represents a course recovery plan for a student.
 * Contains milestones and tracks overall completion status.
 */
public class RecoveryPlan {

    private String studentId;
    private String courseId;
    private List<Milestone> milestones;
    private boolean completed;

    public RecoveryPlan(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.milestones = new ArrayList<>();
        this.completed = false;
    }

    // ---------- Getters ----------

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public boolean isCompleted() {
        return completed;
    }

    // ---------- Milestone Management ----------

    public void addMilestone(Milestone milestone) {
        if (milestone != null) {
            milestones.add(milestone);
            updateCompletionStatus();
        }
    }

    public void removeMilestone(int index) {
        if (index >= 0 && index < milestones.size()) {
            milestones.remove(index);
            updateCompletionStatus();
        }
    }

    public void updateMilestoneStatus(int index, boolean isCompleted) {
        if (index >= 0 && index < milestones.size()) {
            milestones.get(index).setCompleted(isCompleted);
            updateCompletionStatus();
        }
    }

    // ---------- Completion Logic ----------

    /**
     * Updates overall plan completion.
     * Plan is completed only if ALL milestones are completed.
     */
    private void updateCompletionStatus() {
        if (milestones.isEmpty()) {
            completed = false;
            return;
        }

        for (Milestone m : milestones) {
            if (!m.isCompleted()) {
                completed = false;
                return;
            }
        }
        completed = true;
    }

    @Override
    public String toString() {
        return "RecoveryPlan{" +
                "studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", milestones=" + milestones.size() +
                ", completed=" + completed +
                '}';
    }
}
