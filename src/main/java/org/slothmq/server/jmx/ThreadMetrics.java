package org.slothmq.server.jmx;

public record ThreadMetrics(
    Integer threadCount,
    Integer daemonThreadCount,
    Integer peakThreadCount,
    Long totalStartedThreadCount) {
}
