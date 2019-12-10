package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.repository.DatabaseTaskRepository;

import java.io.IOException;

public class ToDoApp {

    public static void main(String[] args) {
        try {
            new AppServer(8080, new DatabaseTaskRepository()).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("The server has been started.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("The server cannot start! Error: " + e);
        }
    }
}
