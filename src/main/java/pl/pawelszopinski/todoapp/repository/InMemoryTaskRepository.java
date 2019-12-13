package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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
    public void addAttachment(long taskId, byte[] fileContent, String fileName) throws IOException {
        File dir = new File("./uploaded-files/" + taskId + "/");
        File file = new File(dir + "/" + fileName);

        if (file.exists()) throw new IOException("File with the same name already exists.");

        try {
            if (!dir.exists()) dir.mkdirs();
        } catch (SecurityException e) {
            throw new IOException("Can't create directory to save file in due to security reasons.");
        }

        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(fileContent);
        } catch (SecurityException e) {
            throw new IOException("Can't write file to disk due to security reasons.");
        }
    }

    @Override
    public byte[] getAttachment(long taskId, String attachmentName) throws IOException {
        File file = new File("./uploaded-files/" + taskId + "/" + attachmentName);

        if (!file.exists()) {
            return null;
        }

        return Files.readAllBytes(file.toPath());
    }

    private long getNextId() {
        return nextIndex;
    }
}
