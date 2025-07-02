package org.slothmq.server.jmx.record;

public record ThreadMetrics(
    Integer threadCount,
    Integer daemonThreadCount,
    Integer peakThreadCount,
    Long totalStartedThreadCount) {
}
