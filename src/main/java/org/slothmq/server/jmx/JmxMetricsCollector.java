package org.slothmq.server.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.*;
import java.util.List;

public class JmxMetricsCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JmxMetricsCollector.class);

    public static void collectAndPush() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        garbageCollectorMXBeans.forEach(gcMXBean -> {
            String name = gcMXBean.getName();
            long collectionCount = gcMXBean.getCollectionCount();
            long collectionTime = gcMXBean.getCollectionTime();
            String[] memoryPoolNames = gcMXBean.getMemoryPoolNames();
            LOG.info("GC: {} collection count: {}, collection time: {}, memoryPoolNames: {}",
                    name, collectionCount, collectionTime, memoryPoolNames);
        });

        LOG.info("JMX memory collector triggered");
        LOG.info("heap- commited: {}, init: {}, max: {}, used: {}", heapMemoryUsage.getCommitted(),
                heapMemoryUsage.getInit(), heapMemoryUsage.getMax(), heapMemoryUsage.getUsed());
        LOG.info("non heap- commited: {}, init: {}, max: {}, used: {}", nonHeapMemoryUsage.getCommitted(),
                nonHeapMemoryUsage.getInit(), nonHeapMemoryUsage.getMax(), nonHeapMemoryUsage.getUsed());


        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        LOG.info("Threads: {}, {}, {}", threadMXBean.getDaemonThreadCount(), threadMXBean.getPeakThreadCount(),
                threadMXBean.getTotalStartedThreadCount());

        com.sun.management.OperatingSystemMXBean operatingSystemMXBean
                = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        LOG.info("OS Information: {}, {}, {}, {}, {}, {}, {}, {}", operatingSystemMXBean.getName(),
                operatingSystemMXBean.getCommittedVirtualMemorySize(), operatingSystemMXBean.getFreeMemorySize(),
                operatingSystemMXBean.getCpuLoad(), operatingSystemMXBean.getFreeSwapSpaceSize(), operatingSystemMXBean.getTotalMemorySize(),
                operatingSystemMXBean.getTotalSwapSpaceSize(), operatingSystemMXBean.getProcessCpuTime());
    }
}
