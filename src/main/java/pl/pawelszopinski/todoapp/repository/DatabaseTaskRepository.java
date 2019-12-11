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
        } catch (SQLException e) {
            throw new SQLException("Can't pull data from db: " + e.getMessage());
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
        } catch (SQLException e) {
            throw new SQLException("Can't pull data from db: " + e.getMessage());
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

            if (statement.executeUpdate() == 1) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getLong(1);
                }
            } else {
                throw new SQLException("Zero records were added.");
            }
        } catch (SQLException e) {
            throw new SQLException("Can't insert data into db: " + e.getMessage());
        }

        return id;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return singleRowModification(id,
                "delete from task where id = ?",
                "Can't remove data from db: ");
    }

    @Override
    public boolean setCompleted(long id) throws SQLException {
        return singleRowModification(id,
                "update task set completed = true where id = ?",
                "Can't update data in db: ");
    }

    //TODO
    @Override
    public boolean addAttachment(long taskId, byte[] fileContent, String fileName) throws SQLException, IOException {
        String sql = "INSERT INTO attachment (name, file, task_id) VALUES (?, ?, ?)";
        boolean attached;

        try (Connection connection = establishDbConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             InputStream inputStream = new ByteArrayInputStream(fileContent)) {

            ps.setString(1, fileName);
            ps.setBinaryStream(2, inputStream, fileContent.length);
            ps.setLong(3, taskId);
            ps.executeUpdate();
            attached = true;

        }

        return attached;
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

    //TODO - check all catch
    private boolean singleRowModification(long id, String query, String errorMessage) throws SQLException {
        boolean taskUpdated = false;

        try (Connection connection = establishDbConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, id);

            if (statement.executeUpdate() == 1) {
                taskUpdated = true;
            }
//        } catch (SQLException e) {
//            throw new SQLException(errorMessage + e.getMessage());
        }

        return taskUpdated;
    }
}
