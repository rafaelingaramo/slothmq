package org.slothmq.client.server.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slothmq.BaseIntegrationTest;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.dto.Paged;
import org.slothmq.server.web.service.UserService;

import java.util.Arrays;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class UserHandlerIT extends BaseIntegrationTest {

    @Test
    public void givenCallToUsersAPIMakeSureResponseIsOKAndStatusIs200() throws JsonProcessingException {
        //given
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/users")
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        String jsonString = response.asString();
        Paged<User> userPaged = SlothHttpHandler.MAPPER.readValue(jsonString, new TypeReference<>() {});

        //then
        assert userPaged.totalPages() == 1;
        assert !userPaged.isEmpty();
        assert userPaged.totalElements() == 1;
        assert Objects.equals(userPaged.content().getFirst().getUserName(), UserService.BASE_USERNAME);
        assert Objects.equals(userPaged.content().getFirst().getName(), UserService.BASE_NAME);
        assert Arrays.equals(userPaged.content().getFirst().getAccessGroups(), new String[] {UserService.BASE_USER_ACCESS_GROUP});
        assert userPaged.content().getFirst().getActive();
    }

    private String getToken() {
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
