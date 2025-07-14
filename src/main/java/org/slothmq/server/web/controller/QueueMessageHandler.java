package org.slothmq.server.web.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slothmq.dto.Tuple;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.service.QueueMessagesService;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueMessageHandler extends SlothHttpHandler {
    private final QueueMessagesService queueMessagesService;

    public QueueMessageHandler(QueueMessagesService queueMessagesService) {
        this.queueMessagesService = queueMessagesService;
    }

    @WebRoute(routeRegexp = "/api/messages$", method = "GET", needsAuthentication = true, authorizationGroups = "viewer,admin")
    public void getAll(HttpExchange exchange) throws IOException  {
        List<Tuple<String, Integer>> queueNames = queueMessagesService.getQueueNames();

        if (queueNames == null || queueNames.isEmpty()) {
            printRawResponse(exchange, "[]", 200);
            return;
        }

        printRawResponse(exchange, queueNames, 200);
    }

    @WebRoute(routeRegexp = "/api/messages/([\\w\\-.]+)$", method = "DELETE", needsAuthentication = true, authorizationGroups = "admin")
    public void purgeMessages(HttpExchange exchange) throws IOException  {
        String path = exchange.getRequestURI().getPath();

        Pattern pattern = Pattern.compile("/api/messages/([\\w\\-.]+)$");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException(); //todo this must be moved to upper interface
        }
        String pathParameter = matcher.group(1);

        queueMessagesService.purgeFromCollection(pathParameter);

        printEmptyResponse(exchange, 204);
    }
}
