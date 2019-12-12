package pl.pawelszopinski.todoapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import pl.pawelszopinski.todoapp.repository.TaskRepository;
import pl.pawelszopinski.todoapp.type.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static fi.iki.elonen.NanoHTTPD.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static pl.pawelszopinski.todoapp.utils.ConstantSringProvider.*;

public class TaskController {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Response serveGetAllRequest() {
        List<Task> tasks;
        try {
            tasks = taskRepository.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        return objectToJson(tasks);
    }

    public Response serveGetSingleRequest(String id) {
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
    public Response serveAddRequest(IHTTPSession session) {
        int lengthHeader = Integer.parseInt(session.getHeaders().get("content-length"));

        byte[] buffer = new byte[lengthHeader];

        try {
            session.getInputStream().read(buffer, 0, lengthHeader);
        } catch (IOException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT,
                    INTERNAL_ERROR.getDescription() + ": can't read the request!");
        }

        String requestBody = new String(buffer).trim();

        Task requestTask;
        try {
            requestTask = objectMapper.readValue(requestBody, Task.class);
        } catch (IOException e) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_PLAINTEXT,
                    BAD_REQUEST.getDescription() + ": incorrect JSON format!");
        }

        if (requestTask.getName() == null) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_PLAINTEXT,
                    BAD_REQUEST.getDescription() + ": missing 'name' value!");
        }

        if (requestTask.getPriority() < 1 && requestTask.getPriority() > 5) {
            return newFixedLengthResponse(BAD_REQUEST, MIME_PLAINTEXT,
                    BAD_REQUEST.getDescription() + ": incorrect/missing 'priority' value!");
        }

        long taskId;
        try {
            taskId = taskRepository.add(requestTask);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        return newFixedLengthResponse(CREATED, MIME_PLAINTEXT, "Task has been added. Id: " + taskId);
    }

    public Response serveDeleteRequest(String id) {
        return serveSingleRowModification(DELETE, id, "Task deleted");
    }

    public Response serveSetCompletedRequest(String id) {
        return serveSingleRowModification(SET_COMPLETED, id, "Status updated!");
    }

    public Response serveAddAttachments(IHTTPSession session, String id) {
        Task task;
        try {
            task = getTask(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return dbError();
        }

        if (task == null) return taskNotFound();

        List<FileItem> files;
        try {
            files = new NanoFileUpload(new DiskFileItemFactory()).parseRequest(session);
        } catch (FileUploadException e) {
            return newFixedLengthResponse(
                    INTERNAL_ERROR, MIME_PLAINTEXT, INTERNAL_ERROR.getDescription() + ": error when uploading files.");
        }

        int uploadedCount = 0;
        for (FileItem file : files) {
            try {
                String fileName = file.getName();
                byte[] fileContent = file.get();
                taskRepository.addAttachment(task.getId(), fileContent, fileName);

                uploadedCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (uploadedCount == files.size()) {
            return newFixedLengthResponse(OK, MIME_PLAINTEXT,
                    "Uploaded all files.");
        } else if (uploadedCount == 0) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT,
                    "No files were uploaded.");
        } else {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT,
                    "Errors occured! Only " + uploadedCount + " out of " + files.size() + " files were uploaded.");
        }
    }

    private Response serveSingleRowModification(String type, String id, String okResponse) {
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

        return newFixedLengthResponse(OK, MIME_PLAINTEXT, okResponse);
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

    private Response objectToJson(Object o) {
        try {
            String response = objectMapper.writeValueAsString(o);
            return newFixedLengthResponse(OK, MIME_APP_JSON, response);
        } catch (JsonProcessingException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT,
                    INTERNAL_ERROR.getDescription() + ": can't convert data to JSON!");
        }
    }

    private Response taskNotFound() {
        return newFixedLengthResponse(NOT_FOUND, MIME_PLAINTEXT,
                NOT_FOUND.getDescription() + ": the task does not exist!");
    }

    private Response dbError() {
        return newFixedLengthResponse(INTERNAL_ERROR, MIME_PLAINTEXT,
                INTERNAL_ERROR.getDescription() + ": there is an issue with database connection!");
    }

    private Response badId() {
        return newFixedLengthResponse(BAD_REQUEST, MIME_PLAINTEXT,
                BAD_REQUEST.getDescription() + ": can't parse id to number!");
    }
}
