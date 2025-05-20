package org.slothmq.queue.runner;

import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.MasterQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class QueueConsumerRunner implements Runnable {
    private final ProtocolTransferObject letter;
    private final Socket socketClient;
    private static final Logger LOG = LoggerFactory.getLogger(QueueConsumerRunner.class);

    public QueueConsumerRunner(ProtocolTransferObject letter, Socket socketClient) {
        this.letter = letter;
        this.socketClient = socketClient;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);

            Object object = MasterQueue.getInstance().consumeFromQueue(letter.getAddress());
            if (object != null) {
                out.println(object);
            }
        } catch (IOException e) {
            //do nothing
            LOG.error("exception while consuming the queue", e);
        }
    }
}
