package org.slothmq.server.web.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.exception.InvalidUserException;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.server.user.User;
import org.slothmq.server.user.UserMapper;
import org.slothmq.server.web.dto.PageRequest;
import org.slothmq.server.web.dto.Paged;

import java.util.*;

public class UserService {
    private final MongoDatabase mongoDatabase = MongoConnector.getDatabaseInstance();
    private static final String BASE_USERNAME = "admin";
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
                .append("accessGroups", BASE_USER_ACCESS_GROUP)
                .append("userName", BASE_USERNAME)
                .append("active", true));
        //TODO swallowing error here

        LOG.info("User collection initialized");
    }

    public Paged<User> listPaged(PageRequest pageRequest) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        PageRequest.Sort sort = pageRequest.sort();
        int skip = (pageRequest.page() - 1) * pageRequest.pageSize();

        Bson filter = pageRequest.search() != null ? Filters.regex("name", pageRequest.search(), "i")
            : new BsonDocument();

        long allDocuments = collection.countDocuments(filter);
        List<User> userResult;
        if (sort != null) {
            userResult = collection.find(filter)
                    .sort(sort.order() == PageRequest.Sort.SortOrder.ASC ? Sorts.ascending(sort.field()) :
                            Sorts.descending(sort.field()))
                    .skip(skip)
                    .limit(pageRequest.pageSize())
                    .into(new ArrayList<>())
                    .stream()
                    .map(UserMapper::from)
                    .toList();
        } else {
            userResult = collection.find(filter)
                    .skip(skip)
                    .limit(pageRequest.pageSize())
                    .into(new ArrayList<>())
                    .stream()
                    .map(UserMapper::from)
                    .toList();
        }

        return new Paged<>(userResult, (long)Math.ceil(allDocuments / (double)pageRequest.pageSize()), allDocuments);
    }

    public User insertOne(User user) {
        if (user.getName() == null ||
            user.getAccessGroups() == null ||
            user.getUserName() == null) {
            throw new InvalidUserException("One or more fields are missing");
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);
        BsonValue insertedId = collection.insertOne(new Document("id", UUID.randomUUID().toString())
                .append("name", user.getName())
                .append("passKey", new String(Base64.getEncoder().encode((user.getUserName() + ":" + UUID.randomUUID()).getBytes())))
                .append("accessGroups", String.join(",", user.getAccessGroups()))
                .append("userName", user.getUserName())
                .append("active", true)).getInsertedId();

        assert insertedId != null;

        return Optional.ofNullable(collection.find(new Document("_id", insertedId))
                        .first())
                .map(UserMapper::from)
                .orElseThrow();
    }

    public void removeOne(String identifier) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier);
        DeleteResult deleteResult = collection.deleteOne(filter);

        assert deleteResult.getDeletedCount() == 1;
    }

    public void editOne(String identifier, User user) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier);

        Document replacement = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow();

        replacement.append("active", user.getActive())
                .append("accessGroups", String.join(",", user.getAccessGroups()))
                .append("userName", user.getUserName())
                .append("name", user.getName());

        UpdateResult updateResult = collection.replaceOne(filter, replacement);
        assert updateResult.getModifiedCount() == 1;
    }

    public User findOne(String identifier) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier);

        Document result = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow();

        return UserMapper.from(result);
    }

    public void changePassword(String identifier, User user) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);

        Document filter = new Document("id", identifier);

        Document replacement = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow();
        replacement.append("passKey", new String(Base64.getEncoder().encode((replacement.get("userName") + ":" + user.getPasskey()).getBytes())));
        UpdateResult updateResult = collection.replaceOne(filter, replacement);
        assert updateResult.getModifiedCount() == 1;
    }

    public User login(String base64Credentials) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(USER_COLLECTION);
        Document filter = new Document("passKey", base64Credentials);
        Document first = Optional.ofNullable(collection.find(filter)
                        .first())
                .orElseThrow(() -> new InvalidUserException("UserName or Password not found"));

        return UserMapper.from(first);
    }
}
//TODO can I have transactions on mongo?
