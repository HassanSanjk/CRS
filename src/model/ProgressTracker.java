package model;

import java.util.List;

/**
 * ProgressTracker
 * Calculates recovery progress percentage based on completed milestones.
 */
public class ProgressTracker {

    /**
     * Calculates progress as a percentage (0–100).
     *
     * @param plan RecoveryPlan
     * @return progress percentage
     */
    public static int calculateProgress(RecoveryPlan plan) {

        if (plan == null) {
            return 0;
        }

        List<Milestone> milestones = plan.getMilestones();

        if (milestones == null || milestones.isEmpty()) {
            return 0;
        }

        int completedCount = 0;

        for (Milestone m : milestones) {
            if (m.isCompleted()) {
                completedCount++;
            }
        }

        return (completedCount * 100) / milestones.size();
    }
}
