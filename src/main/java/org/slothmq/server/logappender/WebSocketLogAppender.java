package org.slothmq.server.logappender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slothmq.server.websocket.LogWebSocketBroadcaster;

public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        LogWebSocketBroadcaster.broadcast(iLoggingEvent.getFormattedMessage());
    }
}
