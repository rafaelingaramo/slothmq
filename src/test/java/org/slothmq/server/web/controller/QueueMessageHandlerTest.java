package org.slothmq.server.web.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slothmq.FakeOutputStream;
import org.slothmq.dto.Tuple;
import org.slothmq.server.web.service.QueueMessagesService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class QueueMessageHandlerTest {
    @Test
    public void givenThereAreSomeQueuesValidateResult() throws IOException {
        //given
        var queueMessageService = mock(QueueMessagesService.class);
        var queueMessageHandler = new QueueMessageHandler(queueMessageService);
        List<Tuple<String, Integer>> queueMessageList = Arrays.asList(new Tuple<>("queue.command.order.purchase", 1000),
                new Tuple<>("queue.command.quote.finish", 1000),
                new Tuple<>("topic.quote.sendQuote", 200));
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var expectedJson = "[{\"key\":\"queue.command.order.purchase\",\"value\":1000},{\"key\":\"queue.command.quote.finish\",\"value\":1000},{\"key\":\"topic.quote.sendQuote\",\"value\":200}]";
        //stubs
        when(queueMessageService.getQueueNames())
                .thenReturn(queueMessageList);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);

        //when
        queueMessageHandler.getAll(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert contents.equals(expectedJson);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(queueMessageService, times(1)).getQueueNames();
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenThereAreNoQueuesValidateResult() throws IOException {
        //given
        var queueMessageService = mock(QueueMessagesService.class);
        var queueMessageHandler = new QueueMessageHandler(queueMessageService);
        List<Tuple<String, Integer>> queueMessageList = new ArrayList<>();
        var fakeOutputStream = new FakeOutputStream();
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();
        var expectedJson = "\"[]\"";
        //stubs
        when(queueMessageService.getQueueNames())
                .thenReturn(queueMessageList);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(fakeOutputStream);

        //when
        queueMessageHandler.getAll(httpExchange);
        var contents = fakeOutputStream.getContents();

        //then
        assert contents.equals(expectedJson);
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(queueMessageService, times(1)).getQueueNames();
        verify(httpExchange, times(1)).sendResponseHeaders(200, contents.length());
    }

    @Test
    public void givenValidPathValidatePurgeMessages() throws URISyntaxException, IOException {
        //given
        var queueMessageService = mock(QueueMessagesService.class);
        var queueMessageHandler = new QueueMessageHandler(queueMessageService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getRequestURI()).thenReturn(new URI("/api/messages/queue.command.order.purchase"));
        doNothing().when(queueMessageService).purgeFromCollection(anyString());

        //when
        queueMessageHandler.purgeMessages(httpExchange);

        //then
        assert Objects.equals(headers.get("Content-Type").getFirst(), "application/json");
        verify(httpExchange, times(1)).sendResponseHeaders(204, 0);
        verify(queueMessageService, times(1)).purgeFromCollection("queue.command.order.purchase");
    }

    @Test
    public void givenInvalidPathValidatePurgeMessagesException() throws URISyntaxException, IOException {
        //given
        var queueMessageService = mock(QueueMessagesService.class);
        var queueMessageHandler = new QueueMessageHandler(queueMessageService);
        var httpExchange = mock(HttpExchange.class);
        var headers = new Headers();

        //stubs
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getRequestURI()).thenReturn(new URI("/api/messages/queue.command.order.purchase/lorem"));
        doNothing().when(queueMessageService).purgeFromCollection(anyString());

        //when
        Assertions.assertThrows(RuntimeException.class, () ->
            queueMessageHandler.purgeMessages(httpExchange));
    }
}
