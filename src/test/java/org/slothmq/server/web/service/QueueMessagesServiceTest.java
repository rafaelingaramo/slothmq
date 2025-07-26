package org.slothmq.server.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slothmq.dto.Tuple;
import org.slothmq.exception.UnableToPurgeException;
import org.slothmq.queue.QueueHandler;
import org.slothmq.server.web.SlothHttpHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class QueueMessagesServiceTest {
    @Test
    public void givenTheQueueNamesAreValidCheckOutputCorrectly() throws JsonProcessingException {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var queueHandler = mock(QueueHandler.class);
        var queueMessagesService = new QueueMessagesService(mongoDatabase, queueHandler);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);
        var collectionNames = new String[]{"collectionOne", "collectionTwo"};
        MongoCollection<Document> collectionOne = mock(MongoCollection.class);
        MongoCollection<Document> collectionTwo = mock(MongoCollection.class);
        AggregateIterable<Document> aggregateOne = mock(AggregateIterable.class);
        AggregateIterable<Document> aggregateTwo = mock(AggregateIterable.class);
        MongoIterable<Integer> iterableOne = mock(MongoIterable.class);
        MongoIterable<Integer> iterableTwo = mock(MongoIterable.class);
        var expectedSortedOutput = "[{\"key\":\"collectionTwo\",\"value\":200},{\"key\":\"collectionOne\",\"value\":100}]";
        //stubs
        doAnswer(invocation -> {
            Consumer<Integer> consumer = invocation.getArgument(0);
            consumer.accept(100);
            return null;
        }).when(iterableOne).forEach(any());

        doAnswer(invocation -> {
            Consumer<Integer> consumer = invocation.getArgument(0);
            consumer.accept(200);
            return null;
        }).when(iterableTwo).forEach(any());

        doAnswer(invocation -> iterableOne).when(aggregateOne).map(any());
        doAnswer(invocation -> iterableTwo).when(aggregateTwo).map(any());
        when(collectionOne.aggregate(any()))
                .thenReturn(aggregateOne);
        when(collectionTwo.aggregate(any()))
                .thenReturn(aggregateTwo);

        when(mongoDatabase.getCollection("collectionOne"))
                .thenReturn(collectionOne);
        when(mongoDatabase.getCollection("collectionTwo"))
                .thenReturn(collectionTwo);

        when(listCollectionNames.batchSize(anyInt()))
                .thenReturn(listCollectionNames);

        doAnswer((invocation) -> {
            Consumer<String> stringConsumer = invocation.getArgument(0);
            Arrays.stream(collectionNames).forEach(stringConsumer);
            return null;
        }).when(listCollectionNames).forEach(any());

        when(mongoDatabase.listCollectionNames()).thenReturn(listCollectionNames);

        //when
        List<Tuple<String, Integer>> queueNames = queueMessagesService.getQueueNames();

        //then
        assert queueNames != null;
        assert Objects.equals(SlothHttpHandler.MAPPER.writeValueAsString(queueNames), expectedSortedOutput);
    }

    @Test
    public void givenNoCollectionsInDatabaseNoErrorShouldBeThrown() throws JsonProcessingException {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var queueHandler = mock(QueueHandler.class);
        var queueMessagesService = new QueueMessagesService(mongoDatabase, queueHandler);
        var listCollectionNames = mock(ListCollectionNamesIterable.class);
        var expectedSortedOutput = "[]";

        //stubs
        when(listCollectionNames.batchSize(anyInt()))
                .thenReturn(listCollectionNames);
        doAnswer((invocation) -> null).when(listCollectionNames).forEach(any());
        when(mongoDatabase.listCollectionNames()).thenReturn(listCollectionNames);

        //when
        AtomicReference<List<Tuple<String, Integer>>> queueNames = new AtomicReference<>();
        Assertions.assertDoesNotThrow(() -> {
            queueNames.set(queueMessagesService.getQueueNames());
        });

        //then
        assert queueNames.get() != null;
        assert Objects.equals(SlothHttpHandler.MAPPER.writeValueAsString(queueNames), expectedSortedOutput);
    }

    @Test
    public void givenPurgeIsCalledVerifyCalls() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var queueHandler = mock(QueueHandler.class);
        var queueMessagesService = new QueueMessagesService(mongoDatabase, queueHandler);
        var collectionName = "collectionName";
        MongoCollection<Document> collection = mock(MongoCollection.class);
        var deleteResult = new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return true;
            }

            @Override
            public long getDeletedCount() {
                return 1;
            }
        };
        //stubs
        when(mongoDatabase.getCollection(collectionName)).thenReturn(collection);
        when(collection.deleteMany(any())).thenReturn(deleteResult);

        //when
        queueMessagesService.purgeFromCollection(collectionName);

        //then
        verify(collection, times(1)).deleteMany(any());
        verify(queueHandler, times(1)).deleteFromQueue(collectionName);
    }

    @Test
    public void givenPurgeIsCalledAcknowledgeIsNotOkVerifyCallsAndException() {
        //given
        var mongoDatabase = mock(MongoDatabase.class);
        var queueHandler = mock(QueueHandler.class);
        var queueMessagesService = new QueueMessagesService(mongoDatabase, queueHandler);
        var collectionName = "collectionName";
        MongoCollection<Document> collection = mock(MongoCollection.class);
        var deleteResult = new DeleteResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public long getDeletedCount() {
                return 0;
            }
        };
        //stubs
        when(mongoDatabase.getCollection(collectionName)).thenReturn(collection);
        when(collection.deleteMany(any())).thenReturn(deleteResult);

        //when
        Assertions.assertThrows(UnableToPurgeException.class, () -> queueMessagesService.purgeFromCollection(collectionName));

        //then
        verify(collection, times(1)).deleteMany(any());
        verify(queueHandler, times(0)).deleteFromQueue(collectionName);
    }
}
