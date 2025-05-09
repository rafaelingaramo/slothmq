package br.slothmq.server.runner;

import br.slothmq.protocol.ProtocolTransferObject;
import br.slothmq.server.MasterQueue;

public class QueueProducerRunner implements Runnable {
    private final ProtocolTransferObject letter;

    public QueueProducerRunner(ProtocolTransferObject letter) {
        this.letter = letter;
    }

    @Override
    public void run() {
        //adds message to database + adds onto the queue to be consumed, needs to be async
        //TODO not into database yet
        MasterQueue.getInstance().pushToQueue(letter.getAddress(), letter.getContents());
    }
}
