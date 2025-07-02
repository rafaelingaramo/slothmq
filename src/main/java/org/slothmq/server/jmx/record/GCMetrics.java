package org.slothmq.server.jmx.record;

public record GCMetrics(
        String name,
        Long collectionCount,
        Long collectionTime) {
}
