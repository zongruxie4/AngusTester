package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportCmd;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportRepo;
import cloud.xcan.sdf.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Conditional(NotCommunityEditionCondition.class)
public class ReportGenerateJob {

  private static final String LOCK_KEY = "job:angustester:ReportGenerateJob";

  private static final Long COUNT = 20L;

  @Resource
  private ReportRepo reportRepo;

  @Resource
  private ReportCmd reportCmd;

  @Resource
  private RedisLock redisLock;

  @Scheduled(fixedDelay = 15 * 1000, initialDelay = 5900)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 5, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("ReportGenerateJob try lock fail!");
        return;
      }
      List<Report> reportsDb = reportRepo.findGenerateByNow(COUNT);
      if (isNotEmpty(reportsDb)) {
        for (Report reportDb : reportsDb) {
          reportCmd.generateRecord(reportDb);
        }
      }
      log.debug("ReportGenerateJob execute successfully");
    } catch (Exception e) {
      log.error("ReportGenerateJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

}
