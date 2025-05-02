package cloud.xcan.angus.core.tester.infra.remoting.hanlder;

import static cloud.xcan.angus.agent.AgentConstant.PUSH_HOST_INFO_METRICS;
import static cloud.xcan.angus.agent.AgentConstant.PUSH_HOST_USAGE_METRICS;
import static cloud.xcan.angus.core.spring.boot.ApplicationInfo.APP_READY;
import static cloud.xcan.angus.core.tester.infra.remoting.converter.NodeMetricsConverter.toDiskUsages;
import static cloud.xcan.angus.core.tester.infra.remoting.converter.NodeMetricsConverter.toNetUsages;
import static cloud.xcan.angus.core.tester.infra.remoting.converter.NodeMetricsConverter.toNodeInfo;
import static cloud.xcan.angus.core.tester.infra.remoting.converter.NodeMetricsConverter.toNodeUsage;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.NO_CLEAR_RESERVED_DAY;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.clearAt3Hour;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.getReservedDay;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfoRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsageRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsageRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsageRepo;
import cloud.xcan.angus.lettucex.distlock.RedisLock;
import cloud.xcan.angus.metrics.Response;
import cloud.xcan.angus.remoting.common.MessageService;
import cloud.xcan.angus.remoting.common.message.BusinessMessage;
import cloud.xcan.angus.remoting.common.message.MessageBusinessType;
import cloud.xcan.angus.remoting.common.spi.CustomMessageHandler;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostMetricsMessageHandler implements CustomMessageHandler {

  private NodeInfoRepo nodeInfoRepo;
  private NodeUsageRepo nodeUsageRepo;
  private DiskUsageRepo diskUsageRepo;
  private NetUsageRepo netUsageRepo;

  private static final int CLEAR_AT_HOUR = 2;

  private static final String LOCK_KEY = "tester:job:HostMetricsClearJob";

  public HostMetricsMessageHandler() {
  }

  @Override
  public MessageBusinessType supportType() {
    return MessageBusinessType.HOST_METRIC;
  }

  @Override
  public String handle(BusinessMessage message) {
    if (!APP_READY) {
      return null;
    }

    List<Response> responses = checkAndParse(message, new TypeReference<List<Response>>() {
    });

    try {
      String customType = message.getCustomType();
      Principal principal = PrincipalContext.create();
      Long tenantId = Long.parseLong(message.getTenantId());
      Long deviceId = Long.parseLong(message.getDeviceId());
      principal.setTenantId(tenantId);
      Long now = System.currentTimeMillis();

      if (isNull(responses)) {
        log.error("Missing metrics for pushing host sample, " + "tenantId: {}, deviceId: {}",
            tenantId, deviceId);
        return null;
      }

      switch (customType) {
        case PUSH_HOST_INFO_METRICS: {
          NodeInfoRepo nodeInfoRepo = getNodeInfoRepo();
          // Insert if not exist, update if exist
          NodeInfo nodeDb = nodeInfoRepo.findById(deviceId).orElse(null);
          nodeInfoRepo.save(toNodeInfo(responses, tenantId, deviceId, nodeDb));
          break;
        }
        case PUSH_HOST_USAGE_METRICS: {
          NodeUsageRepo nodeUsageRepo = getNodeUsageRepo();
          nodeUsageRepo.save(toNodeUsage(responses, tenantId, deviceId, now));

          DiskUsageRepo diskUsageRepo = getDiskUsageRepo();
          List<DiskUsage> diskUsages = toDiskUsages(responses, tenantId, deviceId, now);
          if (isNotEmpty(diskUsages)) {
            diskUsageRepo.batchInsert0(diskUsages);
          }

          NetUsageRepo netUsageRepo = getNetUsageRepo();
          List<NetUsage> netUsages = toNetUsages(responses, tenantId, deviceId, now);
          if (isNotEmpty(netUsages)) {
            netUsageRepo.batchInsert0(netUsages);
          }
          break;
        }
        default: {
          // NOOP
        }
      }
      // Clear old data at 2 o'clock every day
      tryClearOldData(tenantId);
    } finally {
      PrincipalContext.remove();
    }
    return null;
  }

  public static <T> List<T> checkAndParse(BusinessMessage message, TypeReference<List<T>> type) {
    String customType = message.getCustomType();
    MessageService sendService = message.getSendService();
    if (isEmpty(customType) ||  isNull(sendService)) {
      log.error("Metrics message parameter error, customType: {}, service: {}",
          customType, sendService);
      return null;
    }

    try {
      return JsonUtils.fromJsonObject(message.getContent(), type);
    } catch (Exception e) {
      log.error("Parse json message parameter error, cause: {}", e.getMessage());
      return null;
    }
  }

  private void tryClearOldData(Long tenantId) {
    if (!clearAt3Hour(CLEAR_AT_HOUR)) {
      return;
    }
    RedisLock redisLock = SpringContextHolder.getBean(RedisLock.class);
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, tenantId.toString(),
          /* 2 hours: Prevent duplicate cleaning. */
          2, TimeUnit.HOURS);
      if (!tryLock) {
        log.debug("HostMetricsClearJob try lock fail!, tenantId: {}", tenantId);
        return;
      }
      long reservedDay = getReservedDay();
      if (NO_CLEAR_RESERVED_DAY.equals(reservedDay)) {
        return;
      }
      long reservedTime = System.currentTimeMillis() - reservedDay * 24 * 60 * 60 * 1000;
      nodeUsageRepo.deleteBeforeDay(reservedTime);
      diskUsageRepo.deleteBeforeDay(reservedTime);
      netUsageRepo.deleteBeforeDay(reservedTime);
      log.info("HostMetricsClearJob execute successfully, tenantId: {}", tenantId);
    } catch (Exception e) {
      log.error("HostMetricsClearJob execute fail, tenantId:{}, cause:{}", tenantId,
          e.getMessage());
    } finally {
      // Note: Release the lock through timeout to prevent duplicate cleaning.
      // redisLock.releaseLock(LOCK_KEY, tenantId.toString());
    }
  }

  private NodeInfoRepo getNodeInfoRepo() {
    if (nodeInfoRepo == null) {
      nodeInfoRepo = SpringContextHolder.getBean(NodeInfoRepo.class);
    }
    return nodeInfoRepo;
  }

  private NodeUsageRepo getNodeUsageRepo() {
    if (nodeUsageRepo == null) {
      nodeUsageRepo = SpringContextHolder.getBean(NodeUsageRepo.class);
    }
    return nodeUsageRepo;
  }

  private DiskUsageRepo getDiskUsageRepo() {
    if (diskUsageRepo == null) {
      diskUsageRepo = SpringContextHolder.getBean(DiskUsageRepo.class);
    }
    return diskUsageRepo;
  }

  private NetUsageRepo getNetUsageRepo() {
    if (netUsageRepo == null) {
      netUsageRepo = SpringContextHolder.getBean(NetUsageRepo.class);
    }
    return netUsageRepo;
  }

}
