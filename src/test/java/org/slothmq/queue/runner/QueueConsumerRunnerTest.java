package org.slothmq.queue.runner;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class QueueConsumerRunnerTest {

    static class FakeOutputStream extends OutputStream {
        StringBuilder internalBuffer = new StringBuilder();
        @Override
        public void write(int b) throws IOException {
            internalBuffer.append((char)b);
        }

        public String getContents() {
            return internalBuffer.toString();
        }
    }


    @Test
    public void givenIHaveAValidAddressAndValidOutputMakeSureItPrintsOut() throws IOException {
        //given
        var fakeOutputStream = new FakeOutputStream();
        var letter = new ProtocolTransferObject();
        letter.setAddress("any");
        var socket = Mockito.mock(Socket.class);
        var queueHandler = Mockito.mock(QueueHandler.class);
        var helloWorld = "Hello World";
        Mockito.when(socket.getOutputStream()).thenReturn(fakeOutputStream);
        Mockito.when(queueHandler.consumeFromQueue(Mockito.anyString()))
                .thenReturn(helloWorld);
        var queueConsumerRunner = new QueueConsumerRunner(letter, socket, queueHandler);

        //when
        queueConsumerRunner.run();

        //then
        String contents = fakeOutputStream.getContents();
        assert contents.contains(helloWorld);
    }

    @Test
    public void givenIHaveAValidAddressAndNullOutputMakeSureItDoesNotPrintsOut() throws IOException {
        //given
        var fakeOutputStream = new FakeOutputStream();
        var letter = new ProtocolTransferObject();
        letter.setAddress("any");
        var socket = Mockito.mock(Socket.class);
        var queueHandler = Mockito.mock(QueueHandler.class);

        Mockito.when(socket.getOutputStream()).thenReturn(fakeOutputStream);
        Mockito.when(queueHandler.consumeFromQueue(Mockito.anyString()))
                .thenReturn(null);
        var queueConsumerRunner = new QueueConsumerRunner(letter, socket, queueHandler);

        //when
        queueConsumerRunner.run();

        //then
        String contents = fakeOutputStream.getContents();
        assert contents.isEmpty();
    }
}
