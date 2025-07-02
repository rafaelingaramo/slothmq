package org.slothmq.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnector {
    private static final String URI = "mongodb://admin:secret@localhost:27017/?authSource=admin";
    private final MongoDatabase database;

    public MongoConnector() {
        MongoClient client = MongoClients.create(URI);
        this.database = client.getDatabase("slothmq");
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }
}
