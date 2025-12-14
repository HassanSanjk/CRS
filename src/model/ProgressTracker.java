package model;

import java.util.List;

// Calculates progress percentage for a recovery plan.
public class ProgressTracker {


    //Returns progress as a percentage
    public static int calculateProgress(RecoveryPlan plan) {

        if (plan == null) {
            return 0;
        }

        List<Milestone> milestones = plan.getMilestones();

        if (milestones == null || milestones.isEmpty()) {
            return 0;
        }

        int completed = 0;

        for (Milestone m : milestones) {
            if (m != null && m.isCompleted()) {
                completed++;
            }
        }

        return (completed * 100) / milestones.size();
    }
}
