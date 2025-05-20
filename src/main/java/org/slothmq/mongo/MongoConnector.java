package org.slothmq.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnector {
    private static final String URI = "mongodb://admin:secret@localhost:27017/?authSource=admin";
    private static final MongoDatabase MONGO_DATABASE;

    static {
        //TODO I need to create a hook to close this when the server is shutting down
        MongoClient client = MongoClients.create(URI);
        MONGO_DATABASE = client.getDatabase("slothmq");
    }

    public static MongoDatabase getDatabaseInstance() {
        return MongoConnector.MONGO_DATABASE;
    }
}
