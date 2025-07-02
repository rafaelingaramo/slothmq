package org.slothmq.queue.runner;

import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class QueueConsumerRunner implements Runnable {
    private final ProtocolTransferObject letter;
    private final Socket socketClient;
    private final QueueHandler queueHandler;
    private static final Logger LOG = LoggerFactory.getLogger(QueueConsumerRunner.class);


    public QueueConsumerRunner(ProtocolTransferObject letter, Socket socketClient,
                               QueueHandler queueHandler) {
        this.letter = letter;
        this.socketClient = socketClient;
        this.queueHandler = queueHandler;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

            Object object = queueHandler.consumeFromQueue(letter.getAddress());
            if (object != null) {
                out.println(object);
            }
        } catch (IOException e) {
            //do nothing
            LOG.error("exception while consuming the queue", e);
        }
    }
}
