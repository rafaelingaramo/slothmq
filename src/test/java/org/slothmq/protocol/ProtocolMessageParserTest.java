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

    @ParameterizedTest
    @CsvSource({"CONSUME,QUEUE,queue.command.order.purchase",
    "PRODUCE,TOPIC,topic.listener.order.purchase"})
    public void givenIHaveAValidObjectTransformIntoAString(String message,
                                                           String destination,
                                                           String address) {
        //given
        Instant now = Instant.now();
        ProtocolTransferObject protocolTransferObject = new ProtocolTransferObject();
        protocolTransferObject.setMessage(MessageType.valueOf(message));
        protocolTransferObject.setDestination(DestinationType.valueOf(destination));
        protocolTransferObject.setAddress(address);
        protocolTransferObject.setTimestamp(now);
        protocolTransferObject.setAuthentication("base64(admin:secret)");
        //when
        String protocolString = ProtocolMessageParser.fromObject(protocolTransferObject);

        //then
        assert protocolString.contains("--message: " + message);
        assert protocolString.contains("--destination: " + destination);
        assert protocolString.contains("--address: " + address);
        assert protocolString.contains("--timestamp: " + now.toEpochMilli());
        assert protocolString.contains("--authentication: base64(admin:secret)");

    }
}
