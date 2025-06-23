package org.slothmq.server;

import org.slothmq.exception.IncommunicableException;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.protocol.MessageType;
import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;
import org.slothmq.queue.MessageReader;
import org.slothmq.queue.runner.QueueConsumerRunner;
import org.slothmq.queue.runner.QueueProducerRunner;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TODO shutdown hook, remove pool resources
public class SlothSocketServer {
    private static final MongoDatabase MONGO_DATABASE;
    private static final Logger LOG = LoggerFactory.getLogger(SlothSocketServer.class);
    private static final ScheduledExecutorService CONSUMER_THREAD_POOL = Executors.newScheduledThreadPool(100);
    private static final ExecutorService PRODUCER_SERVICE_POOL = Executors.newFixedThreadPool(100);

    static {
        MONGO_DATABASE = MongoConnector.getDatabaseInstance();
    }

    public static MongoDatabase getMongoDatabase() {
        return MONGO_DATABASE;
    }

    public void start() {
        QueueHandler.startQueueFromDatabase();
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                LOG.info("waiting new connections");
                Socket socketClient = serverSocket.accept();
                LOG.debug("new connection from: {}", socketClient.getInetAddress());

                ProtocolTransferObject letter = MessageReader.getInstance().read(socketClient);
                if (letter.getMessage() == MessageType.CONSUME) {
                    CONSUMER_THREAD_POOL.scheduleAtFixedRate(new QueueConsumerRunner(letter, socketClient), 0, 500, TimeUnit.MILLISECONDS);
                } else if (letter.getMessage() == MessageType.PRODUCE){
                    PRODUCER_SERVICE_POOL.execute(new QueueProducerRunner(letter));
                }
            }
        } catch (IOException e) {
            throw new IncommunicableException(e);
        }
    }
}
