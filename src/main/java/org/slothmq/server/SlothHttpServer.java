package org.slothmq.server;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.web.controller.QueueMessageHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SlothHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothHttpServer.class);

    public void start() {
        LOG.info("Initializing web server");
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/health", exchange -> {
                String response = "Winamp the Llama";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            server.createContext("/messages", new QueueMessageHandler());
            server.setExecutor(null);
            server.start();
            LOG.info("Web server initialized");
        } catch (IOException e) {
            LOG.error("Exception trying to initialize HTTP Server on 8080", e);
            throw new RuntimeException(e);
        }
    }
}
