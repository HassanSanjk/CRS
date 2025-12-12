package course_recovery;

public class Milestone {
    
    private String title;
    private String deadline;
    private boolean completed;

    public Milestone(String title, String deadline) {
        this.title = title;
        this.deadline = deadline;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
