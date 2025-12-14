package model;

/**
 * Milestone
 * Represents one recovery task with a deadline.
 */
public class Milestone {

    private String title;
    private String deadline;
    private boolean completed;

    public Milestone(String title, String deadline) {
        this.title = clean(title);
        this.deadline = clean(deadline);
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
        this.title = clean(title);
    }

    public void setDeadline(String deadline) {
        this.deadline = clean(deadline);
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // ---------- Helper ----------

    // prevents null values and trims input
    private String clean(String s) {
        return (s == null) ? "" : s.trim();
    }

    @Override
    public String toString() {
        return title +
                " | Deadline: " + deadline +
                " | Status: " + (completed ? "Completed" : "Pending");
    }
}
