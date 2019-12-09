package pl.pawelszopinski.todoapp.type;

public class Task {

    private long id;
    private String name;
    private String assignee;
    private String description;
    private boolean completed;
    private short priority;

    Task() {
    }

    public Task(long id, String name, String assignee, String description, boolean completed, short priority) {
        this.id = id;
        this.name = name;
        this.assignee = assignee;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", assignee='" + assignee + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                '}';
    }
}
