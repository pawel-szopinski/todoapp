package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.controller.TaskController;

import static fi.iki.elonen.NanoHTTPD.Method.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;

public class RequestUrlMapper {

    private final static String TODOS = "/todos";
    private final static String TODOS_PARAM = "/todos/";
    private final static String COMPLETE = "/complete";

    private final TaskController taskController = new TaskController();

    public NanoHTTPD.Response delegateRequest(NanoHTTPD.IHTTPSession session) {
        if (GET.equals(session.getMethod()) && TODOS.equals(session.getUri())) {
            return taskController.serveGetAllRequest();
        } else if (GET.equals(session.getMethod()) && session.getUri().startsWith(TODOS_PARAM)) {
            return taskController.serveGetSingleRequest(session);
        } else if (POST.equals(session.getMethod()) && TODOS.equals(session.getUri())) {
            return taskController.serveAddRequest(session);
        } else if (DELETE.equals(session.getMethod()) && session.getUri().startsWith(TODOS_PARAM)) {
            return taskController.serveDeleteRequest(session);
        } else if (PUT.equals(session.getMethod()) && session.getUri().startsWith(TODOS_PARAM) &&
            session.getUri().endsWith(COMPLETE)) {
            return taskController.serveSetCompletedRequest(session);
        }

        return NanoHTTPD.newFixedLengthResponse(NOT_FOUND, "text/plain", "Not Found");
    }
}