package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface TaskRepository {

    List<Task> getAll() throws SQLException;

    Task getSingle(long id) throws SQLException;

    long add(Task task) throws SQLException;

    boolean delete(long id) throws SQLException;

    boolean setCompleted(long id) throws SQLException;

    void addAttachment(long id, File file, String originalName) throws IOException, SQLException;
}
