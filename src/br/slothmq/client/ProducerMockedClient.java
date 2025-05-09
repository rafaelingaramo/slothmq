package br.slothmq.client;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ProducerMockedClient {
    public static void main(String[] args) throws JsonProcessingException {

        Sender sender = new Sender();
        for (int i=0;i<1000;i++) {
            sender.postToQueue("Winamp the Lhama: " + i, "queue.command.order.purchase");
        }
    }
}
