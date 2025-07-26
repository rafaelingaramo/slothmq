package org.slothmq.client.server.web.controller;

import org.junit.jupiter.api.Test;
import org.slothmq.BaseIntegrationTest;
import org.slothmq.server.user.User;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class LoginHandlerIT extends BaseIntegrationTest {
    @Test
    public void givenWrongCredentialsMakeSureItFails() {
        User user = new User();
        user.setUserName("admin");
        user.setPasskey("unknown");

        String jsonString = given()
                .body(user)
                .when()
                .post("http://localhost:8080/api/login")
                .then()
                .statusCode(401)
                .extract()
                .asString();

        assert Objects.equals(jsonString, "{\"httpStatus\":401,\"error\":\"UserName or Password not found\"}");
    }

    @Test
    public void givenRightCredentialsMakeSureItWorks() {
        User user = new User();
        user.setUserName("admin");
        user.setPasskey("admin");

        String jsonString = given()
                .body(user)
                .when()
                .post("http://localhost:8080/api/login")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assert jsonString != null;
    }
}
