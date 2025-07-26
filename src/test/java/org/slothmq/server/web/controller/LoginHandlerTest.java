package org.slothmq.server.web.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slothmq.FakeInputStream;
import org.slothmq.FakeOutputStream;
import org.slothmq.exception.InvalidUserException;
import org.slothmq.server.user.User;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class LoginHandlerTest {
    @Test
    public void givenValidLoginMustReturnValidToken() throws IOException {
        //given
        var userService = mock(UserService.class);
        var loginHandler = new LoginHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var userName = "peter.parker";
        var passKey = "maryjane";
        var accessGroups = new String[]{"viewer"};
        var loginJson = "{\"userName\":\"" + userName + "\", \"passkey\": \"" + passKey + "\"}";
        var toBeVerifiedCred = Base64.getEncoder().encodeToString((userName + ":" + passKey).getBytes());
        var toBeReturnedUser = new User(UUID.randomUUID(), userName, userName, accessGroups, true);
        var headers = new Headers();
        var fakeOutputStream = new FakeOutputStream();

        //stubs
        when(httpExchange.getRequestBody()).thenReturn(new FakeInputStream(loginJson));
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(userService.login(toBeVerifiedCred)).thenReturn(toBeReturnedUser);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);

        //when
        loginHandler.login(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert contents.contains("\"token\":");
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).login(toBeVerifiedCred);
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenInvalidLoginMustRethrowException() throws IOException {
        //given
        var userService = mock(UserService.class);
        var loginHandler = new LoginHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var userName = "peter.parker";
        var passKey = "maryjane";
        var loginJson = "{\"userName\":\"" + userName + "\", \"passkey\": \"" + passKey + "\"}";
        var toBeVerifiedCred = Base64.getEncoder().encodeToString((userName + ":" + passKey).getBytes());
        var headers = new Headers();
        var fakeOutputStream = new FakeOutputStream();

        //stubs
        when(httpExchange.getRequestBody()).thenReturn(new FakeInputStream(loginJson));
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(userService.login(toBeVerifiedCred)).thenThrow(new InvalidUserException("Invalid User/Pass combination"));
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);

        //when
        Assertions.assertThrows(InvalidUserException.class, () ->
                loginHandler.login(httpExchange));
    }
}
