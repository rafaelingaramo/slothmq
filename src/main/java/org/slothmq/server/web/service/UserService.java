package org.slothmq.server.web.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.exception.InvalidUserException;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.server.user.User;
import org.slothmq.server.user.UserMapper;
import org.slothmq.server.web.dto.PageRequest;

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
                .append("accessGroup", BASE_USER_ACCESS_GROUP)
                .append("active", true));
        //TODO swallowing error here

        LOG.info("User collection initialized");
    }

    public List<User> listPaged(PageRequest pageRequest) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        PageRequest.Sort sort = pageRequest.sort();
        int skip = (pageRequest.page() - 1) * pageRequest.pageSize();

        if (sort != null) {
            return collection.find()
                    .sort(sort.order() == PageRequest.Sort.SortOrder.ASC ? Sorts.ascending(sort.field()) :
                            Sorts.descending(sort.field()))
                    .skip(skip)
                    .limit(pageRequest.pageSize())
                    .into(new ArrayList<>())
                    .stream()
                    .map(UserMapper::from)
                    .toList();
        }

        return collection.find()
                .skip(skip)
                .limit(pageRequest.pageSize())
                .into(new ArrayList<>())
                .stream()
                .map(UserMapper::from)
                .toList();
    }

    public User insertOne(User user) {
        if (user.name() == null ||
            user.accessGroups() == null ||
            user.passkey() == null) {
            throw new InvalidUserException("One or more fields are missing");
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);
        BsonValue insertedId = collection.insertOne(new Document("id", UUID.randomUUID().toString())
                .append("name", user.name())
                .append("passKey", new String(Base64.getEncoder().encode(user.passkey().getBytes())))
                .append("accessGroup", user.accessGroups())
                .append("active", true)).getInsertedId();

        assert insertedId != null;

        return Optional.ofNullable(collection.find(new Document("_id", insertedId))
                        .first())
                .map(UserMapper::from)
                .orElseThrow();
    }

    public void removeOne(String identifier) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier)
                .append("active", true);

        Document replacement = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow();
        replacement.append("active", false);

        UpdateResult updateResult = collection.replaceOne(filter, replacement);
        assert updateResult.getModifiedCount() == 1;
    }

    public void editOne(String identifier, User user) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier);

        Document replacement = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow();

        replacement.append("active", user.active())
                .append("accessGroup", user.accessGroups())
                .append("name", user.name());

        UpdateResult updateResult = collection.replaceOne(filter, replacement);
        assert updateResult.getModifiedCount() == 1;
    }
}
//TODO can I have transactions on mongo?
