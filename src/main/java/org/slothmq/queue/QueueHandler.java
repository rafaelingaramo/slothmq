package org.slothmq.queue;

import org.slothmq.exception.NonexistentQueueException;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.server.SlothSocketServer;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueHandler {
    private static final Logger LOG = LoggerFactory.getLogger(QueueHandler.class);
    private static final Map<String, Queue<Object>> MAP_QUEUE_STRUCT = new HashMap<>();
    private static final QueueHandler INSTANCE = new QueueHandler();
    private static final MongoDatabase database = SlothSocketServer.getMongoDatabase();
    private static final String PRODUCER_KEY = "MasterQueue.Producer";
    private static final String CONSUMER_KEY = "MasterQueue.Consumer";

    private QueueHandler() {}

    public static void startQueueFromDatabase() {
        LOG.info("Initializing system from database");
        assert MAP_QUEUE_STRUCT.isEmpty();

        ListCollectionNamesIterable collectionNames = database.listCollectionNames();
        collectionNames.batchSize(10)
                .map(database::getCollection)
                .forEach(getInstance()::insertIntoQueue);
        LOG.info("Initialization complete");
    }

    //TODO make it singleton by making the constructor private
    public static QueueHandler getInstance() {
        return INSTANCE;
    }

    //TODO don't use mongo directly, use by the service layer
    public void pushToQueue(String queueName, Object content) {
        Queue<Object> queue;
        if (!MAP_QUEUE_STRUCT.containsKey(queueName)) {
            queue = new LinkedBlockingQueue<>();
            database.createCollection(queueName);
        } else {
            queue = MAP_QUEUE_STRUCT.get(queueName);
        }

        synchronized (PRODUCER_KEY) {
            MongoCollection<Document> queueCollection = database.getCollection(queueName);
            queue.add(content);
            queueCollection.insertOne(new Document("id", UUID.randomUUID().toString())
                    .append("content", content));
            MAP_QUEUE_STRUCT.put(queueName, queue);
        }
    }

    //TODO not deleting from database? need to add an index here to be able to delete from database
    public Object consumeFromQueue(String queueName) {
        if (!MAP_QUEUE_STRUCT.containsKey(queueName)) {
            throw new NonexistentQueueException(queueName);
        }

        synchronized (CONSUMER_KEY) {
            return MAP_QUEUE_STRUCT.get(queueName).poll();
        }
    }

    private void insertIntoQueue(MongoCollection<Document> collection) {
        String queue = collection.getNamespace().getCollectionName();
        MAP_QUEUE_STRUCT.put(queue, new LinkedBlockingQueue<>());

        collection.find().batchSize(100)
                .forEach(item -> MAP_QUEUE_STRUCT.get(queue).add(item));
    }

    public void deleteFromQueue(String queueName) {
        if (!MAP_QUEUE_STRUCT.containsKey(queueName)) {
            throw new NonexistentQueueException(queueName);
        }

        synchronized (QueueHandler.class) {
            MAP_QUEUE_STRUCT.get(queueName).clear();
        }
    }
}
