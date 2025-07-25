package org.slothmq.client.server.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slothmq.BaseIntegrationTest;
import org.slothmq.BaseIntegrationTestSingletonHolder;
import org.slothmq.server.jwt.JwtUtil;
import org.slothmq.server.user.User;
import org.slothmq.server.user.UserMapper;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.dto.Paged;
import org.slothmq.server.web.service.UserService;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class UserHandlerIT extends BaseIntegrationTest {
    @Test
    public void givenCallToUsersAPIMakeSureResponseIsOKAndStatusIs200() throws JsonProcessingException {
        //given
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .queryParam("search", "Admin")
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

    @ParameterizedTest
    @CsvSource({"1,50,name,ASC,,3,50,101",
                "2,50,name,ASC,,3,50,101",
                "3,50,name,ASC,,3,1,101",
                "1,101,name,ASC,,1,101,101",
                "1,10,name,ASC,lorem,10,10,100",
                "1,101,name,ASC,Admin,1,1,1",
                "1,10,name,DESC,lorem,10,10,100"})
    public void givenIHave100ElementsCallWithVariousVariationsAndCheckResults(Integer page, Integer size,
                                 String sortField, String sortOrder,
                                 String searchField, Integer expectedTotalPages,
                                 Integer expectedResultSize,
                                 Integer expectedTotalElements) throws JsonProcessingException {

        //given I insert 100 records onto the database
        insertDataOntoDatabase(100);

        //given call to the service
        RequestSpecification requestSpecification = given()
                .header("Authorization", "Bearer " + getToken())
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortField", sortField)
                .queryParam("sortOrder", sortOrder);

        if (searchField != null) {
            requestSpecification.queryParam("search", searchField);
        }

        Response response = requestSpecification.when()
                .get("http://localhost:8080/api/users")
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        String jsonString = response.asString();
        Paged<User> userPaged = SlothHttpHandler.MAPPER.readValue(jsonString, new TypeReference<>() {});

        //then
        assert userPaged.totalPages() == expectedTotalPages;
        assert !userPaged.isEmpty();
        assert userPaged.totalElements() == expectedTotalElements;
        assert !userPaged.content().isEmpty();
        assert userPaged.content().size() == expectedResultSize;

        eraseData();
    }


    @Test
    public void givenRecordOnDatabaseFindOne() throws JsonProcessingException {
        //given I insert 1 record onto the database
        User user = insertOneOntoDatabaseAndReturn();

        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/users/" + user.getId())
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        String jsonString = response.asString();
        User apiUser = SlothHttpHandler.MAPPER.readValue(jsonString, User.class);

        //then
        assert Objects.equals(apiUser.getUserName(), user.getUserName());
        assert Objects.equals(apiUser.getName(), user.getName());
        assert Arrays.equals(apiUser.getAccessGroups(), user.getAccessGroups());
        assert apiUser.getActive() == user.getActive();

        eraseData();
    }

    @Test
    public void givenRecordNotOnDatabaseFindOne() {
        //given
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(404)
                .extract()
                .response();

        String jsonString = response.asString();
        assert jsonString.equals("{\"httpStatus\":404,\"error\":\"User not found\"}");
    }

    @Test
    public void givenRecordExistsDeleteIt() {
        //given I insert 1 record onto the database
        User user = insertOneOntoDatabaseAndReturn();

        //when
        given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .delete("http://localhost:8080/api/users/" + user.getId())
                .then() //when
                .statusCode(204);

        //then
        given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/users/" + user.getId())
                .then() //when
                .statusCode(404);
    }

    @Test
    public void givenRecordDoesNotExistsThrowErrorOnDelete() {
        //given
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .delete("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(404)
                .extract()
                .response();

        String jsonString = response.asString();
        assert jsonString.equals("{\"httpStatus\":404,\"error\":\"User not found\"}");
    }


    @Test
    public void givenRecordExistsEditIt() throws JsonProcessingException {
        //given I insert 1 record onto the database
        User user = insertOneOntoDatabaseAndReturn();
        user.setUserName("newUserName");
        user.setName("newName");
        user.setAccessGroups(new String[]{"viewer"});

        //when
        given()
                .header("Authorization", "Bearer " + getToken())
                .body(user)
                .when()
                .put("http://localhost:8080/api/users/" + user.getId())
                .then() //when
                .statusCode(204);

        //and
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/users/" + user.getId())
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        String jsonString = response.asString();
        User apiUser = SlothHttpHandler.MAPPER.readValue(jsonString, User.class);
        assert Objects.equals(apiUser.getUserName(), user.getUserName());
        assert Objects.equals(apiUser.getName(), user.getName());
        assert Arrays.equals(apiUser.getAccessGroups(), user.getAccessGroups());
        assert apiUser.getActive() == user.getActive();

        eraseData();
    }

    @Test
    public void givenRecordDoesNotExistsThrowErrorOnEdit() {
        //given
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .body(new User())
                .when()
                .put("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(404)
                .extract()
                .response();

        String jsonString = response.asString();
        assert jsonString.equals("{\"httpStatus\":404,\"error\":\"User not found\"}");
    }

    @Test
    public void givenRecordExistsChangePassword() throws JsonProcessingException {
        //given I insert 1 record onto the database
        User user = insertOneOntoDatabaseAndReturn();
        user.setPasskey("spiderMan");

        //when
        given()
                .header("Authorization", "Bearer " + getToken())
                .body(user)
                .when()
                .put("http://localhost:8080/api/users/" + user.getId() + "/password")
                .then() //when
                .statusCode(204);

        //and
        String token = given()
                .body(user)
                .when()
                .post("http://localhost:8080/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().get("token");

        assert token != null;
        JwtUtil.verifyToken(token);

        eraseData();
    }

    @Test
    public void givenUserDoesNotExistsTryToChangePassword() {
        //when
        String jsonString = given()
                .header("Authorization", "Bearer " + getToken())
                .body(new User())
                .when()
                .put("http://localhost:8080/api/users/" + UUID.randomUUID() + "/password")
                .then() //when
                .statusCode(404)
                .extract()
                .asString();
        assert jsonString.equals("{\"httpStatus\":404,\"error\":\"User not found\"}");
    }

    @Test
    public void givenValidDataInsertOne() {
        //when
        given()
                .header("Authorization", "Bearer " + getToken())
                .body(new User(null, "lorem", "ipsum", new String[]{"viewer"}, true))
                .when()
                .post("http://localhost:8080/api/users")
                .then() //when
                .statusCode(201);

        MongoDatabase database = BaseIntegrationTestSingletonHolder.getDatabase();

        User from = UserMapper.from(database.getCollection(UserService.USER_COLLECTION)
                .find(Filters.not(new Document("name", "Admin")))
                .first());

        assert from.getActive();
        assert from.getName().equals("lorem");
        assert from.getUserName().equals("ipsum");
        assert Arrays.equals(from.getAccessGroups(), new String[]{"viewer"});

        eraseData();
    }

    @ParameterizedTest
    @CsvSource({"lorem,,viewer",
                ",lorem,viewer",
                "lorem,lorem,"})
    public void givenInvalidDataInsertOne(String name, String userName, String accessGroup) {
        //when
        String jsonString = given()
                .header("Authorization", "Bearer " + getToken())
                .body(new User(null, name, userName, accessGroup != null ? new String[]{accessGroup} : null, true))
                .when()
                .post("http://localhost:8080/api/users")
                .then() //when
                .statusCode(400)
                .extract()
                .asString();

        assert jsonString.equals("{\"httpStatus\":400,\"error\":\"One or more fields are missing\"}");
    }

    @Test
    public void checkAuthorizationFeature() {
        //given I insert 1 record onto the database
        User user = insertOneViewerOntoDatabase();

        //I grab the viewer token
        String token = given()
                .body(user)
                .when()
                .post("http://localhost:8080/api/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().get("token");

        //when I try to insert a record, a 403 error should be thrown
        var response = given()
                .header("Authorization", "Bearer " + token)
                .body(new User(null, "lorem", "ipsum", new String[]{"viewer"}, true))
                .when()
                .post("http://localhost:8080/api/users")
                .then() //when
                .statusCode(403)
                .extract()
                .response();
        String jsonResponse = response.asString();
        assert jsonResponse.contains("{\"httpStatus\":403,\"error\":\"Unauthorized access attempt on token");

        //when I try to edit a record, a 403 error should be thrown
        response = given()
                .header("Authorization", "Bearer " + token)
                .body(new User(null, "lorem", "ipsum", new String[]{"viewer"}, true))
                .when()
                .put("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(403)
                .extract()
                .response();
        jsonResponse = response.asString();
        assert jsonResponse.contains("{\"httpStatus\":403,\"error\":\"Unauthorized access attempt on token");

        //when I try to delete a record, a 403 error should be thrown
        response = given()
                .header("Authorization", "Bearer " + token)
                .body(new User(null, "lorem", "ipsum", new String[]{"viewer"}, true))
                .when()
                .delete("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(403)
                .extract()
                .response();
        jsonResponse = response.asString();
        assert jsonResponse.contains("{\"httpStatus\":403,\"error\":\"Unauthorized access attempt on token");

        //when I try to change a password, a 403 error should be thrown
        response = given()
                .header("Authorization", "Bearer " + token)
                .body(new User(null, "lorem", "ipsum", new String[]{"viewer"}, true))
                .when()
                .put("http://localhost:8080/api/users/" + UUID.randomUUID() + "/password")
                .then() //when
                .statusCode(403)
                .extract()
                .response();
        jsonResponse = response.asString();
        assert jsonResponse.contains("{\"httpStatus\":403,\"error\":\"Unauthorized access attempt on token");

        //when I try to find one, it should allow
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("http://localhost:8080/api/users/" + UUID.randomUUID())
                .then() //when
                .statusCode(404); //because the user does not exists

        //when I try to find all, it should allow
        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("search", "Admin")
                .when()
                .get("http://localhost:8080/api/users")
                .then() //when
                .statusCode(200);
    }

    private User insertOneViewerOntoDatabase() {
        MongoDatabase database = BaseIntegrationTestSingletonHolder.getDatabase();

        UserService userService = new UserService(database);
        userService.insertOne(new User(null,
                "lorem",
                "ipsum",new String[]{"viewer"},
                true));
        User user = UserMapper.from(database.getCollection(UserService.USER_COLLECTION)
                .find(Filters.not(new Document("name", "Admin")))
                .first());
        user.setPasskey("ipsum");
        userService.changePassword(user.getId().toString(), user);

        UserMapper.from(database.getCollection(UserService.USER_COLLECTION)
                .find(Filters.not(new Document("name", "Admin")))
                .first());
        return user;
    }

    private User insertOneOntoDatabaseAndReturn() {
        insertDataOntoDatabase(1);
        MongoDatabase database = BaseIntegrationTestSingletonHolder.getDatabase();
        return UserMapper.from(database.getCollection(UserService.USER_COLLECTION)
                .find(Filters.not(new Document("name", "Admin")))
                .first());
    }


    private void eraseData() {
        MongoDatabase database = BaseIntegrationTestSingletonHolder.getDatabase();
        MongoCollection<Document> collection = database.getCollection(UserService.USER_COLLECTION);
        collection.deleteMany(Filters.not(new Document("name", "Admin")));
    }

    private static void insertDataOntoDatabase(Integer amount) {
        MongoDatabase database = BaseIntegrationTestSingletonHolder.getDatabase();

        UserService userService = new UserService(database);

        for (int i=0;i<amount;i++) {
            userService.insertOne(new User(null,
                    "lorem+"+i,
                    "ipsum+"+i,
                    i%2==0? new String[]{"admin"} : new String[]{"viewer"},
                    i % 2 == 0));
        }
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
