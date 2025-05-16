package cloud.xcan.angus.core.tester.infra.agent.converter;


import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_IDLE;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_OTHER;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_SYS;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_TOTAL;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_USER;
import static cloud.xcan.angus.metrics.NodeMetricsName.CPU_WAIT;
import static cloud.xcan.angus.metrics.NodeMetricsName.DISK_READS_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.DISK_READ_BYTES_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.DISK_TOTAL;
import static cloud.xcan.angus.metrics.NodeMetricsName.DISK_WRITES_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.DISK_WRITE_BYTES_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.FS_AVAIL;
import static cloud.xcan.angus.metrics.NodeMetricsName.FS_FREE;
import static cloud.xcan.angus.metrics.NodeMetricsName.FS_USED;
import static cloud.xcan.angus.metrics.NodeMetricsName.FS_USE_PERCENT;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_CPU_NUM;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_CPU_PHYSICAL_NUM;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_FS_TOTAL;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_MEM_TOTAL;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_NET_SPEED;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_PLATFORM;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_SWAP_TOTAL;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_ULIMIT_MAX_OPEN_FILES;
import static cloud.xcan.angus.metrics.NodeMetricsName.HOST_ULIMIT_OPENED_FILES;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_ACTUAL_FREE;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_ACTUAL_FREE_PERCENT;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_ACTUAL_USED;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_ACTUAL_USED_PERCENT;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_FREE;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_FREE_PERCENT;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_SWAP_FREE;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_SWAP_PAGE_IN;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_SWAP_PAGE_OUT;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_SWAP_USED;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_USED;
import static cloud.xcan.angus.metrics.NodeMetricsName.MEM_USED_PERCENT;
import static cloud.xcan.angus.metrics.NodeMetricsName.NET_RX_BYTES;
import static cloud.xcan.angus.metrics.NodeMetricsName.NET_RX_BYTES_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.NET_TX_BYTES;
import static cloud.xcan.angus.metrics.NodeMetricsName.NET_TX_BYTES_RATE;
import static cloud.xcan.angus.metrics.NodeMetricsName.NET_TX_ERRORS;
import static cloud.xcan.angus.metrics.NodeMetricsName.PLATFORM_TAG_OS_ARCHE;
import static cloud.xcan.angus.metrics.NodeMetricsName.PLATFORM_TAG_OS_MACHINE;
import static cloud.xcan.angus.metrics.NodeMetricsName.PLATFORM_TAG_OS_NAME;
import static cloud.xcan.angus.metrics.NodeMetricsName.PLATFORM_TAG_OS_VENDOR;
import static cloud.xcan.angus.metrics.NodeMetricsName.PLATFORM_TAG_OS_VERSION;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.domain.node.info.Info;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.node.info.Os;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Cpu;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.FileSystem;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Memory;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage.Disk;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage.Network;
import cloud.xcan.angus.metrics.AvailableTag;
import cloud.xcan.angus.metrics.Response;
import cloud.xcan.angus.metrics.Sample;
import cloud.xcan.angus.spec.PlatformEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeMetricsConverter {

  public static NodeInfo toNodeInfo(List<Response> content, Long tenantId, Long deviceId,
      NodeInfo nodeDb) {
    if (nodeDb == null) {
      nodeDb = new NodeInfo();
    }
    nodeDb.setTenantId(tenantId);
    nodeDb.setId(deviceId).setOs(new Os())
        .setAgentInstalled(true).setAgentOnline(true)
        .setLastModifiedDate(LocalDateTime.now());
    Info info = new Info();
    Os os = new Os();
    Map<String, Response> contentMap = content.stream()
        .collect(Collectors.toMap(Response::getName, x -> x));
    Response response = contentMap.get(HOST_PLATFORM);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      Sample sample = response.getSamples().get(0);
      if (nonNull(sample)) {
        info.setPlatform(PlatformEnum.getValue(sample.getValue().intValue()).getValue());
      }
      toNodeOs(os, response);
    }
    response = contentMap.get(HOST_CPU_NUM);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setCpuNum(response.getSamples().get(0).getValue().intValue());
    }
    response = contentMap.get(HOST_CPU_PHYSICAL_NUM);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setCpuPhysicalNum(response.getSamples().get(0).getValue().intValue());
    }
    response = contentMap.get(HOST_MEM_TOTAL);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setMemTotal(response.getSamples().get(0).getValue().longValue());
    }
    response = contentMap.get(HOST_SWAP_TOTAL);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setSwapTotal(response.getSamples().get(0).getValue().longValue());
    }
    response = contentMap.get(HOST_FS_TOTAL);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setFsTotal(response.getSamples().get(0).getValue().longValue());
    }
    response = contentMap.get(HOST_NET_SPEED);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setNetworkSpeed(String.valueOf(response.getSamples().get(0).getValue()));
    }
    response = contentMap.get(HOST_ULIMIT_MAX_OPEN_FILES);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setMaxOpenFiles(response.getSamples().get(0).getValue().longValue());
    }
    response = contentMap.get(HOST_ULIMIT_OPENED_FILES);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setOpenedFiles(response.getSamples().get(0).getValue().longValue());
    }
    /*response = contentMap.get(HOST_ULIMIT_STACK_SIZE);
    if (nonNull(response) && isNotEmpty(response.getSamples())) {
      info.setStackSize(response.getSamples().get(0).getValue().longValue());
    }*/
    return nodeDb.setInfo(info).setOs(os);
  }

  public static NodeUsage toNodeUsage(List<Response> content, Long tenantId, Long deviceId,
      Long now) {
    NodeUsage usage = new NodeUsage();
    usage.setTenantId(tenantId);
    usage.setId(getCachedUidGenerator().getUID()).setNodeId(deviceId).setTimestamp(now);

    Cpu cpu = new Cpu();
    Memory memory = new Memory();
    FileSystem fs = new FileSystem();
    for (Response response : content) {
      if (isEmpty(response.getSamples()) || isEmpty(response.getSamples().get(0))) {
        continue;
      }
      Sample sample = response.getSamples().get(0);
      // Set cpu
      if (CPU_IDLE.equals(response.getName())) {
        cpu.setIdle(sample.getValue());
      } else if (CPU_SYS.equals(response.getName())) {
        cpu.setSys(sample.getValue());
      } else if (CPU_USER.equals(response.getName())) {
        cpu.setUser(sample.getValue());
      } else if (CPU_WAIT.equals(response.getName())) {
        cpu.setWait_(sample.getValue());
      } else if (CPU_OTHER.equals(response.getName())) {
        cpu.setOther(sample.getValue());
      } else if (CPU_TOTAL.equals(response.getName())) {
        cpu.setTotal(sample.getValue());
      }
      // Set memory
      if (MEM_FREE.equals(response.getName())) {
        memory.setFree(sample.getValue().longValue());
      } else if (MEM_USED.equals(response.getName())) {
        memory.setUsed(sample.getValue().longValue());
      } else if (MEM_FREE_PERCENT.equals(response.getName())) {
        memory.setFreePercent(sample.getValue());
      } else if (MEM_USED_PERCENT.equals(response.getName())) {
        memory.setUsedPercent(sample.getValue());
      } else if (MEM_ACTUAL_FREE.equals(response.getName())) {
        memory.setActualFree(sample.getValue());
      } else if (MEM_ACTUAL_USED.equals(response.getName())) {
        memory.setActualUsed(sample.getValue());
      } else if (MEM_ACTUAL_FREE_PERCENT.equals(response.getName())) {
        memory.setActualFreePercent(sample.getValue());
      } else if (MEM_ACTUAL_USED_PERCENT.equals(response.getName())) {
        memory.setActualUsedPercent(sample.getValue());
      } else if (MEM_SWAP_FREE.equals(response.getName())) {
        memory.setSwapFree(sample.getValue().longValue());
      } else if (MEM_SWAP_USED.equals(response.getName())) {
        memory.setSwapUsed(sample.getValue().longValue());
      } else if (MEM_SWAP_PAGE_IN.equals(response.getName())) {
        // memory.swapPageIn(sample.getValue());
        // NOOP
      } else if (MEM_SWAP_PAGE_OUT.equals(response.getName())) {
        // memory.swapPageout(sample.getValue());
        // NOOP
      }

      // Set default root FileSystem
      if (FS_FREE.equals(response.getName())) {
        fs.setFree(sample.getValue().longValue());
      } else if (FS_USED.equals(response.getName())) {
        fs.setUsed(sample.getValue().longValue());
      } else if (FS_AVAIL.equals(response.getName())) {
        fs.setAvail(sample.getValue().longValue());
      } else if (FS_USE_PERCENT.equals(response.getName())) {
        fs.setUsedPercent(sample.getValue());
      }
    }
    return usage.setCpu(cpu).setMemory(memory).setFilesystem(fs);
  }

  public static List<DiskUsage> toDiskUsages(List<Response> content, Long tenantId,
      Long deviceId, Long now) {
    Map<String, List<Response>> groupByDevice = groupByName(content.stream().filter(
            response -> response.getTags().stream().anyMatch(t -> "deviceName".equals(t.getTag()))
                && response.getTags().stream().anyMatch(t -> "fileSystem".equals(t.getTag())))
        .collect(Collectors.toList()));
    List<DiskUsage> diskUsages = new ArrayList<>(groupByDevice.size());
    for (Map.Entry<String, List<Response>> entry : groupByDevice.entrySet()) {
      DiskUsage usage = new DiskUsage();
      usage.setTenantId(tenantId);
      usage.setId(getCachedUidGenerator().getUID()).setNodeId(deviceId).setTimestamp(now);
      usage.setDeviceName(entry.getKey());
      Disk disk = new Disk();
      for (Response response : entry.getValue()) {
        if (isEmpty(response.getSamples()) || isEmpty(response.getSamples().get(0))) {
          continue;
        }
        Sample sample = response.getSamples().get(0);
        if (DISK_TOTAL.equals(response.getName())) {
          disk.setTotal(sample.getValue().longValue());
        } else if (DISK_READS_RATE.equals(response.getName())) {
          disk.setReadsRate(sample.getValue());
        } else if (DISK_WRITES_RATE.equals(response.getName())) {
          disk.setWritesRate(sample.getValue());
        } else if (DISK_READ_BYTES_RATE.equals(response.getName())) {
          disk.setReadBytesRate(nullSafe(sample.getValue(), 0D) / 1024 / 1024); // MB/s
        } else if (DISK_WRITE_BYTES_RATE.equals(response.getName())) {
          disk.setWriteBytesRate(nullSafe(sample.getValue(), 0D) / 1024 / 1024); // MB/s
        }
      }
      usage.setDisk(disk);
      diskUsages.add(usage);
    }
    return diskUsages;
  }

  public static List<NetUsage> toNetUsages(List<Response> content, Long tenantId,
      Long deviceId, Long now) {
    Map<String, List<Response>> groupByDevice = groupByName(content.stream().filter(
            response -> response.getTags().stream().anyMatch(t -> "deviceName".equals(t.getTag()))
                && response.getTags().stream().anyMatch(t -> "network".equals(t.getTag())))
        .collect(Collectors.toList()));
    List<NetUsage> netUsages = new ArrayList<>(groupByDevice.size());
    for (Map.Entry<String, List<Response>> entry : groupByDevice.entrySet()) {
      NetUsage usage = new NetUsage();
      usage.setTenantId(tenantId);
      usage.setId(getCachedUidGenerator().getUID())
          .setNodeId(deviceId).setTimestamp(now);
      usage.setDeviceName(entry.getKey());
      Network network = new Network();
      for (Response response : entry.getValue()) {
        if (isEmpty(response.getSamples()) || isEmpty(response.getSamples().get(0))) {
          continue;
        }
        Sample sample = response.getSamples().get(0);
        if (NET_RX_BYTES.equals(response.getName())) {
          network.setRxBytes(sample.getValue().longValue());
        } else if (NET_RX_BYTES_RATE.equals(response.getName())) {
          network.setRxBytesRate(sample.getValue());
        } else if (NET_TX_BYTES.equals(response.getName())) {
          network.setTxBytes(sample.getValue().longValue());
        } else if (NET_TX_BYTES_RATE.equals(response.getName())) {
          network.setTxBytesRate(sample.getValue());
        } else if (NET_TX_ERRORS.equals(response.getName())) {
          network.setRxErrors(sample.getValue().longValue());
        }
      }
      usage.setNetwork(network);
      netUsages.add(usage);
    }
    return netUsages;
  }

  private static Map<String, List<Response>> groupByName(List<Response> requestBody) {
    Map<String, List<Response>> group = new HashMap<>();
    for (Response response : requestBody) {
      List<AvailableTag> tags = response.getTags();
      for (AvailableTag tag : tags) {
        if ("deviceName".equals(tag.getTag())) {
          String deviceName = tag.getValues().stream().findFirst().get();
          if (group.containsKey(deviceName)) {
            group.get(deviceName).add(response);
          } else {
            group.put(deviceName, new ArrayList<>());
            group.get(deviceName).add(response);
          }
        }
      }
    }
    return group;
  }

  private static void toNodeOs(Os os, Response response) {
    for (AvailableTag tag : response.getTags()) {
      if (PLATFORM_TAG_OS_NAME.equals(tag.getTag())) {
        os.setName(tag.getValues().stream().findFirst().orElse(""));
      } else if (PLATFORM_TAG_OS_VERSION.equals(tag.getTag())) {
        os.setVersion(tag.getValues().stream().findFirst().orElse(""));
      } else if (PLATFORM_TAG_OS_ARCHE.equals(tag.getTag())) {
        os.setArch(tag.getValues().stream().findFirst().orElse(""));
      } else if (PLATFORM_TAG_OS_MACHINE.equals(tag.getTag())) {
        os.setMachine(tag.getValues().stream().findFirst().orElse(""));
      } else if (PLATFORM_TAG_OS_VENDOR.equals(tag.getTag())) {
        os.setVendor(tag.getValues().stream().findFirst().orElse(""));
      }
    }
  }

}
