package org.slothmq.server.jmx;

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
