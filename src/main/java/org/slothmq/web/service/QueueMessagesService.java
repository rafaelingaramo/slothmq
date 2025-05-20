package org.slothmq.web.service;

import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.MongoDatabase;
import org.slothmq.mongo.MongoConnector;

import java.util.ArrayList;
import java.util.List;

public class QueueMessagesService {
    public MongoDatabase mongoDatabase;

    public QueueMessagesService() {
        this.mongoDatabase = MongoConnector.getDatabaseInstance();
    }

    public List<String> getQueueNames() {
        ListCollectionNamesIterable collections = mongoDatabase.listCollectionNames();
        List<String> queueList = new ArrayList<>();
        collections.batchSize(10)
                .forEach(queueList::add);
        return queueList;
    }
}
