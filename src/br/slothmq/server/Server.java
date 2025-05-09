package br.slothmq.server;

import br.slothmq.exception.IncommunicableException;
import br.slothmq.protocol.MessageType;
import br.slothmq.protocol.ProtocolTransferObject;
import br.slothmq.server.runner.QueueConsumerRunner;
import br.slothmq.server.runner.QueueProducerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server() throws SQLException {
//        Connection connection = DriverManager.getConnection("jdbc:h2:./test", "sa", "");
//        connection.close();
    }

    public static void main(String[] args) throws SQLException {
        new Server().start();
    }

    //TODO revisit how to leave process opened
    private void start() {
        ScheduledExecutorService service = null;
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            service = Executors.newSingleThreadScheduledExecutor();
            while (true) {
                LOG.info("waiting new connections");
                Socket socketClient = serverSocket.accept();
                LOG.debug("new connection from: {}", socketClient.getInetAddress());

                ProtocolTransferObject letter = MessageReader.getInstance().read(socketClient);
                if (letter.getMessage() == MessageType.CONSUME) {
                    service.scheduleAtFixedRate(new QueueConsumerRunner(letter, socketClient), 0, 500, TimeUnit.MILLISECONDS);
                } else if (letter.getMessage() == MessageType.PRODUCE){
                    Thread.ofVirtual().start(new QueueProducerRunner(letter));
                }
            }
        } catch (IOException e) {
            throw new IncommunicableException(e);
        } finally {
            if (service != null)
                service.shutdown();
        }
    }
}
