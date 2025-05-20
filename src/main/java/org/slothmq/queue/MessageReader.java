package org.slothmq.queue;

import org.slothmq.protocol.ProtocolMessageParser;
import org.slothmq.protocol.ProtocolTransferObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReader {
    private static final MessageReader INSTANCE = new MessageReader();
    private static final Logger LOG = LoggerFactory.getLogger(MessageReader.class);

    public static MessageReader getInstance() {
        return INSTANCE;
    }


    public ProtocolTransferObject read(Socket socketClient) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            char[] buffer = new char[1024];
            StringBuilder builder = new StringBuilder();
            while (true) {
                int read = br.read(buffer);
                if (read == -1) { //EOF
                    //nothing to read, exit
                    continue;
                }
                if (read == 0) {
                    //no bytes to read this time
                    continue;
                }
                builder.append(buffer);
                return ProtocolMessageParser.fromString(builder.toString());
            }
        } catch (IOException e) {
            //do nothing for now throw in  the future
            LOG.error("exception", e);
            throw new RuntimeException(e);
        }
    }
}
