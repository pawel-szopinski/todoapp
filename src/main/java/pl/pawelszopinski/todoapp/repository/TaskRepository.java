package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface TaskRepository {

    List<Task> getAll() throws SQLException;

    Task getSingle(long id) throws SQLException;

    long add(Task task) throws SQLException;

    boolean delete(long id) throws SQLException;

    boolean setCompleted(long id) throws SQLException;

    void addAttachment(long taskId, byte[] fileContent, String fileName) throws IOException, SQLException;

    byte[] getAttachment(long taskId, String attachmentName) throws SQLException, IOException;
}
