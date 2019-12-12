package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DatabaseTaskRepository implements TaskRepository {

    @Override
    public List<Task> getAll() throws SQLException {
        String sql = "select * from task order by id";
        List<Task> tasks = new ArrayList<>();

        try (Connection connection = establishDbConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(getTaskObject(rs));
            }
        }

        return tasks;
    }

    @Override
    public Task getSingle(long id) throws SQLException {
        String sql = "select * from task where id = ?";
        Task task = null;

        try (Connection connection = establishDbConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    task = getTaskObject(rs);
                }
            }
        }

        return task;
    }

    @Override
    public long add(Task task) throws SQLException {
        String sql = "insert into task (name, assignee, description, completed, priority) values (?, ?, ?, ?, ?)";
        long id;

        try (Connection connection = establishDbConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, task.getName());
            statement.setString(2, task.getAssignee());
            statement.setString(3, task.getDescription());
            statement.setBoolean(4, task.isCompleted());
            statement.setShort(5, task.getPriority());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                rs.next();
                id = rs.getLong(1);
            }
        }

        return id;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return singleRowModificationById(id,
                "delete from task where id = ?");
    }

    @Override
    public boolean setCompleted(long id) throws SQLException {
        return singleRowModificationById(id,
                "update task set completed = true where id = ?");
    }

    @Override
    public void addAttachment(long taskId, byte[] fileContent, String fileName) throws SQLException, IOException {
        String sql = "INSERT INTO attachment (name, file, task_id) VALUES (?, ?, ?)";

        try (Connection connection = establishDbConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             InputStream inputStream = new ByteArrayInputStream(fileContent)) {

            statement.setString(1, fileName);
            statement.setBinaryStream(2, inputStream, fileContent.length);
            statement.setLong(3, taskId);

            statement.executeUpdate();
        }
    }

    private Connection establishDbConnection() throws SQLException {
        Connection connection;
        try {
            ResourceBundle reader = ResourceBundle.getBundle("dbconfig");

            connection = DriverManager.getConnection(
                    reader.getString("db.url"),
                    reader.getString("db.username"),
                    reader.getString("db.password"));
        } catch (SQLException | MissingResourceException e) {
            throw new SQLException("Can't connect to the database: " + e.getMessage());
        }

        return connection;
    }

    private Task getTaskObject(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("assignee"),
                rs.getString("description"),
                rs.getBoolean("completed"),
                rs.getShort("priority"));
    }

    private boolean singleRowModificationById(long id, String query) throws SQLException {
        boolean taskUpdated = false;

        try (Connection connection = establishDbConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);

            if (statement.executeUpdate() == 1) {
                taskUpdated = true;
            }
        }

        return taskUpdated;
    }
}
