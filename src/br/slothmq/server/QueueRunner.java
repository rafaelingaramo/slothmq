package br.slothmq.server;

import br.slothmq.Message;
import java.net.Socket;

public class QueueRunner implements Runnable {
    private final Socket socketClient;

    public QueueRunner(Socket socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    public void run() {
        //adds message to database + adds onto the queue to be consumed, needs to be async
        //TODO not into database yet
        MessageReader messageReader = MessageReader.getInstance();
        Message message = messageReader.read(socketClient);
        MasterQueue.getInstance().pushToQueue(message.getQueueName(), message.getContents());
    }
}
