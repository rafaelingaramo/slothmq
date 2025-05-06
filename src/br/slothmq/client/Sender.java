package br.slothmq.client;

import br.slothmq.exception.IncommunicableException;
import br.slothmq.protocol.ProtocolMessageParser;
import br.slothmq.protocol.ProtocolTransferObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Sender {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    public void postToQueue(Object contents, String destination) {
        ProtocolTransferObject letter = ProtocolTransferObject.standardQueueLetter();
        letter.setContents("lorem ipsum".getBytes());
        letter.setAddress(destination);
        LOG.info("Trying to connect to server");
        try (Socket socket = new Socket("127.0.0.1", 9999)) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            LOG.warn("sending message");
            ObjectMapper mapper = new ObjectMapper();
            out.write(ProtocolMessageParser.fromObject(letter));
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }
}
