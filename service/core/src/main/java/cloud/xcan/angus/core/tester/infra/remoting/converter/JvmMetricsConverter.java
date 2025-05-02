package cloud.xcan.angus.core.tester.infra.remoting.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_BUFFER_COUNT;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_BUFFER_MEMORY_USED;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_BUFFER_TOTAL_CAPACITY;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_MEMORY_COMMITTED;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_MEMORY_MAX;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.JVM_MEMORY_USED;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.PROCESSOR_CPU_USAGE;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.SYSTEM_CPU_COUNT;
import static cloud.xcan.angus.metrics.JvmServiceMetricsName.SYSTEM_CPU_USAGE;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage.JvmMemory;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage.JvmProcessor;
import cloud.xcan.angus.metrics.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JvmMetricsConverter {

  public static JvmServiceUsage toJvmMemoryUsage(List<Response> content, Long tenantId,
      Long deviceId, Long serviceId, String source, Long now) {
    JvmServiceUsage usage = new JvmServiceUsage();
    usage.setTenantId(tenantId);
    usage.setId(getCachedUidGenerator().getUID())
        .setTimestamp(now).setNodeId(deviceId)
        .setServiceId(serviceId).setSource(source);
    // Note: The space in different regions of the jvm has been merged by the runner.
    JvmMemory jvmMemory = new JvmMemory();
    JvmProcessor jvmProcessor = new JvmProcessor();

    // Note: Merge different regions of the jvm space.
    Map<String, List<Response>> jvmMetricsMap = content.stream()
        .collect(Collectors.groupingBy(Response::getName));
    jvmMemory.setBufferCount(jvmMetricsMap.get(JVM_BUFFER_COUNT)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());
    jvmMemory.setBufferMemoryUsed(jvmMetricsMap.get(JVM_BUFFER_MEMORY_USED)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());
    jvmMemory.setBufferTotalCapacity(jvmMetricsMap.get(JVM_BUFFER_TOTAL_CAPACITY)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());
    jvmMemory.setMemoryUsed(jvmMetricsMap.get(JVM_MEMORY_USED)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());
    jvmMemory.setMemoryCommitted(jvmMetricsMap.get(JVM_MEMORY_COMMITTED)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());
    jvmMemory.setMemoryMax(jvmMetricsMap.get(JVM_MEMORY_MAX)
        .stream().filter(x -> nonNull(x.getSamples()) && nonNull(x.getSamples().get(0).getValue()))
        .mapToDouble(x -> x.getSamples().get(0).getValue()).sum());

    jvmProcessor.setCpuCount(jvmMetricsMap.get(SYSTEM_CPU_COUNT)
        .get(0).getSamples().get(0).getValue().intValue());
    jvmProcessor.setSysUsage(jvmMetricsMap.get(SYSTEM_CPU_USAGE)
        .get(0).getSamples().get(0).getValue() * 100 /*100%*/);
    jvmProcessor.setProcessUsage(jvmMetricsMap.get(PROCESSOR_CPU_USAGE)
        .get(0).getSamples().get(0).getValue() * 100 /*100%*/);

    usage.setJvm(jvmMemory).setProcessor(jvmProcessor);
    return usage;
  }

}
