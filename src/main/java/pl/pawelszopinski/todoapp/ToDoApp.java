package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.repository.DatabaseTaskRepository;
import pl.pawelszopinski.todoapp.repository.TaskRepository;

import java.io.IOException;

public class ToDoApp extends NanoHTTPD {

    private final RequestUrlMapper requestUrlMapper;

    public static void main(String[] args) {
        try {
            new ToDoApp(8080, new DatabaseTaskRepository()).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("The server has been started.");
        } catch (IOException e) {
            System.out.println("The server cannot start! Error: " + e);
        }
    }

    ToDoApp(int port, TaskRepository taskRepository) {
        super(port);
        this.requestUrlMapper = new RequestUrlMapper(taskRepository);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return requestUrlMapper.delegateRequest(session);
    }
}
