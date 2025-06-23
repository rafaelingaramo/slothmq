package org.slothmq.protocol;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.Objects;

public class ProtocolMessageParserTest {
    @ParameterizedTest
    @CsvSource({"PRODUCE,QUEUE,queue.command.order.purchase", "CONSUME,QUEUE,queue.command.order.purchase",
        "PRODUCE,TOPIC,topic.purchased.orders"})
    public void givenFromStringReceivesInputValidateOutput(String message,
                                                           String destination,
                                                           String address) {
        //given
        Instant now = Instant.now();
        String input = "--message: " + message + "\n" +
                "--destination: " + destination + "\n" +
                "--address: " + address + "\n" +
                "--timestamp:" + now.toEpochMilli() + "\n" +
                "--authentication: base64(admin:1234)\n" +
                "--contents: {\"visible\": true}\n";

        //when
        ProtocolTransferObject protocolTransferObject = ProtocolMessageParser.fromString(input);
        //then
        assert protocolTransferObject.getMessage() == MessageType.valueOf(message);
        assert protocolTransferObject.getDestination() == DestinationType.valueOf(destination);
        assert Objects.equals(protocolTransferObject.getAddress(), address);
        assert protocolTransferObject.getTimestamp().toEpochMilli() == now.toEpochMilli();
        assert Objects.equals(protocolTransferObject.getContents(), "{\"visible\": true}");
    }
}
