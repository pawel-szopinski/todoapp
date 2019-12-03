package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.controller.TaskController;

import static fi.iki.elonen.NanoHTTPD.Method.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;

class RequestUrlMapper {

    private final static String TODOS = "todos";
    private final static String COMPLETE = "complete";

    private final TaskController taskController = new TaskController();

    NanoHTTPD.Response delegateRequest(NanoHTTPD.IHTTPSession session) {
        String[] uriArray = session.getUri().replaceFirst("/", "").split("/");

        if (GET.equals(session.getMethod()) && uriArray.length == 1 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveGetAllRequest();
        } else if (GET.equals(session.getMethod()) && uriArray.length == 2 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveGetSingleRequest(session, uriArray[1]);
        } else if (POST.equals(session.getMethod()) && uriArray.length == 1 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveAddRequest(session);
        } else if (DELETE.equals(session.getMethod()) && uriArray.length == 2 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveDeleteRequest(session, uriArray[1]);
        } else if (PUT.equals(session.getMethod()) && uriArray.length == 3 &&
                uriArray[0].equalsIgnoreCase(TODOS) && uriArray[2].equalsIgnoreCase(COMPLETE)) {
            return taskController.serveSetCompletedRequest(session, uriArray[1]);
        }

        return NanoHTTPD.newFixedLengthResponse(NOT_FOUND, "text/plain", "Not Found");
    }
}