package org.slothmq.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(() -> new SlothHttpServer().start());
        executorService.submit(() -> new SlothSocketServer().start());
        executorService.submit(() -> {
            try {
                new SlothNettyWebSocketServer().start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread.currentThread().join();
        //TODO use NIO for non-blocking communication
    }
}
