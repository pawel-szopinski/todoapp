package pl.pawelszopinski.todoapp;


import fi.iki.elonen.NanoHTTPD;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.pawelszopinski.todoapp.repository.InMemoryTaskRepository;

import java.io.IOException;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.*;

class AppServerTest {

    private static final String TASK_ONE_JSON = "{\n" +
            "    \"name\": \"First task\",\n" +
            "    \"description\": \"Do something\",\n" +
            "    \"priority\": 1,\n" +
            "    \"assignee\": \"Arnold\"\n" +
            "}";

    private static final String TASK_TWO_JSON = "{\n" +
            "    \"name\": \"Second task\",\n" +
            "    \"description\": \"Do something else\",\n" +
            "    \"priority\": 2,\n" +
            "    \"assignee\": \"John\"\n" +
            "}";

    private static final String INVALID_JSON = "{\n" +
            "    \"name\": \"First task\",\n" +
            "    \"description\": \"Do something\",\n" +
            "    \"priority\":" +
            "    \"assignee\": \"Arnold\"\n" +
            "}";

    private static final String INVALID_TASK_JSON = "{\n" +
            "    \"description\": \"Do something\",\n" +
            "    \"priority\": 1,\n" +
            "    \"assignee\": \"Arnold\"\n" +
            "}";

    private static final int SERVER_PORT = 8080;

    private AppServer appServer;

    @BeforeAll
    static void beforeAll() {
        RestAssured.port = SERVER_PORT;
    }

    @BeforeEach
    void beforeEach() throws IOException {
        appServer = new AppServer(SERVER_PORT, new InMemoryTaskRepository());
        appServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @AfterEach
    void afterEach() {
        appServer.stop();
    }

    @Test
    void addTaskShouldReturnSuccess() {
        with()
                .body(TASK_ONE_JSON)
                .when()
                .post("/todos")
                .then()
                .statusCode(201)
                .body(startsWith("Task has been added"));
    }

    @Test
    void addWithInvalidJsonShouldReturnBadRequest() {
        with()
                .body(INVALID_JSON)
                .when()
                .post("/todos")
                .then()
                .statusCode(400)
                .body(endsWith("incorrect JSON format!"));
    }

    @Test
    void addTaskWithoutNameShouldReturnBadRequest() {
        with()
                .body(INVALID_TASK_JSON)
                .when()
                .post("/todos")
                .then()
                .statusCode(400)
                .body(endsWith("missing 'name' value!"));
    }

    @Test
    void getAllShouldReturnEmptyList() {
        with()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void getAllShouldReturnTaskFromRepository() {
        addTaskAndGetId(TASK_ONE_JSON);

        with()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("id", hasItem(1))
                .body("name", hasItem("First task"))
                .body("description", hasItem("Do something"))
                .body("priority", hasItem(1))
                .body("assignee", hasItem("Arnold"))
                .body("completed", hasItem(false));
    }

    @Test
    void getAllShouldReturnTwoTasksFromRepository() {
        addTaskAndGetId(TASK_ONE_JSON);
        addTaskAndGetId(TASK_TWO_JSON);

        with()
                .get("/todos")
                .then()
                .statusCode(200)
                .body("", hasSize(2))
                .body("id", hasItems(1, 2))
                .body("name", hasItems("First task", "Second task"))
                .body("description", hasItems("Do something", "Do something else"))
                .body("priority", hasItems(1, 2))
                .body("assignee", hasItems("Arnold", "John"))
                .body("completed", hasItems(false, false));
    }

    @Test
    void getSingleValidatesIdExists() {
        with()
                .get("/todos/1")
                .then()
                .statusCode(404)
                .body(endsWith("the task does not exist!"));
    }

//    @Test
//    void getSingleValidatesNoMoreThanOneIdParameterIsPassed() {
//        with()
//                .param("id", 1)
//                .param("id", 2)
//                .get("/todos/getSingle")
//                .then()
//                .statusCode(400)
//                .body(equalTo("More than one `id` parameter"));
//    }

//    @Test
//    void getSingleShouldReturnNotFound() {
//        with()
//                .param("id", 123)
//                .get("/todos/getSingle")
//                .then()
//                .statusCode(404)
//                .body(equalTo("Task with given id doesn't exist"));
//    }

    @Test
    void getSingleShouldReturnTask() {
        addTaskAndGetId(TASK_ONE_JSON);

        with()
                .get("/todos/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("First task"))
                .body("description", equalTo("Do something"))
                .body("priority", equalTo(1))
                .body("assignee", equalTo("Arnold"))
                .body("completed", equalTo(false));
    }

    private long addTaskAndGetId(String jsonBody) {
        String response = with()
                .body(jsonBody)
                .post("/todos")
                .then()
                .statusCode(201)
                .extract().asString();

        return Long.parseLong(response.substring(response.lastIndexOf(" ") + 1));
    }
}
