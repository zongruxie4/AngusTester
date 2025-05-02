package cloud.xcan.angus.core.tester.infra.remoting.hanlder;

import static cloud.xcan.angus.agent.AgentConstant.PUSH_AGENT_SERVICE_METRICS;
import static cloud.xcan.angus.core.spring.boot.ApplicationInfo.APP_READY;
import static cloud.xcan.angus.core.tester.infra.remoting.converter.JvmMetricsConverter.toJvmMemoryUsage;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.NO_CLEAR_RESERVED_DAY;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.clearAt3Hour;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.ExecMetricsMessageHandler.getReservedDay;
import static cloud.xcan.angus.core.tester.infra.remoting.hanlder.HostMetricsMessageHandler.checkAndParse;
import static cloud.xcan.angus.mockservice.api.MockServiceConstant.METRICS_SERVICE_ID_TAG_KEY;
import static cloud.xcan.angus.mockservice.api.MockServiceConstant.METRICS_SERVICE_TYPE_TAG_KEY;
import static cloud.xcan.angus.mockservice.api.RemotingCommandType.PUSH_MOCK_SERVICE_METRICS;
import static cloud.xcan.angus.runner.RunnerConstant.PUSH_RUNNER_SERVICE_METRICS;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import cloud.xcan.angus.lettucex.distlock.RedisLock;
import cloud.xcan.angus.metrics.Response;
import cloud.xcan.angus.remoting.common.message.BusinessMessage;
import cloud.xcan.angus.remoting.common.message.MessageBusinessType;
import cloud.xcan.angus.remoting.common.spi.CustomMessageHandler;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockServiceMetricsMessageHandler implements CustomMessageHandler {

  private JvmServiceUsageRepo jvmServiceUsageRepo;

  private static final int CLEAR_AT_HOUR = 3;

  private static final String LOCK_KEY = "tester:job:MockServiceMetricsClearJob";

  public MockServiceMetricsMessageHandler() {
  }

  @Override
  public MessageBusinessType supportType() {
    return MessageBusinessType.SERVICE_METRIC;
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
        log.error(
            "Missing metrics for pushing mock service sample, " + "tenantId: {}, deviceId: {}",
            tenantId, deviceId);
        return null;
      }

      switch (customType) {
        case PUSH_MOCK_SERVICE_METRICS:
        case PUSH_AGENT_SERVICE_METRICS:
        case PUSH_RUNNER_SERVICE_METRICS: {
          if (isEmpty(message.getExtras()) && !message.getExtras().containsKey("tags")) {
            log.error("Missing client tags for pushing mock service sample, "
                + "tenantId: {}, deviceId: {}", tenantId, deviceId);
            break;
          }
          String serviceIdTag = ((Collection<String>) message.getExtras().get("tags")).stream()
              .filter(x -> x.startsWith(METRICS_SERVICE_ID_TAG_KEY)).findFirst()
              .orElse(null);
          String sourceTag = ((Collection<String>) message.getExtras().get("tags")).stream()
              .filter(x -> x.startsWith(METRICS_SERVICE_TYPE_TAG_KEY)).findFirst()
              .orElse(null);
          if (isEmpty(serviceIdTag) || isEmpty(sourceTag)) {
            log.error("Missing client tags for pushing mock service sample, "
                + "serviceIdTag: {}, sourceTag: {}", serviceIdTag, sourceTag);
            break;
          }

          JvmServiceUsageRepo jvmServiceUsageRepo = getJvmServiceUsageRepo();
          jvmServiceUsageRepo.save(toJvmMemoryUsage(responses, tenantId, deviceId,
              Long.parseLong(serviceIdTag.split(":")[1]), sourceTag.split(":")[1], now));
          break;
        }
        default: {
          // NOOP
        }
      }

      // Clear old data at 3 o'clock every day
      tryClearOldData(tenantId);
    } finally {
      PrincipalContext.remove();
    }
    return null;
  }

  private void tryClearOldData(Long tenantId) {
    if (!clearAt3Hour(CLEAR_AT_HOUR)) {
      return;
    }
    RedisLock redisLock = SpringContextHolder.getBean(RedisLock.class);
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, tenantId.toString(),
          /* 2 hours: Prevent duplicate cleaning. */2, TimeUnit.HOURS);
      if (!tryLock) {
        log.debug("MockServiceMetricsClearJob try lock fail!, tenantId: {}", tenantId);
        return;
      }
      long reservedDay = getReservedDay();
      if (NO_CLEAR_RESERVED_DAY.equals(reservedDay)) {
        return;
      }
      long reservedTime = System.currentTimeMillis() - reservedDay * 24 * 60 * 60 * 1000;
      jvmServiceUsageRepo.deleteBeforeDay(reservedTime);
      log.info("MockServiceMetricsClearJob execute successfully, tenantId: {}", tenantId);
    } catch (Exception e) {
      log.error("MockServiceMetricsClearJob execute fail, tenantId:{}, cause:{}", tenantId,
          e.getMessage());
    } finally {
      // Note: Release the lock through timeout to prevent duplicate cleaning.
      // redisLock.releaseLock(LOCK_KEY, tenantId.toString());
    }
  }

  private JvmServiceUsageRepo getJvmServiceUsageRepo() {
    if (jvmServiceUsageRepo == null) {
      jvmServiceUsageRepo = SpringContextHolder.getBean(JvmServiceUsageRepo.class);
    }
    return jvmServiceUsageRepo;
  }
}
