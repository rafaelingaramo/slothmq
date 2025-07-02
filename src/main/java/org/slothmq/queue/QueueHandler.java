package org.slothmq.queue;

import org.slothmq.exception.NonexistentQueueException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueHandler {
    private static final Logger LOG = LoggerFactory.getLogger(QueueHandler.class);
    private static final String PRODUCER_KEY = "MasterQueue.Producer";
    private static final String CONSUMER_KEY = "MasterQueue.Consumer";
    private final MongoDatabase database;
    private final Map<String, Queue<Object>> mapQueueStruct = new HashMap<>();

    public QueueHandler(MongoDatabase database) {
        this.database = database;
        startQueueFromDatabase();
    }

    //TODO cover this method on unit test
    private void startQueueFromDatabase() {
        LOG.info("Initializing system from database");
        assert mapQueueStruct.isEmpty();

        database.listCollectionNames()
                .forEach(this::insertIntoQueue);
        LOG.info("Initialization complete");
    }


    //TODO don't use mongo directly, use by the service layer
    public void pushToQueue(String queueName, Object content) {
        Queue<Object> queue;
        if (!mapQueueStruct.containsKey(queueName)) {
            queue = new LinkedBlockingQueue<>();
            database.createCollection(queueName);
        } else {
            queue = mapQueueStruct.get(queueName);
        }

        synchronized (PRODUCER_KEY) {
            MongoCollection<Document> queueCollection = database.getCollection(queueName);
            queue.add(content);
            queueCollection.insertOne(new Document("id", UUID.randomUUID().toString())
                    .append("content", content));
            mapQueueStruct.put(queueName, queue);
        }
    }

    //TODO not deleting from database? need to add an index here to be able to delete from database
    public Object consumeFromQueue(String queueName) {
        if (!mapQueueStruct.containsKey(queueName)) {
            throw new NonexistentQueueException(queueName);
        }

        synchronized (CONSUMER_KEY) {
            return mapQueueStruct.get(queueName).poll();
        }
    }

    //TODO cover this method on unittest
    //TODO this needs to delete from the database too
    public void deleteFromQueue(String queueName) {
        if (!mapQueueStruct.containsKey(queueName)) {
            throw new NonexistentQueueException(queueName);
        }

        synchronized (QueueHandler.class) {
            mapQueueStruct.get(queueName).clear();
        }
    }

    private void insertIntoQueue(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        mapQueueStruct.put(collectionName, new LinkedBlockingQueue<>());

        collection.find()
                .forEach(item -> mapQueueStruct.get(collectionName).add(item));
    }

}
