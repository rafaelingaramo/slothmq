package org.slothmq.server.jmx;

import org.junit.jupiter.api.Test;
import org.slothmq.server.websocket.MetricsWebSocketBroadcaster;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class JmxMetricsCollectorTest {
    @Test
    void givenOnNormalSystemSituationVerifyTheOutput() {
        //given
        try (var broadcaster = mockStatic(MetricsWebSocketBroadcaster.class)) {
            final AtomicReference<String> capturedArg = new AtomicReference<>();
            //stubs
            broadcaster.when(() -> MetricsWebSocketBroadcaster.broadcast(any()))
                    .thenAnswer(inv -> {
                        capturedArg.set(inv.getArgument(0));
                        return null;
                    });
            //when
            JmxMetricsCollector.collectAndPush();

            //then
            assert capturedArg.get().contains("gcMetrics");
            assert capturedArg.get().contains("heapMetrics");
            assert capturedArg.get().contains("nonHeapMetrics");
            assert capturedArg.get().contains("threadMetrics");
            assert capturedArg.get().contains("osMetrics");
        }
    }
}
