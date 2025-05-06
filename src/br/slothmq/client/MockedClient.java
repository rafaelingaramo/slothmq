package br.slothmq.client;

public class MockedClient {
    public static void main(String[] args) {
        new Sender().postToQueue("Hello world", "queue.command.order.purchase");
    }
}
