package org.slothmq;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slothmq.server.Server;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

public class BaseIntegrationTestSingletonHolder {
    private static Server SERVER;
    private static boolean SERVER_STARTED = false;

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:6.0.11");
    static MongoClient mongoClient;
    static MongoDatabase mongoDatabase;

    public static void startServer() throws InterruptedException {
        if (SERVER_STARTED)
            return;

        mongodb.start();
        String replicaSetUrl = mongodb.getReplicaSetUrl();

        SERVER = new Server(replicaSetUrl);
        SERVER.start();

        SERVER_STARTED = true;

        Thread.sleep(1000);
    }

    public static void stopServer() {
        SERVER.stop();
        SERVER_STARTED = false;
    }

    public static MongoDatabase getDatabase() {
        if (mongoDatabase != null) {
            return mongoDatabase;
        }
        mongoClient = MongoClients.create(mongodb.getConnectionString());
        mongoDatabase = mongoClient.getDatabase("slothmq");
        return mongoDatabase;
    }
}
