package br.slothmq.server;

import br.slothmq.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageReader {
    private static final MessageReader INSTANCE = new MessageReader();

    public static MessageReader getInstance() {
        return INSTANCE;
    }


    public Message read(Socket socketClient) {

        try {
            DataInputStream dis = new DataInputStream(socketClient.getInputStream());
            byte[] buffer = new byte[1024];
            while (true) {
                int read = dis.read(buffer);
                if (read == -1) { //EOF
                    //nothing to read, exit
                    break;
                }
                if (read == 0) {
                    //no bytes to read this time
                    continue;
                }
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(buffer, Message.class);
            }
        } catch (IOException e) {
            //do nothing for now throw in  the future
        } finally {
            try {
                socketClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e); //? TODO review this
            }
        }
        return null;
    }
}
