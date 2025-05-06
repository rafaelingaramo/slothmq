package br.slothmq.protocol;


import java.time.Instant;
import java.util.Arrays;

public class ProtocolMessageParser {
    /*
    *
        --message: produce
        --destination: queue
        --address: queue.command.order.purchase
        --timestamp: 12314214141
        --authentication: base64(admin:1234)
        --contents: [a3fe2c4]
    */

    //TODO make cleaner or in a better way, like a tokenizer ? 
    public static ProtocolTransferObject fromString(String message) {
        ProtocolTransferObject protocolTransferObject = new ProtocolTransferObject();
        String[] split = message.split("\n");

        Arrays.stream(split)
                .filter(s -> s.contains("message"))
                .findFirst()
                .ifPresent(value -> protocolTransferObject
                        .setMessage(MessageType.valueOf(value.replaceAll("--message:", "").trim())));

        Arrays.stream(split)
                .filter(s -> s.contains("destination"))
                .findFirst()
                .ifPresent(value -> protocolTransferObject
                        .setDestination(DestinationType.valueOf(value.replaceAll("--destination:", "").trim())));

        Arrays.stream(split)
                .filter(s -> s.contains("address"))
                .findFirst()
                .ifPresent(value -> protocolTransferObject
                        .setAddress(value.replaceAll("--address:", "").trim()));

        Arrays.stream(split)
                .filter(s -> s.contains("timestamp"))
                .findFirst()
                .ifPresent(value -> protocolTransferObject
                        .setTimestamp(Instant.ofEpochMilli(Long.parseLong(value.replaceAll("--timestamp:", "").trim()))));

        Arrays.stream(split)
                .filter(s -> s.contains("contents"))
                .findFirst()
                .ifPresent(value -> protocolTransferObject
                        .setContents(value.replaceAll("--contents:", "").trim().getBytes()));

        return protocolTransferObject;
    }

    public static String fromObject(ProtocolTransferObject message) {
        return message.toString();
    }

    public static void main(String[] args) {
        String example = """
                --message: PRODUCE
                        --destination: QUEUE
                        --address: queue.command.order.purchase
                        --timestamp: 12314214141
                        --authentication: base64(admin:1234)
                        --contents: [a3fe2c4]""";
        ProtocolTransferObject protocolTransferObject = ProtocolMessageParser.fromString(example);
        System.out.println(protocolTransferObject);
    }
}
