package org.slothmq;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slothmq.server.user.User;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class BaseIntegrationTest {
    @BeforeAll
    public static void beforeAll() throws Exception {
       BaseIntegrationTestSingletonHolder.startServer();
    }

    protected String getToken() {
        User user = new User();
        user.setUserName("admin");
        user.setPasskey("admin");
        return given()
                .body(user)
                .when()
                .post("http://localhost:8080/api/login")
                .then()
                .extract()
                .jsonPath().get("token");
    }
}
