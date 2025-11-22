package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportCmd;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.core.tester.domain.report.ReportRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Conditional(NotCommunityEditionCondition.class)
public class ReportGenerateJob {

  private static final String LOCK_KEY = "tester:job:ReportGenerateJob";

  private static final Long COUNT = 20L;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ReportRepo reportRepo;

  @Resource
  private ReportCmd reportCmd;

  @Scheduled(fixedDelay = 15 * 1000, initialDelay = 5900)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      List<Report> reportsDb = reportRepo.findGenerateByNow(COUNT);
      if (isNotEmpty(reportsDb)) {
        for (Report reportDb : reportsDb) {
          reportCmd.generateRecord(reportDb);
        }
      }
    });
  }

}
