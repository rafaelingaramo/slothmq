package org.slothmq.web.service;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;
import org.slothmq.dto.Tuple;
import org.slothmq.mongo.MongoConnector;

import java.util.ArrayList;
import java.util.List;

public class QueueMessagesService {
    public MongoDatabase mongoDatabase;

    public QueueMessagesService() {
        this.mongoDatabase = MongoConnector.getDatabaseInstance();
    }

    public List<Tuple<String, String>> getQueueNames() {
        ListCollectionNamesIterable collections = mongoDatabase.listCollectionNames();

        List<Tuple<String, String>> queueList = new ArrayList<>();
        collections
                .batchSize(0)
                .forEach(collectionName -> {
                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
                    mongoCollection.aggregate(List.of(Aggregates.count("totalDocuments")))
                            .map(Document::toJson)
                            .forEach(s -> queueList.add(new Tuple<>(collectionName, s)));
                });

        return queueList;
    }
}
