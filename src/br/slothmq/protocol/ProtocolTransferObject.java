package br.slothmq.protocol;

import java.time.Instant;
import java.util.Arrays;

public class ProtocolTransferObject {
    /*
    *
        --message: produce
        --destination: queue
        --address: queue.command.order.purchase
        --timestamp: 12314214141
        --authentication: base64(admin:1234)
        --contents: [a3fe2c4]
    */

    private MessageType message;
    private DestinationType destination;
    private String address;
    private Instant timestamp;
    private String authentication;
    private byte[] contents;

    public static ProtocolTransferObject standardQueueLetter() {
        ProtocolTransferObject letter = new ProtocolTransferObject();
        letter.setTimestamp(Instant.now());
        letter.setMessage(MessageType.PRODUCE);
        letter.setDestination(DestinationType.QUEUE);

        return letter;
    }

    public MessageType getMessage() {
        return message;
    }

    public void setMessage(MessageType message) {
        this.message = message;
    }

    public DestinationType getDestination() {
        return destination;
    }

    public void setDestination(DestinationType destination) {
        this.destination = destination;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "--message: " + message + "\n" +
                "--destination: " + destination + "\n" +
                "--address: " + address + "\n" +
                "--timestamp: " + timestamp + "\n" +
                "--authentication: " + authentication + "\n" +
                "--contents: " + Arrays.toString(contents);
    }
}
