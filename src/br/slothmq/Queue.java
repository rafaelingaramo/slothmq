package br.slothmq;

import br.slothmq.client.Client;

public class Queue {
    private final String queueName;

    public Queue(String queueName) {
        this.queueName = queueName;
    }

    public void postMessage(Message message) {
        Client client = new Client();
        message.setQueueName(queueName);
        client.postToQueue(message);
    }
}
