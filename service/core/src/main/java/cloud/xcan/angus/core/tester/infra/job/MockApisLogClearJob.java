package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogRepo;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
 */@Slf4j
@Component
public class MockApisLogClearJob {

  private static final String LOCK_KEY = "job:angustester:MockApisLogClearJob";

  private static final Long RESERVED_NUM = 100L;
  private static final Long COUNT = 2000L;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private MockApisLogRepo mockApisLogRepo;

  /**
   * Only {@link MockApisLogClearJob#RESERVED_NUM} logs are reserved for each apis
   */
  @Scheduled(fixedDelay = 32 * 1000, initialDelay = 2000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      List<Long> apisIds = mockApisLogRepo.getApisIdsHavingCount(RESERVED_NUM, COUNT);
      if (isNotEmpty(apisIds)) {
        for (Long apisId : apisIds) {
          try {
            // Submitted transaction by repo
            mockApisLogRepo.deleteByApisIdAndCount(apisId, RESERVED_NUM);
          } catch (Exception e) {
            log.error("MockApisLogClearJob#inner execute fail:{}", e.getMessage());
          }
        }
      }
    });
  }

}
