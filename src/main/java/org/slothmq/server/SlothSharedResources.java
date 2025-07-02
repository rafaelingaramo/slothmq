package org.slothmq.server;

import com.mongodb.client.MongoDatabase;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.queue.QueueHandler;

public class SlothSharedResources {
    public static final MongoDatabase MONGO_DATABASE;
    public static final QueueHandler QUEUE_HANDLER;

    static {
        MongoConnector mongoConnector = new MongoConnector();
        MONGO_DATABASE = mongoConnector.getDatabase();
        QUEUE_HANDLER = new QueueHandler(MONGO_DATABASE);
    }

}
