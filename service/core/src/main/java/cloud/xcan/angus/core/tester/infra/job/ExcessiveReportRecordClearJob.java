package cloud.xcan.angus.core.tester.infra.job;


import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Retain for a maximum of 30 records.
 */
@Slf4j
@Component
@Conditional(NotCommunityEditionCondition.class)
public class ExcessiveReportRecordClearJob {

  private static final String LOCK_KEY = "tester:job:ExcessiveReportRecordClearJob";

  private static final Long RESERVED_NUM = 20L;
  private static final Long COUNT = 2000L;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ReportRecordRepo reportRecordRepo;

  @Scheduled(fixedDelay = 3 * 50 * 1000, initialDelay = 2 * 5 * 1000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      List<Long> reportIds = reportRecordRepo.getReportIdsHavingCount(RESERVED_NUM, COUNT);
      if (CollectionUtils.isNotEmpty(reportIds)) {
        for (Long reportId : reportIds) {
          try {
            reportRecordRepo.deleteByReportIdAndCount(reportId, RESERVED_NUM);
          } catch (Exception e) {
            log.error("ExcessiveReportRecordClearJob#inner execute fail:{}", e.getMessage());
          }
        }
      }
    });
  }

}
