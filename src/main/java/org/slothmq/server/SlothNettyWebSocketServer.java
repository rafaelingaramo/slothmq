package org.slothmq.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlothNettyWebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(SlothNettyWebSocketServer.class);


    public void start(int port, String path, SimpleChannelInboundHandler<?> handler) throws InterruptedException {
        LOG.info("starting WebSocket channel on port {}", port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //https://github.com/netty/netty/blob/4.2/example/src/main/java/io/netty/example/http/websocketx/server/WebSocketServer.java
            //https://github.com/netty/netty/blob/4.2/example/src/main/java/io/netty/example/http/websocketx/server/WebSocketIndexPageHandler.java
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
                            pipeline.addLast(new WebSocketServerProtocolHandler(path));
                            pipeline.addLast(handler);
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            LOG.info("WebSocket server started at ws://localhost:{}{}", port, path);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
