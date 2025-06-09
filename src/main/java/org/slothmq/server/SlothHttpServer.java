package org.slothmq.server;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.server.web.controller.HealthHandler;
import org.slothmq.server.web.controller.LoginHandler;
import org.slothmq.server.web.controller.QueueMessageHandler;
import org.slothmq.server.web.controller.UserHandler;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.slothmq.server.configuration.CorsHandler.withCors;

public class SlothHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothHttpServer.class);

    public void start() {
        LOG.info("Initializing web server");
        try {
            //TODO make the server port a parameter
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/messages", withCors(new QueueMessageHandler()));
            server.createContext("/api/users", withCors(new UserHandler()));
            server.createContext("/api/login", withCors(new LoginHandler()));
            server.createContext("/api/health", withCors(new HealthHandler()));
            //TODO make the executor handle exceptions and/or other errors
            server.setExecutor(null);
            server.start();
            LOG.info("Web server initialized");
            new UserService().initUserCollectionIfNeeded();
        } catch (Exception e) {
            LOG.error("Exception trying to initialize HTTP Server on 8080", e);
            throw new RuntimeException(e);
        }
    }
}
