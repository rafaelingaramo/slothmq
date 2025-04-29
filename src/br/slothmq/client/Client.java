package br.slothmq.client;

import br.slothmq.Message;
import br.slothmq.exception.IncommunicableException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

public class Client {

    public void postToQueue(Message message) {
        System.out.println("Trying to connect to server");
        try (Socket socket = new Socket("127.0.0.1", 9999)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("sending message");
            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(message);
            out.write(bytes);
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }
}
