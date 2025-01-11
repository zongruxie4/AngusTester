package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static org.springframework.cache.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioMonitorCmd;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorRepo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.thread.MultiTaskThreadPool;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@Component
@DoInFuture("Conflict with AngusCtrl ExecRunnerClearJob")
public class ScenarioMonitorJob {

  private static final String LOCK_KEY = "job:angustester:ScenarioMonitorJob";

  private static final Long COUNT = 100L;

  @Resource
  private ScenarioMonitorRepo scenarioMonitorRepo;

  @Resource
  private ScenarioMonitorCmd scenarioMonitorCmd;

  @Resource
  private MultiTaskThreadPool taskThreadPool;

  @Resource
  private RedisLock redisLock;

  @Scheduled(fixedDelay = 61 * 1000, initialDelay = 23000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 10, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("ScenarioMonitorJob try lock fail!");
        return;
      }
      List<ScenarioMonitor> monitorsDb = scenarioMonitorRepo.findRunByNow(COUNT);
      if (isNotEmpty(monitorsDb)) {
        for (ScenarioMonitor monitorDb : monitorsDb) {
          taskThreadPool.submit((Callable<Void>) () -> {
            scenarioMonitorCmd.runNow(monitorDb.getId());
            return null;
          }).get(); // Waiting for task execution to complete
        }
      }
      log.debug("ScenarioMonitorJob execute successfully");
    } catch (Exception e) {
      log.error("ScenarioMonitorJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

  @Bean
  public MultiTaskThreadPool multiTaskThreadPool() {
    return new MultiTaskThreadPool("ScenarioMonitorThread",
        5, 10, 60, 500);
  }

}
