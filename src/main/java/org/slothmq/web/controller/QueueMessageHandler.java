package org.slothmq.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slothmq.web.service.QueueMessagesService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

public class QueueMessageHandler implements HttpHandler {
    private final QueueMessagesService queueMessagesService;

    public QueueMessageHandler() {
        this.queueMessagesService = new QueueMessagesService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String[] split = requestURI.toString().split("/");
        if (split.length == 0) { //

        }

        List<String> queueNames = queueMessagesService.getQueueNames();

        if (queueNames == null || queueNames.isEmpty()) {
            printRawResponse(exchange, "[]", 204);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        printRawResponse(exchange, mapper.writeValueAsString(queueNames), 200);
    }

    private void printRawResponse(HttpExchange exchange, String response, Integer httpCode) throws IOException {
        exchange.sendResponseHeaders(httpCode, response.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response.getBytes());
        }
    }
}
