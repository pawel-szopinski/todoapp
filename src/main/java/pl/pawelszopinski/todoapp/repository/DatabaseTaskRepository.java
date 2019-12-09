package pl.pawelszopinski.todoapp.repository;

import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DatabaseTaskRepository implements TaskRepository {

    private Connection connection;

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

    private void closeDbConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Can't close connection to db: " + e.getMessage());
        }
    }

    @Override
    public List<Task> getAll() throws SQLException {
        connection = establishDbConnection();

        List<Task> tasks = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from task order by id");

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("assignee"),
                        rs.getString("description"),
                        rs.getBoolean("completed"),
                        rs.getShort("priority")));
            }

            rs.close();
            statement.close();
        } catch (SQLException e) {
            throw new SQLException("Can't pull data from db: " + e.getMessage());
        } finally {
            closeDbConnection();
        }

        return tasks;
    }

    @Override
    public Task getSingle(long id) throws SQLException {
        connection = establishDbConnection();

        Task task = null;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from task where id = ?");

            statement.setLong(1, id);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                task = new Task(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("assignee"),
                        rs.getString("description"),
                        rs.getBoolean("completed"),
                        rs.getShort("priority"));
            }

            rs.close();
            statement.close();
        } catch (SQLException e) {
            throw new SQLException("Can't pull data from db: " + e.getMessage());
        } finally {
            closeDbConnection();
        }

        return task;
    }

    @Override
    public long add(Task task) throws SQLException {
        connection = establishDbConnection();

        long id;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into task (name, assignee, description, completed, priority) values (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, task.getName());
            statement.setString(2, task.getAssignee());
            statement.setString(3, task.getDescription());
            statement.setBoolean(4, task.isCompleted());
            statement.setShort(5, task.getPriority());

            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            rs.next();

            id = rs.getLong(1);

            rs.close();
            statement.close();
        } catch (SQLException e) {
            throw new SQLException("Can't insert data into db: " + e.getMessage());
        } finally {
            closeDbConnection();
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

    private boolean singleRowModification(long id, String query, String errorMessage) throws SQLException {
        connection = establishDbConnection();

        boolean taskUpdated = false;

        try {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, id);

            if (statement.executeUpdate() > 0) {
                taskUpdated = true;
            }

            statement.close();
        } catch (SQLException e) {
            throw new SQLException(errorMessage + e.getMessage());
        } finally {
            closeDbConnection();
        }

        return taskUpdated;
    }

    @Override
    public void addAttachment(long id, File file, String originalName) throws IOException {

    }
}
