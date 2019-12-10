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
import static pl.pawelszopinski.todoapp.utils.ConstantSringProvider.*;

public class TaskController {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public NanoHTTPD.Response serveGetAllRequest() {
        List<Task> tasks;
        try {
            tasks = taskRepository.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        return objectToJson(tasks);
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
            return objectToJson(task);
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
            return newFixedLengthResponse(INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    INTERNAL_ERROR.getDescription() + ": can't read the request!");
        }

        String requestBody = new String(buffer).trim();

        Task requestTask;
        try {
            requestTask = objectMapper.readValue(requestBody, Task.class);
        } catch (IOException e) {
            return newFixedLengthResponse(BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    BAD_REQUEST.getDescription() + ": incorrect JSON format!");
        }

        long taskId;
        try {
            taskId = taskRepository.add(requestTask);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        return newFixedLengthResponse(OK, NanoHTTPD.MIME_PLAINTEXT, "Task has been added. Id: " + taskId + ".");
    }

    public NanoHTTPD.Response serveDeleteRequest(String id) {
        return serveSingleRowModification(DELETE, id, "Task deleted");
    }

    public NanoHTTPD.Response serveSetCompletedRequest(String id) {
        return serveSingleRowModification(SET_COMPLETED, id, "Status updated!");
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
            return newFixedLengthResponse(INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    INTERNAL_ERROR.getDescription() + ": attachment could not be added!");
        }

        return newFixedLengthResponse(OK, NanoHTTPD.MIME_PLAINTEXT, "The file has been attached to a task.");
    }

    private NanoHTTPD.Response serveSingleRowModification(String type, String id, String okResponse) {
        long taskId;
        try {
            taskId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return badId();
        }

        boolean modified = false;
        try {
            if (type.equals(SET_COMPLETED)) {
                modified = taskRepository.setCompleted(taskId);
            } else if (type.equals(DELETE)) {
                modified = taskRepository.delete(taskId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (!modified) return taskNotFound();

        return newFixedLengthResponse(OK, NanoHTTPD.MIME_PLAINTEXT, okResponse);
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

    private NanoHTTPD.Response objectToJson(Object o) {
        try {
            String response = objectMapper.writeValueAsString(o);
            return newFixedLengthResponse(OK, MIME_APP_JSON, response);
        } catch (JsonProcessingException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    INTERNAL_ERROR.getDescription() + ": can't convert data to JSON!");
        }
    }

    private NanoHTTPD.Response taskNotFound() {
        return newFixedLengthResponse(NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                NOT_FOUND.getDescription() +  ": the task does not exist!");
    }

    private NanoHTTPD.Response dbError() {
        return newFixedLengthResponse(INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                INTERNAL_ERROR.getDescription() + ": there is an issue with database connection!");
    }

    private NanoHTTPD.Response badId() {
        return newFixedLengthResponse(BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                BAD_REQUEST.getDescription() + ": can't parse id to number!");
    }
}
