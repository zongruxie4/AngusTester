package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.api.commonlink.setting.Setting;
import cloud.xcan.sdf.api.commonlink.setting.SettingKey;
import cloud.xcan.sdf.api.manager.SettingManager;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityRepo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActivityClearJob {

  private static final String LOCK_KEY = "job:angustester:ActivityClearJob";

  private static final Long RESERVED_NUM = 200L;
  private static final Long COUNT = 2000L;

  @Resource
  private RedisLock redisLock;

  @Resource
  private ActivityRepo activityRepo;

  @Resource
  private SettingManager settingManager;

  /**
   * Only {@link ActivityClearJob#RESERVED_NUM} activities are reserved for each target
   */
  @Scheduled(fixedDelay = 31 * 1000, initialDelay = 1000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 6, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("ActivityClearJob try lock fail!");
        return;
      }
      long reservedNum = getReservedNum();
      List<Long> targetIds = activityRepo.getTargetIdsHavingCount(reservedNum, COUNT);
      if (CollectionUtils.isNotEmpty(targetIds)) {
        for (Long targetId : targetIds) {
          try {
            // Submitted transaction by repo
            activityRepo.deleteByTargetIdAndCount(targetId, reservedNum);
          } catch (Exception e) {
            log.error("ActivityClearJob#inner execute fail:{}", e.getMessage());
          }
        }
      }
      log.debug("ActivityClearJob execute successfully");
    } catch (Exception e) {
      log.error("ActivityClearJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

  private long getReservedNum() {
    long reservedNum = RESERVED_NUM;
    try {
      Setting setting = settingManager.setting(SettingKey.MAX_RESOURCE_ACTIVITIES);
      reservedNum = setting.getMaxResourceActivities() > 0 ? setting.getMaxResourceActivities()
          : RESERVED_NUM;
    } catch (Exception e) {
      log.error("The maximum number of resource activities is not configured, SettingKey: {}",
          SettingKey.MAX_RESOURCE_ACTIVITIES.getValue());
    }
    return reservedNum;
  }

}
