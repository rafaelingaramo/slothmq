package org.slothmq.server.web.controller;

import com.sun.net.httpserver.HttpExchange;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
import org.slothmq.server.web.annotation.WebRoute;
import org.slothmq.server.web.dto.PageRequest;
import org.slothmq.server.web.dto.Paged;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserHandler extends SlothHttpHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @WebRoute(routeRegexp = "/api/users(\\?.*)?$", method = "GET", needsAuthentication = true, authorizationGroups = "admin,viewer")
    public void getAll(HttpExchange exchange) throws IOException  {
        PageRequest pageRequest = extractPageRequest(exchange);
        Paged<User> pagedUserList = userService.listPaged(pageRequest);

        if (pagedUserList == null || pagedUserList.isEmpty()) {
            printRawResponse(exchange, "[]", 200);
            return;
        }

        printRawResponse(exchange, pagedUserList, 200);
    }

    @WebRoute(routeRegexp = "/api/users/([^/?]+)", method = "GET", needsAuthentication = true, authorizationGroups = "admin,viewer")
    public void findOne(HttpExchange exchange) throws IOException  {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/api/users/([^/?]+)");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException();
        }

        String pathParameter = matcher.group(1);
        User user = userService.findOne(pathParameter);

        if (user == null) {
            printRawResponse(exchange, "[]", 404);
            return;
        }

        printRawResponse(exchange, user, 200);
    }

    @WebRoute(routeRegexp = "/api/users$", method = "POST", needsAuthentication = true, authorizationGroups = "admin")
    public void insertOne(HttpExchange exchange) throws IOException {
        User user = extractRequestBody(exchange, User.class);
        User createdUser = userService.insertOne(user);

        printRawResponse(exchange, createdUser, 201);
    }

    @WebRoute(routeRegexp = "/api/users/([\\w\\-.]+)$", method = "DELETE", needsAuthentication = true, authorizationGroups = "admin")
    public void deleteOne(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/api/users/([\\w\\-.]+)$");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException();
        }
        String pathParameter = matcher.group(1);

        userService.removeOne(pathParameter);
        printEmptyResponse(exchange, 204);
    }

    @WebRoute(routeRegexp = "/api/users/([\\w\\-.]+)$", method = "PUT", needsAuthentication = true, authorizationGroups = "admin")
    public void editOne(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/api/users/([\\w\\-.]+)$");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException();
        }
        String pathParameter = matcher.group(1);
        User user = extractRequestBody(exchange, User.class);

        userService.editOne(pathParameter, user);
        printEmptyResponse(exchange, 204);
    }

    @WebRoute(routeRegexp = "/api/users/([\\w\\-.]+)/password", method = "PUT", needsAuthentication = true, authorizationGroups = "admin")
    public void changePassword(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/api/users/([\\w\\-.]+)/password");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            throw new RuntimeException();
        }
        String pathParameter = matcher.group(1);
        User user = extractRequestBody(exchange, User.class);

        userService.changePassword(pathParameter, user);
        printEmptyResponse(exchange, 204);
    }
}
