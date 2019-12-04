package pl.pawelszopinski.todoapp.storage;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskStorage implements TaskStorage {

    private final Map<Long, Task> taskDb = new HashMap<>();
    private long nextIndex = 1;

    @Override
    public List<Task> getAll() {
        return new ArrayList<>(taskDb.values());
    }

    @Override
    public Task getSingle(long id) {
        return taskDb.get(id);
    }

    @Override
    public void add(Task task) {
        taskDb.put(task.getId(), task);
        nextIndex++;
    }

    @Override
    public void delete(long id) {
        taskDb.remove(id);
    }

    @Override
    public void setCompleted(long id) {
        Task task = taskDb.get(id);
        task.setCompleted(true);
        taskDb.put(id, task);
    }

    @Override
    public void addAttachment(long id, File file, String originalName) {
        try {
            Files.copy(file.toPath(),
                    new File("./src/main/resources/" + id + "/" + originalName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getNextId() {
        return nextIndex;
    }
}
