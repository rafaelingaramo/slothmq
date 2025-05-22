package org.slothmq.server;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.server.configuration.CorsHandler;
import org.slothmq.web.controller.QueueMessageHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.slothmq.server.configuration.CorsHandler.withCors;

public class SlothHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothHttpServer.class);

    public void start() {
        LOG.info("Initializing web server");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/messages", withCors(new QueueMessageHandler()));
            server.setExecutor(null);
            server.start();
            LOG.info("Web server initialized");
        } catch (IOException e) {
            LOG.error("Exception trying to initialize HTTP Server on 8080", e);
            throw new RuntimeException(e);
        }
    }
}
