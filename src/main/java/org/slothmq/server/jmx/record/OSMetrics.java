package org.slothmq.server.jmx.record;

public record OSMetrics(
    String name,
    Long commitedVirtualMemorySize,
    Long freeMemorySize,
    Double cpuLoad,
    Long freeSwapSpaceSize,
    Long totalMemorySize,
    Long totalSwapSpaceSize,
    Long processCpuTime) {
}
