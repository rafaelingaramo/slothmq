package org.slothmq.server.web.service;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.slothmq.dto.Tuple;
import org.slothmq.exception.UnableToPurgeException;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.queue.QueueHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QueueMessagesService {
    private final MongoDatabase mongoDatabase;
    private final QueueHandler queueHandler;

    public QueueMessagesService(MongoDatabase mongoDatabase, QueueHandler queueHandler) {
        this.mongoDatabase = mongoDatabase;
        this.queueHandler = queueHandler;
    }

    public List<Tuple<String, Integer>> getQueueNames() {
        ListCollectionNamesIterable collections = mongoDatabase.listCollectionNames();

        List<Tuple<String, Integer>> queueList = new ArrayList<>();
        collections
                .batchSize(0)
                .forEach(collectionName -> {
                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
                    mongoCollection.aggregate(List.of(Aggregates.count("totalDocuments")))
                            .map(d -> (Integer)d.get("totalDocuments"))
                            .forEach(s -> queueList.add(new Tuple<>(collectionName, s)));
                });

        return queueList.stream()
                .sorted(Comparator
                        .comparing((Tuple<String, Integer> t) -> t.getValue())
                        .reversed()
                        .thenComparing(Tuple::getKey))
                .toList();
    }

    public void purgeFromCollection(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        DeleteResult deleteResult = collection.deleteMany(BsonDocument.parse("{}"));
        boolean acknowledged = deleteResult.wasAcknowledged();
        if (!acknowledged) {
            throw new UnableToPurgeException(collectionName);
        }

        queueHandler.deleteFromQueue(collectionName);
    }
}
