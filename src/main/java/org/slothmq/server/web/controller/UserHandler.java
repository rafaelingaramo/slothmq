package org.slothmq.server.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.util.List;

public class UserHandler implements SlothHttpHandler {
    private final UserService userService;

    public UserHandler() {
        this.userService = new UserService();
    }

    @WebRoute(routeRegexp = "/api/users$", method = "GET")
    public void getAll(HttpExchange exchange) throws IOException  {
        Object pageRequest = new Object();
        List<?> pagedUserList = userService.listPaged(pageRequest);

        if (pagedUserList == null || pagedUserList.isEmpty()) {
            printRawResponse(exchange, "[]", 200);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        printRawResponse(exchange, mapper.writeValueAsString(pagedUserList), 200);
    }
}
