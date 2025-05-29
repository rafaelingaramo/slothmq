package org.slothmq.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.slothmq.dto.Tuple;
import org.slothmq.web.SlothHttpHandler;
import org.slothmq.web.annotation.WebRoute;
import org.slothmq.web.service.QueueMessagesService;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueMessageHandler implements SlothHttpHandler {
    private final QueueMessagesService queueMessagesService;

    public QueueMessageHandler() {
        this.queueMessagesService = new QueueMessagesService();
    }

    @WebRoute(routeRegexp = "/api/messages$", method = "GET")
    public void getAll(HttpExchange exchange) throws IOException  {
        List<Tuple<String, Integer>> queueNames = queueMessagesService.getQueueNames();

        if (queueNames == null || queueNames.isEmpty()) {
            printRawResponse(exchange, "[]", 200);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        printRawResponse(exchange, mapper.writeValueAsString(queueNames), 200);
    }

    @WebRoute(routeRegexp = "/api/messages/([\\w\\-.]+)$", method = "DELETE")
    public void purgeMessages(HttpExchange exchange) throws IOException  {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/api/messages/([\\w\\-.]+)");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException();
        }
        String pathParameter = matcher.group(1);

        queueMessagesService.purgeFromCollection(pathParameter);

        printEmptyResponse(exchange, 204);
    }
}
