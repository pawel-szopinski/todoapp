package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.controller.TaskController;

import static fi.iki.elonen.NanoHTTPD.Method.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;

public class RequestUrlMapper {

    private final static String ADD_TASK_URL = "/todos/add";
    private final static String GET_ALL_TASK_URL = "/todos/getAll";
    private final static String GET_SINGLE_TASK_URL = "/todos/getSingle";
    private final static String DELETE_TASK_URL = "/todos/delete";
    private final static String PUT_COMPLETED_TASK_URL = "/todos/setCompleted";

    private TaskController taskController = new TaskController();

    public NanoHTTPD.Response delegateRequest(NanoHTTPD.IHTTPSession session) {
        if (GET.equals(session.getMethod()) && GET_ALL_TASK_URL.equals(session.getUri())) {
            return taskController.serveGetAllRequest();
        } else if (GET.equals(session.getMethod()) && GET_SINGLE_TASK_URL.equals(session.getUri())) {
            return taskController.serveGetSingleRequest(session);
        } else if (POST.equals(session.getMethod()) && ADD_TASK_URL.equals(session.getUri())) {
            return taskController.serveAddRequest(session);
        } else if (DELETE.equals(session.getMethod()) && DELETE_TASK_URL.equals(session.getUri())) {
            return taskController.serveDeleteRequest(session);
        } else if (PUT.equals(session.getMethod()) && PUT_COMPLETED_TASK_URL.equals(session.getUri())) {
            return taskController.serveSetCompletedRequest(session);
        }

        return NanoHTTPD.newFixedLengthResponse(NOT_FOUND, "text/plain", "Not Found");
    }
}
