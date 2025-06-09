package org.slothmq.server.web.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;

import java.io.IOException;
import java.util.HashMap;

public class HealthHandler extends SlothHttpHandler {
    @WebRoute(routeRegexp = "/api/health$", method = "GET")
    public void health(HttpExchange exchange) throws IOException {
        HashMap<String, String> health = new HashMap<>();
        health.put("status", "ok");
        printRawResponse(exchange, health, 200);
    }
}
