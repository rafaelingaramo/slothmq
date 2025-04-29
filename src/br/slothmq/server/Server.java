package br.slothmq.server;

import br.slothmq.Message;
import br.slothmq.exception.IncommunicableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server() throws SQLException {
//        Connection connection = DriverManager.getConnection("jdbc:h2:./test", "sa", "");
//        connection.close();
    }

    public static void main(String[] args) throws SQLException {
        new Server().start();
    }

    private void start() {

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                LOG.info("waiting new connections");
                Socket socketClient = serverSocket.accept();
                LOG.debug("new connection from: {}", socketClient.getInetAddress());
                Thread.ofVirtual().start(new QueueRunner(socketClient));
            }
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }
}
