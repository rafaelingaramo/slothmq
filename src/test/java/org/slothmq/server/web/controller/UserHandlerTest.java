package org.slothmq.server.web.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slothmq.FakeOutputStream;
import org.slothmq.server.user.User;
import org.slothmq.server.web.dto.PageRequest;
import org.slothmq.server.web.dto.Paged;
import org.slothmq.server.web.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class UserHandlerTest {
    @ParameterizedTest
    @CsvSource({"/api/users", "/api/users?lorem"})
    public void givenIvePassedNoPagingParametersVerifyResultAndCallsToGetAll(String uriVariation) throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var requestUri = new URI(uriVariation);
        Paged<User> pagedResult = buildPagedResult(List.of(new User(UUID.fromString("7d705eb2-1f3a-4ef5-adbd-e3f1c33baed2"), "jonah", "jameson", new String[]{"admin"}, true),
                new User(UUID.fromString("97365f06-9398-4b9d-8845-8363794e2d93"), "peter", "parker", new String[]{"admin", "viewer"}, false)));

        var expectedJson = "{\"content\":[{\"id\":\"7d705eb2-1f3a-4ef5-adbd-e3f1c33baed2\",\"name\":\"jonah\",\"userName\":\"jameson\",\"accessGroups\":[\"admin\"],\"active\":true},{\"id\":\"97365f06-9398-4b9d-8845-8363794e2d93\",\"name\":\"peter\",\"userName\":\"parker\",\"accessGroups\":[\"admin\",\"viewer\"],\"active\":false}],\"totalPages\":1,\"totalElements\":2,\"empty\":false}";

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(userService.listPaged(any())).thenReturn(pagedResult);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);

        //when
        userHandler.getAll(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(expectedJson, contents);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).listPaged(PageRequest.DEFAULT_PAGING);
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenIPassPagingParamsVerifyResultAndCallsToGetAll() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();

        var requestUri = new URI("/api/users?page=1&size=10&sortField=name&sortOrder=DESC&search=lorem");
        Paged<User> pagedResult = buildPagedResult(List.of(new User(UUID.fromString("7d705eb2-1f3a-4ef5-adbd-e3f1c33baed2"), "jonah", "jameson", new String[]{"admin"}, true),
                new User(UUID.fromString("97365f06-9398-4b9d-8845-8363794e2d93"), "peter", "parker", new String[]{"admin", "viewer"}, false)));

        var expectedJson = "{\"content\":[{\"id\":\"7d705eb2-1f3a-4ef5-adbd-e3f1c33baed2\",\"name\":\"jonah\",\"userName\":\"jameson\",\"accessGroups\":[\"admin\"],\"active\":true},{\"id\":\"97365f06-9398-4b9d-8845-8363794e2d93\",\"name\":\"peter\",\"userName\":\"parker\",\"accessGroups\":[\"admin\",\"viewer\"],\"active\":false}],\"totalPages\":1,\"totalElements\":2,\"empty\":false}";

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(userService.listPaged(any())).thenReturn(pagedResult);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);

        //when
        userHandler.getAll(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(expectedJson, contents);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).listPaged(PageRequest.parseOrGetDefaults(1, "10", "name", "DESC", "lorem"));
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenIHaveNoDataOnDatabaseValidateResultForGetAll() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();

        var requestUri = new URI("/api/users?page=1&size=10&sortField=name&sortOrder=DESC&search=lorem");
        Paged<User> pagedResult = buildPagedResult(null);

        var expectedJson = "\"[]\"";

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(userService.listPaged(any())).thenReturn(pagedResult);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);

        //when
        userHandler.getAll(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(expectedJson, contents);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).listPaged(PageRequest.parseOrGetDefaults(1, "10", "name", "DESC", "lorem"));
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }




    private Paged<User> buildPagedResult(List<User> contents) {
        if (contents == null) {
            return new Paged<>(Collections.emptyList(), 0, 0);
        }
        return new Paged<>(contents, 1, contents.size());
    }
}
