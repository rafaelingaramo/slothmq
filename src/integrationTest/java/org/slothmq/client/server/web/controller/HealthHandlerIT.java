package org.slothmq.client.server.web.controller;

import org.junit.jupiter.api.Test;
import org.slothmq.BaseIntegrationTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class HealthHandlerIT extends BaseIntegrationTest {

    @Test
    public void healthCheck() {
        given()
                .when()
                .get("http://localhost:8080/api/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("ok"));
    }
}
