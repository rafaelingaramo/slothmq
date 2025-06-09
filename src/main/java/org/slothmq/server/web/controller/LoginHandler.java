package org.slothmq.server.web.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.jwt.JwtUtil;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;

public class LoginHandler extends SlothHttpHandler {
    private final UserService userService = new UserService();

    @WebRoute(routeRegexp = "/api/login$", method = "POST")
    public void login(HttpExchange exchange) throws IOException {
        // Basic auth expected
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            printEmptyResponse(exchange, 401);
            return;
        }

        String base64Credentials = authHeader.substring("Basic ".length());

        User loggedUser = userService.login(base64Credentials);
        String jwtToken = JwtUtil.generateToken(loggedUser.getUserName(), loggedUser.getAccessGroups());

        printRawResponse(exchange, jwtToken, 200);
    }
}
