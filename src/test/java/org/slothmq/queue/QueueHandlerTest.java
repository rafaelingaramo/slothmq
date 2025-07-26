package org.slothmq.queue;

import com.mongodb.client.*;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slothmq.exception.NonexistentQueueException;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QueueHandlerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void givenIHaveAMessagePushToQueueAndItNeedsToBeConsumed() {
        //given
        Map<String, List<Document>> inMemoryDatabase = new HashMap<>();
        var queueName = "command.order.purchase";

        var mongoDatabaseMock = mock(MongoDatabase.class);
        var mongoCollectionMock = mock(MongoCollection.class);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);

        //stubs
        when(mongoDatabaseMock.listCollectionNames()).thenReturn(listCollectionNames); //no prior collection is loaded from database
        when(mongoCollectionMock.insertOne(any()))
                .then(o -> {
                    Document argument = (Document) o.getArguments()[0];
                    if (inMemoryDatabase.containsKey(queueName)) {
                        inMemoryDatabase.get(queueName).add(argument);
                    } else {
                        inMemoryDatabase.put(queueName, new ArrayList<>(List.of(argument)));
                    }
                    return null;
                });
        when(mongoDatabaseMock.getCollection(queueName)).thenReturn(mongoCollectionMock);
        doNothing().when(mongoDatabaseMock).createCollection(any());

        //when
        QueueHandler queueHandler = new QueueHandler(mongoDatabaseMock);
        queueHandler.pushToQueue(queueName, "1");

        //and I want to push a second time to make sure the queue is not recreated
        queueHandler.pushToQueue(queueName, "2");

        //then
        var consumedObject = queueHandler.consumeFromQueue(queueName);
        assert consumedObject == "1";

        //and i verify again
        consumedObject = queueHandler.consumeFromQueue(queueName);
        assert consumedObject == "2";

        //and i verify the in memory database
        var memoryQueue = inMemoryDatabase.get(queueName);
        assert memoryQueue.getFirst().get("id") != null;
        assert memoryQueue.getFirst().get("content") == "1";
        assert memoryQueue.get(1).get("id") != null;
        assert memoryQueue.get(1).get("content") == "2";
    }

    @Test
    void testIWantToConsumeFromAQueueThatDoesNotExists(){
        //given
        var mongoDatabaseMock = mock(MongoDatabase.class);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);

        //stubs
        when(mongoDatabaseMock.listCollectionNames()).thenReturn(listCollectionNames); //no prior collection is loaded from database
        QueueHandler queueHandler = new QueueHandler(mongoDatabaseMock);

        //when
        assertThrows(NonexistentQueueException.class, () -> {
            queueHandler.consumeFromQueue("unExistentQueueName");
        });
    }

    @Test
    void testStartQueueFromDatabase() {
        //given
        var mongoDatabaseMock = mock(MongoDatabase.class);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);
        MongoCollection<Document> collectionOne = mock(MongoCollection.class);
        MongoCollection<Document> collectionTwo = mock(MongoCollection.class);
        FindIterable<Document> collectionOneIterable = mock(FindIterable.class);
        FindIterable<Document> collectionTwoIterable = mock(FindIterable.class);
        var fakeDocumentsOnCollectionOne = new String[]{"1", "2", "3", "4"};
        var fakeDocumentsOnCollectionTwo = new String[]{"5", "6", "7", "8"};

        //stubs
        doAnswer(invocation -> {
            Consumer<Document> consumer = invocation.getArgument(0);
            Arrays.stream(fakeDocumentsOnCollectionOne).forEach(e -> consumer.accept(new Document("id", e)));
            return null;
        }).when(collectionOneIterable).forEach(any());

        doAnswer(invocation -> {
            Consumer<Document> consumer = invocation.getArgument(0);
            Arrays.stream(fakeDocumentsOnCollectionTwo).forEach(e -> consumer.accept(new Document("id", e)));
            return null;
        }).when(collectionTwoIterable).forEach(any());

        when(collectionOne.find()).thenReturn(collectionOneIterable);
        when(collectionTwo.find()).thenReturn(collectionTwoIterable);
        when(mongoDatabaseMock.getCollection("collectionOne")).thenReturn(collectionOne);
        when(mongoDatabaseMock.getCollection("collectionTwo")).thenReturn(collectionTwo);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            Arrays.asList("collectionOne", "collectionTwo").forEach(consumer);
            return null;
        }).when(listCollectionNames).forEach(any());
        when(mongoDatabaseMock.listCollectionNames()).thenReturn(listCollectionNames);
        //when
        QueueHandler queueHandler = new QueueHandler(mongoDatabaseMock);

        //then
        List<String> verificationStringList = new ArrayList<>();

        for (int i=1;i<=8;i++) {
            Document doc = (Document) queueHandler.consumeFromQueue(i<=4?"collectionOne":"collectionTwo");
            verificationStringList.add((String) doc.get("id"));
        }

        assert verificationStringList.stream()
            .sorted()
            .collect(Collectors.joining(",")).equals("1,2,3,4,5,6,7,8");
    }

    @Test
    void givenITryToDeleteFromUnExistentQueueMakeSureExceptionIsThrown() {
        //given
        var mongoDatabaseMock = mock(MongoDatabase.class);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);

        //stubs
        when(mongoDatabaseMock.listCollectionNames()).thenReturn(listCollectionNames); //no prior collection is loaded from database
        QueueHandler queueHandler = new QueueHandler(mongoDatabaseMock);

        //when
        assertThrows(NonexistentQueueException.class, () ->
                queueHandler.deleteFromQueue("unExistentQueueName"));
    }

    @Test
    void givenIHaveObjectsOnMyQueueINeedToDeleteThose() {
        //given
        var mongoDatabaseMock = mock(MongoDatabase.class);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);
        MongoCollection<Document> collectionOne = mock(MongoCollection.class);
        FindIterable<Document> collectionOneIterable = mock(FindIterable.class);
        var fakeDocumentsOnCollectionOne = new String[]{"1", "2", "3", "4"};

        //stubs
        doAnswer(invocation -> {
            Consumer<Document> consumer = invocation.getArgument(0);
            Arrays.stream(fakeDocumentsOnCollectionOne).forEach(e -> consumer.accept(new Document("id", e)));
            return null;
        }).when(collectionOneIterable).forEach(any());

        when(collectionOne.find()).thenReturn(collectionOneIterable);
        when(mongoDatabaseMock.getCollection("collectionOne")).thenReturn(collectionOne);

        doAnswer(invocation -> {
            Consumer<String> consumer = invocation.getArgument(0);
            List.of("collectionOne").forEach(consumer);
            return null;
        }).when(listCollectionNames).forEach(any());
        when(mongoDatabaseMock.listCollectionNames()).thenReturn(listCollectionNames);
        //when the class is loaded, then the data should be loaded from memory
        QueueHandler queueHandler = new QueueHandler(mongoDatabaseMock);

        //and when I prune that queue I need to make sure the data is erased
        queueHandler.deleteFromQueue("collectionOne");

        //then I need to verify if the data was deleted
        assert queueHandler.consumeFromQueue("collectionOne") == null;
    }

}

