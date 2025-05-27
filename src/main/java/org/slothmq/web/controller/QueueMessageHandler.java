package org.slothmq.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slothmq.dto.Tuple;
import org.slothmq.web.annotation.WebRoute;
import org.slothmq.web.service.QueueMessagesService;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueMessageHandler implements HttpHandler {
    private final QueueMessagesService queueMessagesService;

    public QueueMessageHandler() {
        this.queueMessagesService = new QueueMessagesService();
    }

    @Override
    public void handle(HttpExchange exchange) {
        routeRequests(exchange);
    }

    @WebRoute(routeRegexp = "/api/messages$", method = "GET")
    public void getAll(HttpExchange exchange) throws IOException  {
        List<Tuple<String, String>> queueNames = queueMessagesService.getQueueNames();

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

    //TODO if I want to have a separated route, use like this.
//    @WebRoute(routeRegexp = "/messages/(\\w$)/?", method = "GET")
//    public void getMessagesByQueueName(HttpExchange exchange) throws IOException  {
//        String path = exchange.getRequestURI().getPath();
//        Pattern pattern = Pattern.compile("/messages/(\\w$)/?");
//        Matcher matcher = pattern.matcher(path);
//        String pathParameter = matcher.group(1);
//
//
//        List<String> queueNames = queueMessagesService.getQueueNames();
//
//        if (queueNames == null || queueNames.isEmpty()) {
//            printRawResponse(exchange, "[]", 204);
//            return;
//        }
//
//        ObjectMapper mapper = new ObjectMapper();
//        printRawResponse(exchange, mapper.writeValueAsString(queueNames), 200);
//    }

    private void routeRequests(HttpExchange exchange) {
        URI requestURI = exchange.getRequestURI();
        String requestMethod = exchange.getRequestMethod();

        Method method = Arrays.stream(this.getClass().getMethods())
                .filter(m -> m.getAnnotations().length > 0)
                .filter(m -> Arrays.stream(m.getAnnotations())
                        .anyMatch(a -> {
                            if (!(a instanceof WebRoute annotation)) {
                                return false;
                            }
                            return annotation.method().equals(requestMethod) &&
                                    requestURI.toString().matches(annotation.routeRegexp());
                        }))
                .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(HttpExchange.class))
                .findAny().orElseThrow();

        try {
            method.invoke(this, exchange);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void printEmptyResponse(HttpExchange exchange, Integer httpCode) throws IOException {
        exchange.sendResponseHeaders(httpCode, 0);
    }

    private void printRawResponse(HttpExchange exchange, String response, Integer httpCode) throws IOException {
        exchange.sendResponseHeaders(httpCode, response.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response.getBytes());
        }
    }
}
