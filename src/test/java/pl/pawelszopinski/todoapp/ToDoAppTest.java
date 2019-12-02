package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ToDoAppTest {

    ToDoApp app;

    @BeforeEach
    void setUp() {
        try {
            app = new ToDoApp(8080);
            app.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }

    @Test
    void testGetAllSuccessWhenStorageEmpty() {
        RestAssured.when()
                .get("/todos")
                .then()
                .statusCode(200);
    }
}