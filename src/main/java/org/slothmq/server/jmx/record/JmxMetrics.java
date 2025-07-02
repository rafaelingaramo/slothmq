package org.slothmq.server.jmx.record;

import java.util.List;

public record JmxMetrics(
    List<GCMetrics> gcMetrics,
    MemoryMetrics heapMetrics,
    MemoryMetrics nonHeapMetrics,
    ThreadMetrics threadMetrics,
    OSMetrics osMetrics) {
}
