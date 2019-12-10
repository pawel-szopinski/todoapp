package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pawelszopinski.todoapp.repository.InMemoryTaskRepository;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;

class ToDoAppTest {

    AppServer server;

    @BeforeEach
    void setUp() {
        try {
            server = new AppServer(8080, new InMemoryTaskRepository());
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetAllSuccessWhenEmpty() {
        RestAssured.when()
                .get("/todos")
                .then()
                .body("", hasSize(0))
                .statusCode(200);
    }
}