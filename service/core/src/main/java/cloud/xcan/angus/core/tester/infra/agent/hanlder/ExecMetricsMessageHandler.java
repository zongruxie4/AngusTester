package cloud.xcan.angus.core.tester.infra.agent.hanlder;

import static cloud.xcan.angus.core.spring.boot.ApplicationInfo.APP_READY;
import static cloud.xcan.angus.core.tester.infra.agent.converter.ExecMetricsConverter.toExecMetrics;
import static cloud.xcan.angus.core.tester.infra.agent.converter.ExecMetricsConverter.toExecSampleContent;
import static cloud.xcan.angus.core.tester.infra.agent.converter.ExecMetricsConverter.toExecSampleErrorCause;
import static cloud.xcan.angus.core.tester.infra.agent.hanlder.HostMetricsMessageHandler.checkAndParse;
import static cloud.xcan.angus.model.AngusConstant.SAMPLE_TOTAL_NAME;
import static cloud.xcan.angus.runner.RunnerConstant.METRICS_EXEC_ID_TAG_KEY;
import static cloud.xcan.angus.runner.RunnerConstant.PUSH_RUNNER_FINISH_TASK_METRICS;
import static cloud.xcan.angus.runner.RunnerConstant.PUSH_RUNNER_TASK_METRICS;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.DateUtils.asLocalDateTime;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.setting.Setting;
import cloud.xcan.angus.api.commonlink.setting.SettingKey;
import cloud.xcan.angus.api.manager.SettingManager;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugRepo;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNode;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContentRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCauseRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleRepo;
import cloud.xcan.angus.lettucex.distlock.RedisLock;
import cloud.xcan.angus.metrics.ExecMetrics;
import cloud.xcan.angus.model.meter.MeterStatus;
import cloud.xcan.angus.remoting.common.message.BusinessMessage;
import cloud.xcan.angus.remoting.common.message.MessageBusinessType;
import cloud.xcan.angus.remoting.common.spi.CustomMessageHandler;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecMetricsMessageHandler implements CustomMessageHandler {

  private ExecSampleRepo execSampleRepo;

  private ExecSampleErrorCauseRepo execSampleErrorsRepo;

  private ExecSampleContentRepo execSampleExtcRepo;

  private ExecRepo execRepo;

  private ExecNodeRepo execNodeRepo;

  private ExecDebugRepo execDebugRepo;

  private static final Long RESERVED_DAY = 15L;
  public static final Long NO_CLEAR_RESERVED_DAY = -1L;

  private static final int CLEAR_AT_HOUR = 1;

  private static final String LOCK_KEY = "tester:job:ExecMetricsClearJob";

  public ExecMetricsMessageHandler() {
  }

  @Override
  public MessageBusinessType supportType() {
    return MessageBusinessType.EXEC_METRICS;
  }

  @Override
  public String handle(BusinessMessage message) {
    if (!APP_READY) {
      return null;
    }

    List<ExecMetrics> metrics = checkAndParse(message, new TypeReference<List<ExecMetrics>>() {
    });

    try {
      String customType = message.getCustomType();
      Principal principal = PrincipalContext.create();
      Long tenantId = Long.parseLong(message.getTenantId());
      Long deviceId = Long.parseLong(message.getDeviceId());

      if (isNull(metrics)) {
        log.error("Missing metrics for pushing exec sample, " + "tenantId: {}, deviceId: {}",
            tenantId, deviceId);
        return null;
      }

      if (isEmpty(message.getExtras()) && !message.getExtras().containsKey("tags")) {
        log.error("Missing client tags for pushing exec sample, "
            + "tenantId: {}, deviceId: {}", tenantId, deviceId);
        return null;
      }
      String execIdTag = ((Collection<String>) message.getExtras().get("tags")).stream()
          .filter(x -> x.startsWith(METRICS_EXEC_ID_TAG_KEY)).findFirst().orElse(null);
      if (isEmpty(execIdTag)) {
        log.error("Missing client tags for pushing exec sample, execNoTag: {}", execIdTag);
        return null;
      }

      Long now = System.currentTimeMillis();
      Long execId = Long.parseLong(nullSafe(execIdTag.split(":")[1], "-1"));
      tenantId = judgeTrailTenantId(tenantId, execId);
      principal.setTenantId(tenantId);

      switch (customType) {
        case PUSH_RUNNER_TASK_METRICS: {
          saveExecMetrics(metrics, tenantId, deviceId, now, execId, false);
          break;
        }
        // Note: Multi node task execution will have multiple end messages.
        case PUSH_RUNNER_FINISH_TASK_METRICS: {
          saveExecMetrics(metrics, tenantId, deviceId, now, execId, true);

          updateFinishStatus(metrics, execId, deviceId, now);
          break;
        }
        default: {
          // NOOP
        }
      }
    } finally {
      PrincipalContext.remove();
    }
    return null;
  }

  private Long judgeTrailTenantId(Long tenantId, Long execId) {
    if (OWNER_TENANT_ID.equals(tenantId)) {
      Long actualTenantId = getExecRepo().findActualTenantIdByIdAndTrial(execId);
      if (nonNull(actualTenantId)) {
        tenantId = actualTenantId;
      } else {
        actualTenantId = getExecDebugRepo().findActualTenantIdByIdAndTrial(execId);
        if (nonNull(actualTenantId)) {
          tenantId = actualTenantId;
        }
      }
    }
    return tenantId;
  }

  private void updateFinishStatus(List<ExecMetrics> metrics, Long execId, Long deviceId, Long now) {
    ExecRepo execRepo = getExecRepo();
    if (execRepo.existsById(execId)) {
      ExecNodeRepo execNodeRepo = getExecNodeRepo();
      execNodeRepo.deleteByNodeIdIn(Collections.singleton(deviceId));

      // End when all nodes are released
      List<ExecNode> nodes = execNodeRepo.findByExecId(execId);
      if (isEmpty(nodes)) {
        ExecMetrics totalMetrics = metrics.stream()
            .filter(x -> SAMPLE_TOTAL_NAME.equals(x.getName()))
            .findFirst().orElse(metrics.get(0));
        // Fix:: the sampling result where sampling data is empty
        // Configuration stop task sampling when encountering errors, will appears when running multiple executions
        boolean isSamplingError = totalMetrics.getDuration() == 0
            && totalMetrics.getOperations() == 0;
        if (isNull(totalMetrics.getMeterStatus()) && isSamplingError) {
          execRepo.updateFinishById(execId, ExecStatus.FAILED.getValue(), asLocalDateTime(now),
              MeterStatus.UNKOWN.name(), "Exiting due to sampling error or data abnormality");
        } else {
          ExecStatus status = isNull(totalMetrics.getMeterStatus())
              || MeterStatus.valueOf(totalMetrics.getMeterStatus()).isSuccess()
              ? ExecStatus.COMPLETED : ExecStatus.FAILED;
          execRepo.updateFinishById(execId, status.getValue(), asLocalDateTime(now),
              totalMetrics.getMeterStatus(), totalMetrics.getMeterMessage());
        }
      }
    } else {
      // Debug execution
      ExecDebugRepo execDebugRepo = getExecDebugRepo();
      ExecMetrics totalMetrics = metrics.stream()
          .filter(x -> SAMPLE_TOTAL_NAME.equals(x.getName()))
          .findFirst().orElse(metrics.get(0));
      // Fix:: the sampling result where sampling data is empty
      // Configuration stop task sampling when encountering errors, will appears when running multiple executions
      boolean isSamplingError = totalMetrics.getDuration() == 0
          && totalMetrics.getOperations() == 0;
      if (isNull(totalMetrics.getMeterStatus()) && isSamplingError) {
        execDebugRepo.updateFinishById(execId, ExecStatus.FAILED.getValue(), asLocalDateTime(now),
            MeterStatus.UNKOWN.name(), "Exiting due to sampling error or data abnormality");
      } else {
        ExecStatus status = isNull(totalMetrics.getMeterStatus())
            || MeterStatus.valueOf(totalMetrics.getMeterStatus()).isSuccess()
            ? ExecStatus.COMPLETED : ExecStatus.FAILED;
        execDebugRepo.updateFinishById(execId, status.getValue(), asLocalDateTime(now),
            totalMetrics.getMeterStatus(), totalMetrics.getMeterMessage());
      }
    }
  }

  private void saveExecMetrics(List<ExecMetrics> metrics, Long tenantId, Long deviceId, Long now,
      Long execId, boolean finish) {
    // 1. Save main sampling sample
    ExecSampleRepo execMetricsRepo = getExecSampleRepo();
    execMetricsRepo.batchInsert0(toExecMetrics(metrics, tenantId, deviceId, execId, now, finish));

    // 2. Save sampling error cause
    boolean hasErrorCause = metrics.stream().anyMatch(x -> isNotEmpty(x.getErrorCause()));
    if (hasErrorCause) {
      ExecSampleErrorCauseRepo sampleErrorCauseRepo = getExecSampleErrorsRepo();
      List<ExecSampleErrorCause> errorCauses = toExecSampleErrorCause(metrics, tenantId, deviceId,
          execId, now, finish);
      sampleErrorCauseRepo.batchInsert0(errorCauses);
    }

    // 3. Save sampling content
    boolean hasContent = metrics.stream().anyMatch(x -> isNotEmpty(x.getSampleResultContent())
        || isNotEmpty(x.getExtContent1()) || isNotEmpty(x.getExtContent2()));
    if (hasContent) {
      ExecSampleContentRepo execSampleExtcRepo = getExecSampleExtcRepo();
      List<ExecSampleContent> extcs = toExecSampleContent(metrics, tenantId, deviceId,
          execId, now, finish);
      execSampleExtcRepo.batchInsert0(extcs);
    }

    // 4. Clear old data at 1 o'clock every day
    tryClearOldData(tenantId);
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
        log.debug("ExecMetricsClearJob try lock fail!, tenantId: {}", tenantId);
        return;
      }
      long reservedDay = getReservedDay();
      if (NO_CLEAR_RESERVED_DAY.equals(reservedDay)) {
        return;
      }
      execSampleRepo.deleteBeforeDay(reservedDay);
      execSampleErrorsRepo.deleteBeforeDay(reservedDay);
      execSampleExtcRepo.deleteBeforeDay(reservedDay);
      log.info("ExecMetricsClearJob execute successfully, tenantId: {}", tenantId);
    } catch (Exception e) {
      log.error("ExecMetricsClearJob execute fail, tenantId:{}, cause:{}", tenantId,
          e.getMessage());
    } finally {
      // Note: Release the lock through timeout to prevent duplicate cleaning.
      // redisLock.releaseLock(LOCK_KEY, tenantId.toString());
    }
  }

  public static long getReservedDay() {
    long reservedDay = RESERVED_DAY;
    try {
      SettingManager settingManager = SpringContextHolder.getBean(SettingManager.class);
      Setting setting = settingManager.setting(SettingKey.MAX_METRICS_DAYS);
      reservedDay =
          setting.getMaxMetricsDays() == null ? RESERVED_DAY : setting.getMaxMetricsDays();
    } catch (Exception e) {
      log.error("The maximum day of metrics is not configured, SettingKey: {}",
          SettingKey.MAX_METRICS_DAYS.getValue());
    }
    return reservedDay <= 0 ? NO_CLEAR_RESERVED_DAY : reservedDay;
  }

  public static boolean clearAt3Hour(int clearAtHour) {
    LocalTime now = LocalTime.now();
    int hour = LocalTime.now().getHour();
    return hour == clearAtHour && now.getMinute() <= 15;
  }

  private ExecSampleRepo getExecSampleRepo() {
    if (execSampleRepo == null) {
      execSampleRepo = SpringContextHolder.getBean(ExecSampleRepo.class);
    }
    return execSampleRepo;
  }

  private ExecSampleErrorCauseRepo getExecSampleErrorsRepo() {
    if (execSampleErrorsRepo == null) {
      execSampleErrorsRepo = SpringContextHolder.getBean(ExecSampleErrorCauseRepo.class);
    }
    return execSampleErrorsRepo;
  }

  private ExecSampleContentRepo getExecSampleExtcRepo() {
    if (execSampleExtcRepo == null) {
      execSampleExtcRepo = SpringContextHolder.getBean(ExecSampleContentRepo.class);
    }
    return execSampleExtcRepo;
  }

  private ExecRepo getExecRepo() {
    if (execRepo == null) {
      execRepo = SpringContextHolder.getBean(ExecRepo.class);
    }
    return execRepo;
  }

  private ExecNodeRepo getExecNodeRepo() {
    if (execNodeRepo == null) {
      execNodeRepo = SpringContextHolder.getBean(ExecNodeRepo.class);
    }
    return execNodeRepo;
  }

  private ExecDebugRepo getExecDebugRepo() {
    if (execDebugRepo == null) {
      execDebugRepo = SpringContextHolder.getBean(ExecDebugRepo.class);
    }
    return execDebugRepo;
  }
}
