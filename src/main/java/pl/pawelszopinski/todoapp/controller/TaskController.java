package pl.pawelszopinski.todoapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.storage.InMemoryTaskStorage;
import pl.pawelszopinski.todoapp.type.Task;

import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class TaskController {

    private InMemoryTaskStorage taskStorage = new InMemoryTaskStorage();

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String TASK_ID_PARAM_NAME = "id";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_APP_JSON = "application/json";

    public NanoHTTPD.Response serveAddRequest(NanoHTTPD.IHTTPSession session) {
        long taskId = taskStorage.getNextId();

        int lengthHeader = Integer.parseInt(session.getHeaders().get("content-length"));

        byte[] buffer = new byte[lengthHeader];

        try {
            session.getInputStream().read(buffer, 0, lengthHeader);
            String requestBody = new String(buffer).trim();
            Task requestTask = objectMapper.readValue(requestBody, Task.class);
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

    public NanoHTTPD.Response serveGetSingleRequest(NanoHTTPD.IHTTPSession session) {
        String taskIdFromUri = session.getUri().replaceAll("\\D+","");
        long taskId;
        try {
            taskId = Long.parseLong(taskIdFromUri);
        } catch (NumberFormatException e) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_TEXT_PLAIN,
                    "Bad request - missing " + TASK_ID_PARAM_NAME);
        }

        Task task = taskStorage.getSingle(taskId);
        if (task != null) {
            try {
                String response = objectMapper.writeValueAsString(task);
                return newFixedLengthResponse(OK, MIME_APP_JSON, response);
            } catch (JsonProcessingException e) {
                return newFixedLengthResponse(INTERNAL_ERROR, MIME_TEXT_PLAIN,
                        "Internal error - could not return object as JSON!");
            }
        }

        return newFixedLengthResponse(NOT_FOUND, MIME_TEXT_PLAIN, "Task not found in Db!");
    }

    public NanoHTTPD.Response serveDeleteRequest(NanoHTTPD.IHTTPSession session) {
        String taskIdFromUri = session.getUri().replaceAll("\\D+","");
        long taskId;
        try {
            taskId = Long.parseLong(taskIdFromUri);
        } catch (NumberFormatException e) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_TEXT_PLAIN,
                    "Bad request - missing " + TASK_ID_PARAM_NAME);
        }

        Task task = taskStorage.getSingle(taskId);
        if (task != null) {
            taskStorage.delete(taskId);
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Task deleted");
        }

        return newFixedLengthResponse(NOT_FOUND, MIME_TEXT_PLAIN, "Task not found in Db!");
    }

    public NanoHTTPD.Response serveSetCompletedRequest(NanoHTTPD.IHTTPSession session) {
        String taskIdFromUri = session.getUri().replaceAll("\\D+","");
        long taskId;
        try {
            taskId = Long.parseLong(taskIdFromUri);
        } catch (NumberFormatException e) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_TEXT_PLAIN,
                    "Bad request - missing " + TASK_ID_PARAM_NAME);
        }

        Task task = taskStorage.getSingle(taskId);
        if (task != null) {
            taskStorage.setCompleted(taskId);
            return newFixedLengthResponse(OK, MIME_TEXT_PLAIN, "Status updated");
        }

        return newFixedLengthResponse(NOT_FOUND, MIME_TEXT_PLAIN, "Task not found in Db!");
    }
}
