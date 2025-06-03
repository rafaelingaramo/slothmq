package org.slothmq.server.jmx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slothmq.server.websocket.MetricsWebSocketBroadcaster;

import java.lang.management.*;
import java.util.List;
import java.util.Optional;

public class JmxMetricsCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JmxMetricsCollector.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void collectAndPush() {
        LOG.info("JMX memory collector triggered");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean
                = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        List<GCMetrics> gcMetrics = garbageCollectorMXBeans.stream().map(garbageCollectorMXBean ->
                new GCMetrics(garbageCollectorMXBean.getName(), garbageCollectorMXBean.getCollectionCount(), garbageCollectorMXBean.getCollectionTime())
        ).toList();

        MemoryMetrics heapMetrics = Optional.of(heapMemoryUsage)
                .map(memoryUsage -> new MemoryMetrics(memoryUsage.getInit(), memoryUsage.getMax(), memoryUsage.getUsed()))
                .orElseThrow();

        MemoryMetrics nonHeapMetrics = Optional.of(nonHeapMemoryUsage)
                .map(memoryUsage -> new MemoryMetrics(memoryUsage.getInit(), memoryUsage.getMax(), memoryUsage.getUsed()))
                .orElseThrow();

        ThreadMetrics threadMetrics = Optional.of(threadMXBean)
                .map(threadMx -> new ThreadMetrics(threadMx.getThreadCount(),
                        threadMx.getDaemonThreadCount(),
                        threadMx.getPeakThreadCount(),
                        threadMx.getTotalStartedThreadCount()))
                .orElseThrow();

        OSMetrics osMetrics = Optional.of(operatingSystemMXBean)
                .map(osMx -> new OSMetrics(osMx.getName(),
                        osMx.getCommittedVirtualMemorySize(),
                        osMx.getFreeMemorySize(),
                        osMx.getCpuLoad(),
                        osMx.getFreeSwapSpaceSize(),
                        osMx.getTotalMemorySize(),
                        osMx.getTotalSwapSpaceSize(),
                        osMx.getProcessCpuTime()))
                .orElseThrow();

        JmxMetrics jmxMetrics = new JmxMetrics(gcMetrics, heapMetrics, nonHeapMetrics, threadMetrics, osMetrics);
        try {
            String jsonMetrics = MAPPER.writeValueAsString(jmxMetrics);
            MetricsWebSocketBroadcaster.broadcast(jsonMetrics);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        LOG.info("JMX memory collector finished");
    }
}
