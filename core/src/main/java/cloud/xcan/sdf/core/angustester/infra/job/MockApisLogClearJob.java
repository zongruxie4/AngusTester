package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Slf4j
@Component
public class MockApisLogClearJob {

  private static final String LOCK_KEY = "job:angustester:MockApisLogClearJob";

  private static final Long RESERVED_NUM = 100L;
  private static final Long COUNT = 2000L;

  @Resource
  private RedisLock redisLock;

  @Resource
  private MockApisLogRepo mockApisLogRepo;

  /**
   * Only {@link MockApisLogClearJob#RESERVED_NUM} logs are reserved for each apis
   */
  @Scheduled(fixedDelay = 32 * 1000, initialDelay = 2000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 6, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("MockApisLogClearJob try lock fail!");
        return;
      }
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
      log.debug("MockApisLogClearJob execute successfully");
    } catch (Exception e) {
      log.error("MockApisLogClearJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

}
