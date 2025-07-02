package org.slothmq.server.web.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.SlothSharedResources;
import org.slothmq.server.jwt.JwtUtil;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.dto.JwtToken;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.util.Base64;

public class LoginHandler extends SlothHttpHandler {
    private final UserService userService = new UserService(SlothSharedResources.MONGO_DATABASE);

    @WebRoute(routeRegexp = "/api/login$", method = "POST")
    public void login(HttpExchange exchange) throws IOException {
        User user = extractRequestBody(exchange, User.class);

        User loggedUser = userService.login(Base64.getEncoder().encodeToString((user.getUserName() + ":" + user.getPasskey()).getBytes()));
        JwtToken jwtToken = JwtUtil.generateToken(loggedUser.getUserName(), loggedUser.getAccessGroups());
        printRawResponse(exchange, jwtToken, 200);
    }
}
