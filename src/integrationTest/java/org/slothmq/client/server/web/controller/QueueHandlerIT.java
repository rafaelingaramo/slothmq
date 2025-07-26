package org.slothmq.client.server.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slothmq.BaseIntegrationTest;
import org.slothmq.client.Sender;
import org.slothmq.dto.Tuple;
import org.slothmq.server.web.SlothHttpHandler;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class QueueHandlerIT extends BaseIntegrationTest {
    @Test
    public void givenIInsertDataOntoTheQueueMakeSureItReturnsThroughTheApiAndItsPurgedAfter() throws JsonProcessingException, InterruptedException {
        //given
        Sender sender = new Sender();
        sender.postToQueue("lorem ipsum", "queue.command.payment.process");
        sender.postToQueue("lorem ipsum", "queue.command.payment.read");

        Thread.sleep(1000);

        //when I call the endpoint
        Response response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/messages")
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        String jsonString = response.asString();
        List<Tuple<String, Integer>> tupleList = SlothHttpHandler.MAPPER.readValue(jsonString, new TypeReference<>() {});

        //then I assert the values
        assert tupleList.size() == 3;
        assert Objects.equals(tupleList.get(0).getKey(), "private.user.collection");
        assert tupleList.get(0).getValue() == 1;
        assert Objects.equals(tupleList.get(1).getKey(), "queue.command.payment.process");
        assert tupleList.get(1).getValue() == 1;
        assert Objects.equals(tupleList.get(2).getKey(), "queue.command.payment.read");
        assert tupleList.get(2).getValue() == 1;

        //and I purge the queues
        purgeFromQueue("queue.command.payment.process");
        purgeFromQueue("queue.command.payment.read");

        //and I check the endpoint again
        response = given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .get("http://localhost:8080/api/messages")
                .then() //when
                .statusCode(200)
                .extract()
                .response();

        jsonString = response.asString();
        tupleList = SlothHttpHandler.MAPPER.readValue(jsonString, new TypeReference<>() {});

        //and I assert the response is size == 1
        assert tupleList.size() == 1;
    }

    private void purgeFromQueue(String queueName) {
        given()
                .header("Authorization", "Bearer " + getToken())
                .when()
                .delete("http://localhost:8080/api/messages/" + queueName)
                .then() //when
                .statusCode(204);
    }
}
