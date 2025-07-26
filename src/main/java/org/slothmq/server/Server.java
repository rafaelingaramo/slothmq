package org.slothmq.server;

import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.mongo.MongoConnector;
import org.slothmq.queue.QueueHandler;
import org.slothmq.server.jmx.JmxMetricsCollector;
import org.slothmq.server.websocket.LogWebSocketHandler;
import org.slothmq.server.websocket.MetricsWebSocketHandler;

import java.util.concurrent.*;

public class Server {
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final MongoDatabase mongoDatabase;
    private final QueueHandler queueHandler;
    private ScheduledFuture<?> jmxCollector;
    private Future<?> secondWebSocketServer;
    private Future<?> webSocketServer;
    private Future<?> socketServer;
    private Future<?> httpServerFuture;
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server() {
        this(new MongoConnector().getDatabase());
    }

    public Server(String mongoUri) {
        this(new MongoConnector(mongoUri).getDatabase());
    }

    private Server(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.queueHandler = new QueueHandler(mongoDatabase);
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
        Thread.currentThread().join();
    }

    public void start() {
        LOG.info("Initializing services");
        this.httpServerFuture  = executorService.submit(() -> new SlothHttpServer(mongoDatabase, queueHandler).start());
        this.socketServer = executorService.submit(() -> new SlothSocketServer(queueHandler).start());
        //TODO verify if there's a way to skip the handler by using overload methods
        this.webSocketServer = executorService.submit(() -> {
            try {
                new SlothNettyWebSocketServer().start(8081, "/logs", new LogWebSocketHandler());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        this.secondWebSocketServer = executorService.submit(() -> {
            try {
                new SlothNettyWebSocketServer().start(8082, "/metrics", new MetricsWebSocketHandler());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.jmxCollector = scheduledExecutor.scheduleAtFixedRate(JmxMetricsCollector::collectAndPush, 15, 15, TimeUnit.SECONDS);

        //TODO use NIO for non-blocking communication
    }

    public void stop() {
        LOG.info("Stopping services");
        if (jmxCollector != null) { jmxCollector.cancel(false); }
        if (secondWebSocketServer != null) { secondWebSocketServer.cancel(false); }
        if (webSocketServer != null) { webSocketServer.cancel(false); }
        if (socketServer != null) { socketServer.cancel(false); }
        if (httpServerFuture != null) { httpServerFuture.cancel(false); }
    }
}
