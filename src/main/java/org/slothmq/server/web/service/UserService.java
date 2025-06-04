package org.slothmq.server.web.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.mongo.MongoConnector;

import java.util.*;

public class UserService {
    private final MongoDatabase mongoDatabase = MongoConnector.getDatabaseInstance();
    private static final String USER_COLLECTION = "private.user.collection";
    //TODO inject through environment variable
    private static final String BASE_USER_PASSKEY = "admin:admin";
    private static final String BASE_USER_ACCESS_GROUP = "admin";
    private static final String BASE_USER_NAME = "Admin";
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    public void initUserCollectionIfNeeded() {
        boolean collectionExists = Optional.ofNullable(mongoDatabase.listCollections()
                .filter(BsonDocument.parse(String.format("{'name': '%s'}", USER_COLLECTION)))
                .first()).isPresent();
        if (collectionExists) {
            LOG.info("User collection creation skipped");
            return;
        }

        mongoDatabase.createCollection(USER_COLLECTION);
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);
        collection.insertOne(new Document("id", UUID.randomUUID().toString())
                .append("name", BASE_USER_NAME)
                .append("passkey", new String(Base64.getEncoder().encode(BASE_USER_PASSKEY.getBytes())))
                .append("accessGroup", BASE_USER_ACCESS_GROUP));
        //TODO swallowing error here
        LOG.info("User collection initialized");
    }



    public List<?> listPaged(Object pageRequest) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);
        return null;
    }
}
