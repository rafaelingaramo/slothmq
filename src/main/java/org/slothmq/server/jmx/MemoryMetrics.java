package org.slothmq.server.jmx;

public record MemoryMetrics(
        Long init,
        Long max,
        Long used
) {
}
