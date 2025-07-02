package org.slothmq.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slothmq.protocol.DestinationType;
import org.slothmq.protocol.MessageType;
import org.slothmq.protocol.ProtocolTransferObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

public class MessageReaderTest {
    static class FakeInputStream extends InputStream {
        private final String contents;
        private int pointer = 0;

        public FakeInputStream(String contents) {
            this.contents = contents;
        }

        @Override
        public int read() throws IOException {
            if (contents == null || contents.isEmpty()) {
                return 0;
            }

            if (pointer >= contents.length()){
                return -1;
            }

            return contents.charAt(pointer++);
        }
    }

    @ParameterizedTest
    @CsvSource({"CONSUME,QUEUE,queue.command.order.purchase,{}",
                "PRODUCE,QUEUE,queue.command.order.purchase,{visible: true}",
                "CONSUME,TOPIC,topic.listener.order.purchased,{}",
                "PRODUCE,TOPIC,topic.listener.order.purchased,{visible: true}"})
    public void givenTheStreamHasDataCheckOutput(String message,
                                                 String destination,
                                                 String address,
                                                 String contents) throws IOException {

        //given
        var messageContents = "--message: "+ message + "\n" +
                "--destination: "+ destination + "\n" +
                "--address: " + address + "\n" +
                "--timestamp: 1751045678592\n" +
                "--authentication: base64(admin:secret)\n" +
                "--contents: " + contents;

        var fakeInputStream = new FakeInputStream(messageContents);
        var messageReader = MessageReader.getInstance();
        var client = Mockito.mock(Socket.class);
        Mockito.when(client.getInputStream()).thenReturn(fakeInputStream);


        //when
        ProtocolTransferObject read = messageReader.read(client);

        //then
        assert read.getTimestamp().toEpochMilli() == 1751045678592L;
        assert read.getMessage() == MessageType.valueOf(message);
        assert read.getDestination() == DestinationType.valueOf(destination);
        assert Objects.equals(read.getAddress(), address);
//        TODO adjust here when the authentication is created
//        assert Objects.equals(read.getAuthentication(), "base64(admin:secret)");
        assert Objects.equals(read.getContents(), contents);
    }
}
