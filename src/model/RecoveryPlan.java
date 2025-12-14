package model;

import java.util.ArrayList;
import java.util.List;

/**
 * RecoveryPlan
 * A recovery plan for ONE student and ONE course.
 * It contains milestones (tasks) and becomes completed only when all milestones are done.
 */
public class RecoveryPlan {

    private String studentId;
    private String courseId;
    private List<Milestone> milestones;
    private boolean completed;

    public RecoveryPlan(String studentId, String courseId) {
        this.studentId = clean(studentId);
        this.courseId = clean(courseId);
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
        if (milestone == null) return;

        milestones.add(milestone);
        updateCompletionStatus();
    }

    public void removeMilestone(int index) {
        if (index < 0 || index >= milestones.size()) return;

        milestones.remove(index);
        updateCompletionStatus();
    }

    public void updateMilestoneStatus(int index, boolean isCompleted) {
        if (index < 0 || index >= milestones.size()) return;

        milestones.get(index).setCompleted(isCompleted);
        updateCompletionStatus();
    }

    // ---------- Completion Logic ----------

    
    //Plan is completed only if ALL milestones are completed.
    //If there are no milestones, the plan is not completed.
     
    private void updateCompletionStatus() {
        if (milestones.isEmpty()) {
            completed = false;
            return;
        }

        for (Milestone m : milestones) {
            if (m != null && !m.isCompleted()) {
                completed = false;
                return;
            }
        }

        completed = true;
    }

    // ---------- Helper ----------
    private String clean(String s) {
        return (s == null) ? "" : s.trim();
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
