package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class ToDoApp extends NanoHTTPD {

    private final RequestUrlMapper requestUrlMapper = new RequestUrlMapper();

    public static void main(String[] args) {
        try {
            new ToDoApp(8080).start(5000, false);
            System.out.println("The server has been started.");
        } catch (IOException e) {
            System.out.println("The server cannot start! Error: " + e);
        }
    }

    ToDoApp(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return requestUrlMapper.delegateRequest(session);
    }
}
