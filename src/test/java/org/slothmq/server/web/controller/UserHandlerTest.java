package org.slothmq.server.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slothmq.FakeInputStream;
import org.slothmq.FakeOutputStream;
import org.slothmq.server.user.User;
import org.slothmq.server.web.SlothHttpHandler;
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

    @Test
    public void givenValidDatabaseRecordMakeSureRecordIsReturnedOnFindOne() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var uuid = UUID.randomUUID();
        var requestUri = new URI("/api/users/" + uuid);
        var requestedUser = new User(uuid, "lorem", "ipsum", new String[1], true);
        var expectedJson = "{\"id\":\""+ uuid + "\",\"name\":\"lorem\",\"userName\":\"ipsum\",\"accessGroups\":[null],\"active\":true}";

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);
        when(userService.findOne(uuid.toString())).thenReturn(requestedUser);

        //when
        userHandler.findOne(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(expectedJson, contents);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).findOne(uuid.toString());
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenNoValidDatabaseRecordMakeSureNoUserIsReturned() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var uuid = UUID.randomUUID();
        var requestUri = new URI("/api/users/" + uuid);
        var expectedJson = "\"[]\"";

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);
        when(userService.findOne(uuid.toString())).thenReturn(null);

        //when
        userHandler.findOne(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(expectedJson, contents);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).findOne(uuid.toString());
        verify(httpExchange, times(1)).sendResponseHeaders(404, contents.length());
    }

    @Test
    public void givenValidUserInputMakeSureServiceIsCalledWithCorrectParameters() throws IOException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var userInput = new User(null, "lorem", "ipsum", new String[]{"viewer"}, true);
        var expectedId = UUID.randomUUID();
        var fakeOutputStream = new FakeOutputStream();
        String userStringInput = SlothHttpHandler.MAPPER.writeValueAsString(userInput);
        var expectedUserInput = new User(expectedId, userInput.getName(), userInput.getUserName(),
                userInput.getAccessGroups(), userInput.getActive());
        var expectedOutput = "{\"id\":\""+ expectedId + "\",\"name\":\"lorem\",\"userName\":\"ipsum\",\"accessGroups\":[\"viewer\"],\"active\":true}";
        //stubs
        when(httpExchange.getRequestBody()).thenReturn(new FakeInputStream(userStringInput));
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);
        when(userService.insertOne(any())).thenReturn(expectedUserInput);

        //when
        userHandler.insertOne(httpExchange);
        String contents = fakeOutputStream.getContents();

        //then
        assert Objects.equals(contents, expectedOutput);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).insertOne(argThat((user) ->
                Objects.equals(user.getName(), "lorem") && Objects.equals(user.getUserName(), "ipsum") &&
                        user.getActive() && Objects.deepEquals(user.getAccessGroups(), new String[]{"viewer"})));
        verify(httpExchange, times(1)).sendResponseHeaders(201, contents.length());
    }


    @Test
    public void givenValidDatabaseRecordMakeSureRecordIsDeleted() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var uuid = UUID.randomUUID();
        var requestUri = new URI("/api/users/" + uuid);

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);
        doNothing().when(userService).removeOne(uuid.toString());

        //when
        userHandler.deleteOne(httpExchange);

        //then
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).removeOne(uuid.toString());
        verify(httpExchange, times(1)).sendResponseHeaders(204, 0);
    }

    @Test
    public void givenValidDatabaseRecordMakeSureRecordIsEdited() throws IOException, URISyntaxException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var uuid = UUID.randomUUID();
        var requestUri = new URI("/api/users/" + uuid);
        var userInput = new User(null, "lorem", "ipsum", new String[]{"viewer"}, false);
        String userStringInput = SlothHttpHandler.MAPPER.writeValueAsString(userInput);

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);
        when(httpExchange.getRequestBody()).thenReturn(new FakeInputStream(userStringInput));
        doNothing().when(userService).editOne(eq(uuid.toString()), any());

        //when
        userHandler.editOne(httpExchange);

        //then
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).editOne(eq(uuid.toString()), argThat(user -> Objects.equals(user.getName(), "lorem")
            && Objects.equals(user.getUserName(), "ipsum") && Objects.equals(user.getAccessGroups()[0], "viewer") && !user.getActive()));
        verify(httpExchange, times(1)).sendResponseHeaders(204, 0);
    }

    @Test
    public void givenValidDatabaseRecordMakeSurePasswordIsChanged() throws URISyntaxException, IOException {
        //given
        var userService = mock(UserService.class);
        var userHandler = new UserHandler(userService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var uuid = UUID.randomUUID();
        var requestUri = new URI("/api/users/" + uuid + "/password");
        var expectedUser = new User();
        expectedUser.setPasskey("changedPassKey");

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getRequestURI()).thenReturn(requestUri);
        when(httpExchange.getRequestBody()).thenReturn(new FakeInputStream(SlothHttpHandler.MAPPER.writeValueAsString(expectedUser)));
        doNothing().when(userService).changePassword(eq(uuid.toString()), any());

        //when
        userHandler.changePassword(httpExchange);

        //then
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(userService, times(1)).changePassword(eq(uuid.toString()), argThat(user -> Objects.equals(user.getPasskey(), "changedPassKey")));
        verify(httpExchange, times(1)).sendResponseHeaders(204, 0);
    }

    private Paged<User> buildPagedResult(List<User> contents) {
        if (contents == null) {
            return new Paged<>(Collections.emptyList(), 0, 0);
        }
        return new Paged<>(contents, 1, contents.size());
    }
}
