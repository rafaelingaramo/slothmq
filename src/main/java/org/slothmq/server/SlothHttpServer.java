package org.slothmq.server;

import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.queue.QueueHandler;
import org.slothmq.server.web.controller.HealthHandler;
import org.slothmq.server.web.controller.LoginHandler;
import org.slothmq.server.web.controller.QueueMessageHandler;
import org.slothmq.server.web.controller.UserHandler;
import org.slothmq.server.web.service.QueueMessagesService;
import org.slothmq.server.web.service.UserService;

import java.net.InetSocketAddress;

import static org.slothmq.server.configuration.CorsHandler.withCors;

public class SlothHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothHttpServer.class);
    private final MongoDatabase mongoDatabase;
    private final QueueHandler queueHandler;

    public SlothHttpServer(MongoDatabase mongoDatabase, QueueHandler queueHandler) {
        this.mongoDatabase = mongoDatabase;
        this.queueHandler = queueHandler;
    }

    public void start() {
        LOG.info("Initializing web server");
        try {
            //TODO make the server port a parameter
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/messages", withCors(new QueueMessageHandler(new QueueMessagesService(mongoDatabase,
                    queueHandler))));
            server.createContext("/api/users", withCors(new UserHandler(new UserService(mongoDatabase))));
            server.createContext("/api/login", withCors(new LoginHandler(new UserService(mongoDatabase))));
            server.createContext("/api/health", withCors(new HealthHandler()));
            server.setExecutor(null);
            server.start();
            LOG.info("Web server initialized");
            new UserService(mongoDatabase).initUserCollectionIfNeeded();
        } catch (Exception e) {
            LOG.error("Exception trying to initialize HTTP Server on 8080", e);
            throw new RuntimeException(e);
        }
    }
}
