package org.slothmq.client;

import org.slothmq.exception.IncommunicableException;
import org.slothmq.protocol.ProtocolMessageParser;
import org.slothmq.protocol.ProtocolTransferObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Sender {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    public void postToQueue(Object contents, String destination) throws JsonProcessingException {
        ProtocolTransferObject letter = ProtocolTransferObject.standardQueueLetter();
        letter.setContents(new ObjectMapper().writeValueAsString(contents));
        letter.setAddress(destination);
        LOG.info("Trying to connect to server");
        try (Socket socket = new Socket("127.0.0.1", 9999)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            LOG.warn("sending message");
            out.println(ProtocolMessageParser.fromObject(letter));
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }

    public void consumeFromQueue(String destination) {
        ProtocolTransferObject letter = ProtocolTransferObject.standardConsumerLetter();
        letter.setAddress(destination);
        LOG.info("trying to connect consumer to server");
        try (Socket socket = new Socket("127.0.0.1", 9999)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            LOG.warn("consuming messages");
            out.println(ProtocolMessageParser.fromObject(letter));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) { //Waiting for EOS
                String response = bufferedReader.readLine();
                if (response == null) { //EOS break
                    break;
                }
                dispatchResponse(response);
            }
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }

    private void dispatchResponse(String response) {
        LOG.info("response from server: " + response);
    }
}
