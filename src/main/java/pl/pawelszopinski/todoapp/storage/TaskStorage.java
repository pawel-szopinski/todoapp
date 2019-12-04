package pl.pawelszopinski.todoapp.storage;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.util.List;

public interface TaskStorage {

    List<Task> getAll();

    Task getSingle(long id);

    void add(Task task);

    void delete(long id);

    void setCompleted(long id);

    void addAttachment(long id, File file, String originalName);
}
