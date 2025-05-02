package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoRepo;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleRepo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.model.meter.MeterStatus;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.DateUtils;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Closing sampling collection timeout exceeding 3 iteration intervals and release execution timeout
 * nodes.
 */
@Slf4j
@Component
public class ExecTimeoutJob {

  private static final String LOCK_KEY = "tester:job:ExecTimeoutJob";
  private static final int COUNT = 200;
  private static final int DURATION_EXCEEDS_MINUTES = 2;
  private static final int MAX_DURATION_EXCEEDS_MINUTES = 24 * 60;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ExecInfoRepo execInfoRepo;

  @Resource
  private ExecNodeRepo execNodeRepo;

  @Resource
  private ExecSampleRepo execSampleRepo;

  @Resource
  private ExecCmd execCmd;

  @Scheduled(fixedDelay = 32 * 1000, initialDelay = 1200)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 10, TimeUnit.MINUTES, () -> {
      // Submitted transaction by repo
      List<ExecInfo> runningExecs = execInfoRepo.findRunningByDuration(
          LocalDateTime.now().minusMinutes(DURATION_EXCEEDS_MINUTES), COUNT);
      if (isNotEmpty(runningExecs)) {
        for (ExecInfo execDb : runningExecs) {
          try {
            PrincipalContext.set(new Principal().setOptTenantId(execDb.getTenantId()));

            // Exceeded the maximum time limit by 24 hours
            if (execDb.getActualStartDate().plusMinutes(MAX_DURATION_EXCEEDS_MINUTES)
                .isBefore(LocalDateTime.now())) {
              try {
                execCmd.stop0(execDb, new ExecStopDto().setId(execDb.getId()).setBroadcast(true));
              } catch (Exception e) {
                e.printStackTrace();
              }

              execInfoRepo.updateFinishById(execDb.getId(), ExecStatus.TIMEOUT.getValue(),
                  LocalDateTime.now(), MeterStatus.TIMEOUT.name(),
                  "Exceeding the maximum 24-hour time limit");
              execNodeRepo.deleteByExecId(execDb.getId());
            }

            // It has not yet reached the sampling interval
            if (execDb.getActualStartDate()
                .plusSeconds(Double.valueOf(execDb.getReportInterval().toSecond()).intValue())
                .isAfter(LocalDateTime.now())) {
              return;
            }

            ExecSample sample = execSampleRepo.findLatestByExecId(execDb.getId());
            int timeoutInterval =
                Double.valueOf(execDb.getReportInterval().toSecond()).intValue() * 6;
            if ((isNull(sample) && execDb.getActualStartDate()
                .plusSeconds(timeoutInterval).isBefore(LocalDateTime.now())) ||
                DateUtils.asLocalDateTime(new Date(sample.getTimestamp()))
                    .plusSeconds(timeoutInterval).isBefore(LocalDateTime.now())) {
              try {
                execCmd.stop0(execDb, new ExecStopDto().setId(execDb.getId()).setBroadcast(true));
              } catch (Exception e) {
                e.printStackTrace();
              }

              execInfoRepo.updateFinishById(execDb.getId(), ExecStatus.TIMEOUT.getValue(),
                  LocalDateTime.now(), MeterStatus.TIMEOUT.name(),
                  "Obtaining the latest sampling content exceeds the limit of " + timeoutInterval
                      + " seconds");
              execNodeRepo.deleteByExecId(execDb.getId());
            }
          } catch (Exception e) {
            log.error("ExecTimeoutJob#inner execute fail: ", e);
          } finally {
            PrincipalContext.remove();
          }
        }
      }
    });
  }
}
