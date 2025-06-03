package org.slothmq.server.websocket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsWebSocketBroadcaster {
    private static final Set<Channel> channels = ConcurrentHashMap.newKeySet();

    public static void register(Channel channel) {
        channels.add(channel);
    }

    public static void unregister(Channel channel) {
        channels.remove(channel);
    }

    public static void broadcast(String message) {
        for (Channel channel : channels) {
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }
        }
    }
}
