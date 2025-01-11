package cloud.xcan.sdf.core.angustester.infra.job;

import static org.springframework.cache.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeCmd;
import cloud.xcan.sdf.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Conditional(CloudServiceEditionCondition.class)
public class NodeInstanceInfoSyncJob {

  private static final String LOCK_KEY = "job:angustester:NodeInstanceInfoSyncJob";

  @Resource
  private NodeCmd nodeCmd;

  @Resource
  private RedisLock redisLock;

  @Scheduled(fixedDelay = 11 * 1000, initialDelay = 3000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 15, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("NodeInfoSyncJob try lock failure");
        return;
      }
      nodeCmd.syncInstanceInfo();
      log.debug("NodeInfoSyncJob execute successfully");
    } catch (Exception e) {
      log.error("NodeInfoSyncJob execute failure", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }
}
