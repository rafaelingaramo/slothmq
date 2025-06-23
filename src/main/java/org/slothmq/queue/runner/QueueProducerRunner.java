package org.slothmq.queue.runner;

import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;

public class QueueProducerRunner implements Runnable {
    private final ProtocolTransferObject letter;

    public QueueProducerRunner(ProtocolTransferObject letter) {
        this.letter = letter;
    }

    @Override
    public void run() {
        //adds message to database + adds onto the queue to be consumed, needs to be async
        QueueHandler.getInstance().pushToQueue(letter.getAddress(), letter.getContents());
    }
}
