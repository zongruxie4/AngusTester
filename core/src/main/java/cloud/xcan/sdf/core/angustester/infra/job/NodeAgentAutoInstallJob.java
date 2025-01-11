package cloud.xcan.sdf.core.angustester.infra.job;


import static org.springframework.cache.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeCmd;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Automatically install the agent on the online purchase node.
 *
 * @author xiaolong.liu
 */
@Slf4j
@Component
public class NodeAgentAutoInstallJob {

  private static final String LOCK_KEY = "job:angustester:NodeAgentAutoInstallJob";

  @Resource
  private NodeCmd nodeCmd;

  @Resource
  private RedisLock redisLock;

  @Scheduled(fixedDelay = 15 * 1000, initialDelay = 3000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 30, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("NodeAgentAutoInstallJob try lock failure");
        return;
      }
      nodeCmd.agentAutoInstall();
      log.debug("NodeAgentAutoInstallJob execute successfully");
    } catch (Exception e) {
      log.error("NodeAgentAutoInstallJob execute failure", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

}
