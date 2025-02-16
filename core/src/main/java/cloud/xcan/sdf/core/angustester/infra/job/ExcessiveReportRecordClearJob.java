package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordRepo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Retain for a maximum of 30 records.
 */
@Slf4j
@Component
public class ExcessiveReportRecordClearJob {

  private static final String LOCK_KEY = "job:angustester:ExcessiveReportRecordClearJob";

  private static final Long RESERVED_NUM = 20L;
  private static final Long COUNT = 2000L;

  @Resource
  private RedisLock redisLock;

  @Resource
  private ReportRecordRepo reportRecordRepo;

  @Scheduled(fixedDelay = 3 * 50 * 1000, initialDelay = 2 * 5 * 1000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 6, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("ExcessiveReportRecordClearJob try lock fail!");
        return;
      }
      List<Long> reportIds = reportRecordRepo.getReportIdsHavingCount(RESERVED_NUM, COUNT);
      if (CollectionUtils.isNotEmpty(reportIds)) {
        for (Long reportId : reportIds) {
          try {
            // Submitted transaction by repo
            reportRecordRepo.deleteByReportIdAndCount(reportId, RESERVED_NUM);
          } catch (Exception e) {
            log.error("ExcessiveReportRecordClearJob#inner execute fail:{}", e.getMessage());
          }
        }
      }
      log.debug("ExcessiveReportRecordClearJob execute successfully");
    } catch (Exception e) {
      log.error("ExcessiveReportRecordClearJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

}
