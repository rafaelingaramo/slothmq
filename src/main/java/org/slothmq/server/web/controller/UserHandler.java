package org.slothmq.server.web.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.dto.PageRequest;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.util.List;

public class UserHandler extends SlothHttpHandler {
    private final UserService userService;

    public UserHandler() {
        this.userService = new UserService();
    }

    @WebRoute(routeRegexp = "/api/users.*?", method = "GET")
    public void getAll(HttpExchange exchange) throws IOException  {
        PageRequest pageRequest = extractPageRequest(exchange);
        List<User> pagedUserList = userService.listPaged(pageRequest);

        if (pagedUserList == null || pagedUserList.isEmpty()) {
            printRawResponse(exchange, "[]", 200);
            return;
        }

        printRawResponse(exchange, pagedUserList, 200);
    }

    @WebRoute(routeRegexp = "/api/users$", method = "POST")
    public void insertOne(HttpExchange exchange) throws IOException {
        User user = extractRequestBody(exchange, User.class);
        User createdUser = userService.insertOne(user);

        printRawResponse(exchange, createdUser, 201);
    }
}
