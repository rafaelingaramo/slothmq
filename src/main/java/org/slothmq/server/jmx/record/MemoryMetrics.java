package org.slothmq.server.jmx.record;

public record MemoryMetrics(
        Long init,
        Long max,
        Long used
) {
}
