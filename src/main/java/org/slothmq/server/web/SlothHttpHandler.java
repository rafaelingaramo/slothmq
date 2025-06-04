package org.slothmq.server.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slothmq.server.web.annotation.WebRoute;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

/**
 * SlothHttpHandler is a default interface that allows the user to easily reuse print and or route methods inside its own controller
 */
public interface SlothHttpHandler extends HttpHandler {
    @Override
    default void handle(HttpExchange exchange) {
        routeRequests(exchange);
    }

    default void routeRequests(HttpExchange exchange) {
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

    default void printEmptyResponse(HttpExchange exchange, Integer httpCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(httpCode, 0);
    }

    default void printRawResponse(HttpExchange exchange, String response, Integer httpCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(httpCode, response.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response.getBytes());
        }
    }
}
