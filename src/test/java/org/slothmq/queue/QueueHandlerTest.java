package org.slothmq.queue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slothmq.server.SlothSocketServer;

import java.util.HashMap;
import java.util.Map;

public class QueueHandlerTest {
    private final Map<String, Object> inMemoryDatabase = new HashMap<>();

    @Test
    @SuppressWarnings("unchecked")
    public void givenIHaveAMessagePushToQueue() {
        //given
        try (MockedStatic<?> mocked = Mockito.mockStatic(SlothSocketServer.class)) {
            String queueName = "command.order.purchase";
            String helloWorld = "Hello World";

            MongoDatabase mongoDatabaseMock = Mockito.mock(MongoDatabase.class);
            MongoCollection<Document> mongoCollectionMock = Mockito.mock(MongoCollection.class);
            Mockito.when(mongoCollectionMock.insertOne(Mockito.any()))
                    .then(o -> inMemoryDatabase.put(queueName, o.getArguments()[0]));
            Mockito.when(mongoDatabaseMock.getCollection(queueName)).thenReturn(mongoCollectionMock);
            mocked.when(SlothSocketServer::getMongoDatabase).thenReturn(mongoDatabaseMock);
            Mockito.doNothing().when(mongoDatabaseMock).createCollection(Mockito.any());

            QueueHandler queueHandler = QueueHandler.getInstance();

            //when
            queueHandler.pushToQueue(queueName, helloWorld);
            Object consumedObject = queueHandler.consumeFromQueue(queueName);
            //then

            Document d = (Document)inMemoryDatabase.get(queueName);
            assert d.get("id") != null;
            assert d.get("content") == helloWorld;
            assert consumedObject == helloWorld;
        }

    }
}
