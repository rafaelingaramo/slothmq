package org.slothmq.server.configuration;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CorsHandler implements HttpHandler {
    private final HttpHandler next;
    private static final Logger LOG = LoggerFactory.getLogger(CorsHandler.class);

    public CorsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        // Allow only localhost origin
        if (origin != null && (origin.contains("localhost:5173") || origin.contains("127.0.0.1:5173"))) {
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }

        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // No content
            return;
        }

        try {
            // continue with actual handler
            next.handle(exchange);
        } catch (Exception e) {
            LOG.error("exception routing the request", e);
            throw e;
        }
    }

    public static HttpHandler withCors(HttpHandler handler) {
        return new CorsHandler(handler);
    }
}
