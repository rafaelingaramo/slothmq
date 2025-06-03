package org.slothmq.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.server.jmx.JmxMetricsCollector;
import org.slothmq.server.websocket.LogWebSocketHandler;
import org.slothmq.server.websocket.MetricsWebSocketHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SlothNettyWebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothNettyWebSocketServer.class);
    private static final int PORT = 8081;

    public void start() throws InterruptedException {
        LOG.info("starting WebSocket channel on port 8081");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast(new LogWebSocketHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/metrics"));
                            pipeline.addLast(new MetricsWebSocketHandler());
                        }
                    });

            Channel ch = b. bind(PORT).sync().channel();
            LOG.info("WebSocket server started at ws://localhost:" + PORT + "/ws");
            this.startMetricsThread();
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void startMetricsThread() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(JmxMetricsCollector::collectAndPush, 0, 15, TimeUnit.SECONDS);
    }
}
