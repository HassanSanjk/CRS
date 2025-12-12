package course_recovery;

import java.util.List;
import course_recovery.Milestone;

public class ProgressTracker {

    public static int calculateProgress(RecoveryPlan plan) {

        List<Milestone> list = plan.getMilestones();

        if (list == null || list.isEmpty()) {
            return 0;
        }

        long completed = list.stream()
                             .filter(Milestone::isCompleted)
                             .count();

        return (int) ((completed * 100) / list.size());
    }
}
