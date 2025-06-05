package org.slothmq.server.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.dto.PageRequest;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * SlothHttpHandler is a default interface that allows the user to easily reuse print and or route methods inside its own controller
 */
public class SlothHttpHandler implements HttpHandler {
    static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void handle(HttpExchange exchange) {
        routeRequests(exchange);
    }

    void routeRequests(HttpExchange exchange) {
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
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void printEmptyResponse(HttpExchange exchange, Integer httpCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(httpCode, 0);
    }

    protected void printRawResponse(HttpExchange exchange, Object object, Integer httpCode) throws IOException {
        String response = MAPPER.writeValueAsString(object);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(httpCode, response.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response.getBytes());
        }
    }

    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        String[] split = query.split("[?&]");

        for (String s : split) {
            String[] keyVal = s.split("=");
            String key = URLDecoder.decode(keyVal[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyVal[1], StandardCharsets.UTF_8);
            params.put(key, value);
        }

        return params;
    }

    protected PageRequest extractPageRequest(HttpExchange exchange) {
        String stringQueryParams = exchange.getRequestURI().getQuery();

        if (stringQueryParams == null) {
            return PageRequest.DEFAULT_PAGING;
        }

        Map<String, String> queryParams = parseQueryParams(stringQueryParams);

        if (!queryParams.containsKey("page")) {
            return PageRequest.DEFAULT_PAGING;
        }

        Integer page = Integer.valueOf(queryParams.get("page"));
        String pageSize = queryParams.get("pageSize");
        String sortField = queryParams.get("sortField");
        String sortOrder = queryParams.get("sortOrder");

        return PageRequest.parseOrGetDefaults(page, pageSize, sortField, sortOrder);
    }

    protected <T> T extractRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null) {
            builder.append(line);
        }

        return MAPPER.readValue(builder.toString(), clazz);
    }
}
