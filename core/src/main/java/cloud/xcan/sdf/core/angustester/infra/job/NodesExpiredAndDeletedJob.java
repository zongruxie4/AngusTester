package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;

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
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@Conditional(CloudServiceEditionCondition.class)
public class NodesExpiredAndDeletedJob {

  private static final String LOCK_KEY = "job:angustester:NodesExpiredAndDeletedJob";

  @Resource
  private NodeCmd nodeCmd;

  @Resource
  private RedisLock redisLock;

  @Transactional(rollbackFor = Exception.class)
  @Scheduled(fixedDelay = 61 * 1000, initialDelay = 4000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 30, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("NodesExpiredAndDeletedJob try lock failure");
        return;
      }
      nodeCmd.expireAndDeleteAliYunInstances();
      log.debug("NodesExpiredAndDeletedJob execute successfully");
    } catch (Exception e) {
      log.error("NodesExpiredAndDeletedJob execute failure", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }
}
