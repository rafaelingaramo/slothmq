package org.slothmq.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerMockedClient {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 1; i++) {
            executorService.submit(() -> new Sender().consumeFromQueue("queue.command.order.purchase"));
        }

        Thread.currentThread().join();
    }
}
