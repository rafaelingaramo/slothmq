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

public class MasterQueue {
    private static final Logger LOG = LoggerFactory.getLogger(MasterQueue.class);
    private static final Map<String, Queue<Object>> MAP_QUEUE_STRUCT = new HashMap<>();
    private static final MasterQueue INSTANCE = new MasterQueue();
    private static final MongoDatabase database = SlothSocketServer.mongoDatabase;
    private static final String PRODUCER_KEY = "MasterQueue.Producer";
    private static final String CONSUMER_KEY = "MasterQueue.Consumer";

    public static void startQueueFromDatabase() {
        LOG.info("Initializing system from database");
        assert MAP_QUEUE_STRUCT.isEmpty();

        ListCollectionNamesIterable collectionNames = database.listCollectionNames();
        collectionNames.batchSize(10)
                .map(database::getCollection)
                .forEach(getInstance()::insertIntoQueue);
        LOG.info("Initialization complete");
    }

    public static MasterQueue getInstance() {
        return INSTANCE;
    }

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
}
