package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.controller.TaskController;
import pl.pawelszopinski.todoapp.repository.TaskRepository;

import static fi.iki.elonen.NanoHTTPD.Method.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND;
import static pl.pawelszopinski.todoapp.utils.ConstantStringProvider.*;

class RequestUrlMapper {

    private final TaskController taskController;

    RequestUrlMapper(TaskRepository taskRepository) {
        this.taskController = new TaskController(taskRepository);
    }

    NanoHTTPD.Response delegateRequest(NanoHTTPD.IHTTPSession session) {
        String[] uriArray = session.getUri().replaceFirst("/", "").split("/");

        if (GET.equals(session.getMethod()) && uriArray.length == 1 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveGetAllRequest();
        } else if (GET.equals(session.getMethod()) && uriArray.length == 2 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveGetSingleRequest(uriArray[1]);
        } else if (POST.equals(session.getMethod()) && uriArray.length == 1 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveAddRequest(session);
        } else if (DELETE.equals(session.getMethod()) && uriArray.length == 2 &&
                uriArray[0].equalsIgnoreCase(TODOS)) {
            return taskController.serveDeleteRequest(uriArray[1]);
        } else if (PUT.equals(session.getMethod()) && uriArray.length == 3 &&
                uriArray[0].equalsIgnoreCase(TODOS) && uriArray[2].equalsIgnoreCase(SET_COMPLETED)) {
            return taskController.serveSetCompletedRequest(uriArray[1]);
        } else if (POST.equals(session.getMethod()) && uriArray.length == 3 &&
                uriArray[0].equalsIgnoreCase(TODOS) && uriArray[2].equalsIgnoreCase(ADD_ATTACH)) {
            return taskController.serveAddAttachments(session, uriArray[1]);
        } else if (GET.equals(session.getMethod()) && uriArray.length == 3 &&
                uriArray[0].equalsIgnoreCase(TODOS) && uriArray[2].equalsIgnoreCase(GET_ATTACH)) {
            return taskController.serveGetAttachment(session, uriArray[1]);
        }

        return NanoHTTPD.newFixedLengthResponse(NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, NOT_FOUND.getDescription());
    }
}