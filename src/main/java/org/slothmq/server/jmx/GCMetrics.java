package org.slothmq.server.jmx;

public record GCMetrics(
        String name,
        Long collectionCount,
        Long collectionTime) {
}
