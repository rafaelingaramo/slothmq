package org.slothmq.queue.runner;

import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;

public class QueueProducerRunner implements Runnable {
    private final ProtocolTransferObject letter;
    private final QueueHandler queueHandler;

    public QueueProducerRunner(ProtocolTransferObject letter,
                               QueueHandler queueHandler) {
        this.letter = letter;
        this.queueHandler = queueHandler;
    }

    @Override
    public void run() {
        //adds message to database + adds onto the queue to be consumed, needs to be async
        queueHandler.pushToQueue(letter.getAddress(), letter.getContents());
    }
}
