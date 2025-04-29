package br.slothmq.client;

import br.slothmq.Message;
import br.slothmq.Queue;

public class MockedClient {
    public static void main(String[] args) {
        new Queue("queue.command.order.purchase").postMessage(new Message("Hello world"));
    }
}
