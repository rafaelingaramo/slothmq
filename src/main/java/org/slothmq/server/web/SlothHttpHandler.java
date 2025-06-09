package org.slothmq.server.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slothmq.exception.InvalidUserException;
import org.slothmq.exception.UnauthorizedAccessException;
import org.slothmq.server.jwt.JwtUtil;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.dto.PageRequest;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * SlothHttpHandler is a default interface that allows the user to easily reuse print and or route methods inside its own controller
 */
public class SlothHttpHandler implements HttpHandler {
    private static final ObjectMapper MAPPER;

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
                            boolean methodMatches = annotation.method().equals(requestMethod) &&
                                    requestURI.toString().matches(annotation.routeRegexp());

                            if (!methodMatches) return false;


                            if (annotation.needsAuthentication()) {
                                checkAuthentication(exchange);
                                //means that the method matches, although there's no authorization to it
                                if (!annotation.authorizationGroups().isEmpty()) {
                                    checkAuthorization(exchange, annotation.authorizationGroups());
                                }
                            }

                            return true;
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
        String pageSize = queryParams.get("size");
        String sortField = queryParams.get("sortField");
        String sortOrder = queryParams.get("sortOrder");
        String search = queryParams.get("search");

        return PageRequest.parseOrGetDefaults(page, pageSize, sortField, sortOrder, search);
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

    private void checkAuthentication(HttpExchange exchange) {
        String authentication = exchange.getRequestHeaders().getFirst("Authentication");

        if (authentication == null || !authentication.startsWith("Bearer ")) {
            throw new InvalidUserException("Invalid user/pass combination");
        }

        String base64Credentials = authentication.substring("Bearer ".length());
        JwtUtil.verifyToken(base64Credentials);
    }

    private void checkAuthorization(HttpExchange exchange, String authGroups) {
        String authentication = exchange.getRequestHeaders().getFirst("Authentication");

        if (authentication == null || !authentication.startsWith("Bearer ")) {
            throw new InvalidUserException("Invalid user/pass combination");
        }

        String base64Credentials = authentication.substring("Bearer ".length());
        List<String> audience = JwtUtil.verifyAccessGroups(base64Credentials);
        boolean hasAudience = audience.stream()
                .anyMatch(a -> Arrays.asList(authGroups.split(",")).contains(a));

        if (!hasAudience) {
            throw new UnauthorizedAccessException(base64Credentials, authGroups);
        }
    }
}
