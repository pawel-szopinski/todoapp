package pl.pawelszopinski.todoapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.storage.InMemoryTaskStorage;
import pl.pawelszopinski.todoapp.type.Task;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class TaskController {

    private final InMemoryTaskStorage taskStorage = new InMemoryTaskStorage();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_APP_JSON = "application/json";

    public NanoHTTPD.Response serveAddRequest(NanoHTTPD.IHTTPSession session) {
        int lengthHeader = Integer.parseInt(session.getHeaders().get("content-length"));

        byte[] buffer = new byte[lengthHeader];

        long taskId;
        try {
            session.getInputStream().read(buffer, 0, lengthHeader);
            String requestBody = new String(buffer).trim();

            Task requestTask = objectMapper.readValue(requestBody, Task.class);
            taskId = taskStorage.getNextId();
            requestTask.setId(taskId);
            taskStorage.add(requestTask);
        } catch (Exception e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                    "Internal error - task could not be added!");
        }

        return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Task has been added. Id: " + taskId + ".");
    }

    public NanoHTTPD.Response serveGetAllRequest() {
        String response;

        try {
            response = objectMapper.writeValueAsString(taskStorage.getAll());
        } catch (JsonProcessingException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                    "Internal error - can't read all tasks collection!");
        }

        return newFixedLengthResponse(OK, MIME_APP_JSON, response);
    }

    public NanoHTTPD.Response serveGetSingleRequest(NanoHTTPD.IHTTPSession session, String id) {
        Task task = getTask(id);
        if (task != null) {
            try {
                String response = objectMapper.writeValueAsString(task);
                return newFixedLengthResponse(OK, MIME_APP_JSON, response);
            } catch (JsonProcessingException e) {
                return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                        "Internal error - could not return object as JSON!");
            }
        }

        return taskNotFound();
    }

    public NanoHTTPD.Response serveDeleteRequest(NanoHTTPD.IHTTPSession session, String id) {
        Task task = getTask(id);
        if (task != null) {
            taskStorage.delete(task.getId());
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Task deleted");
        }

        return taskNotFound();
    }

    public NanoHTTPD.Response serveSetCompletedRequest(NanoHTTPD.IHTTPSession session, String id) {
        Task task = getTask(id);
        if (task != null) {
            taskStorage.setCompleted(task.getId());
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Status updated");
        }

        return taskNotFound();
    }

    private Task getTask(String id) {
        long taskId;
        try {
            taskId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }

        return taskStorage.getSingle(taskId);
    }

    private NanoHTTPD.Response taskNotFound() {
        return newFixedLengthResponse(NOT_FOUND, MIME_TEXT_PLAIN, "Task not found in Db!");
    }
}
