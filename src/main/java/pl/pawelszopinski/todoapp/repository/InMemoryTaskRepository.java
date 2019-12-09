package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskRepository implements TaskRepository {

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
    public long add(Task task) {
        long id = getNextId();
        task.setId(id);
        taskDb.put(id, task);

        nextIndex++;

        return id;
    }

    @Override
    public boolean delete(long id) {
        if (!taskDb.containsKey(id)) {
            return false;
        }

        taskDb.remove(id);

        return true;
    }

    @Override
    public boolean setCompleted(long id) {
        if (!taskDb.containsKey(id)) {
            return false;
        }

        Task task = taskDb.get(id);
        task.setCompleted(true);
        taskDb.put(id, task);
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void addAttachment(long id, File file, String originalName) throws IOException {
        File dir = new File("./src/main/resources/" + id + "/");
        if (!dir.exists()) dir.mkdirs();

        Files.copy(file.toPath(),
                new File(dir.getPath() + "/" + originalName).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private long getNextId() {
        return nextIndex;
    }
}
