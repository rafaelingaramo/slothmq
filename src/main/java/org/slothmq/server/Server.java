package org.slothmq.server;

import org.slothmq.server.jmx.JmxMetricsCollector;
import org.slothmq.server.websocket.LogWebSocketHandler;
import org.slothmq.server.websocket.MetricsWebSocketHandler;

import java.util.concurrent.*;

public class Server {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(() -> new SlothHttpServer().start());
        executorService.submit(() -> new SlothSocketServer().start());
        executorService.submit(() -> {
            try {
                new SlothNettyWebSocketServer().start(8081, "/logs", new LogWebSocketHandler());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.submit(() -> {
            try {
                new SlothNettyWebSocketServer().start(8082, "/metrics", new MetricsWebSocketHandler());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(JmxMetricsCollector::collectAndPush, 15, 15, TimeUnit.SECONDS);

        Thread.currentThread().join();
        //TODO use NIO for non-blocking communication
    }

}
