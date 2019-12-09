package pl.pawelszopinski.todoapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.repository.TaskRepository;
import pl.pawelszopinski.todoapp.type.Task;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class TaskController {

    private final TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_APP_JSON = "application/json";

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public NanoHTTPD.Response serveGetAllRequest() {
        String response;

        List<Task> tasks;
        try {
            tasks = taskRepository.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        try {
            response = objectMapper.writeValueAsString(tasks);
        } catch (JsonProcessingException e) {
            return cantConvertToJson();
        }

        return newFixedLengthResponse(OK, MIME_APP_JSON, response);
    }

    public NanoHTTPD.Response serveGetSingleRequest(String id) {
        Task task;
        try {
            task = getTask(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (task != null) {
            try {
                String response = objectMapper.writeValueAsString(task);
                return newFixedLengthResponse(OK, MIME_APP_JSON, response);
            } catch (JsonProcessingException e) {
                return cantConvertToJson();
            }
        }

        return taskNotFound();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public NanoHTTPD.Response serveAddRequest(NanoHTTPD.IHTTPSession session) {
        int lengthHeader = Integer.parseInt(session.getHeaders().get("content-length"));

        byte[] buffer = new byte[lengthHeader];

        try {
            session.getInputStream().read(buffer, 0, lengthHeader);
        } catch (IOException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                    "Internal error - can't read the request!");
        }

        String requestBody = new String(buffer).trim();

        Task requestTask;
        try {
            requestTask = objectMapper.readValue(requestBody, Task.class);
        } catch (IOException e) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_TEXT_PLAIN,
                    "Bad Request - incorrect JSON format!");
        }

        long taskId;
        try {
            taskId = taskRepository.add(requestTask);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Task has been added. Id: " + taskId + ".");
    }

    public NanoHTTPD.Response serveDeleteRequest(String id) {
        Task task;
        try {
            task = getTask(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (task != null) {
            try {
                taskRepository.delete(task.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Task deleted");
        }

        return taskNotFound();
    }

    public NanoHTTPD.Response serveSetCompletedRequest(String id) {
        Task task;
        try {
            task = getTask(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (task != null) {
            try {
                taskRepository.setCompleted(task.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Status updated");
        }

        return taskNotFound();
    }

    public NanoHTTPD.Response serveAddAttachment(NanoHTTPD.IHTTPSession session, String id) {
        Task task;
        try {
            task = getTask(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (task == null) return taskNotFound();

        Map<String, List<String>> params = session.getParameters();

        Map<String, String> files = new HashMap<>();

        try {
            session.parseBody(files);

            int paramIndex = 0;
            for (String key : files.keySet()) {
                String location = files.get(key);

                File tempFile = new File(location);

                String originalName = params.get("file").get(paramIndex);
                paramIndex++;

                taskRepository.addAttachment(task.getId(), tempFile, originalName);
            }
        } catch (IOException | NanoHTTPD.ResponseException | SQLException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                    "Internal error - attachment could not be added!");
        }

        return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "The file has been attached to a task.");
    }

    private Task getTask(String id) throws SQLException {
        long taskId;
        try {
            taskId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }

        return taskRepository.getSingle(taskId);
    }

    private NanoHTTPD.Response cantConvertToJson() {
        return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                "Internal error - can't convert data to JSON!");
    }

    private NanoHTTPD.Response taskNotFound() {
        return newFixedLengthResponse(NOT_FOUND, MIME_TEXT_PLAIN,
                "Task not found in repository!");
    }

    private NanoHTTPD.Response dbError() {
        return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                "Internal error - there was an issue with database connection!");
    }
}
