package model;

/**
 * Milestone
 * Represents a single recovery task with a deadline.
 */
public class Milestone {

    private String title;
    private String deadline;   // e.g. "Week 2" or "2025-04-10"
    private boolean completed;

    public Milestone(String title, String deadline) {
        this.title = safe(title);
        this.deadline = safe(deadline);
        this.completed = false;
    }

    // ---------- Getters ----------

    public String getTitle() {
        return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    // ---------- Setters ----------

    public void setTitle(String title) {
        this.title = safe(title);
    }

    public void setDeadline(String deadline) {
        this.deadline = safe(deadline);
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // ---------- Helpers ----------

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    @Override
    public String toString() {
        return title +
                " | Deadline: " + deadline +
                " | Status: " + (completed ? "Completed" : "Pending");
    }
}
