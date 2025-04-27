package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioMonitorCmd;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorRepo;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.thread.MultiTaskThreadPool;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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
  private JobTemplate jobTemplate;

  @Resource
  private ScenarioMonitorRepo scenarioMonitorRepo;

  @Resource
  private ScenarioMonitorCmd scenarioMonitorCmd;

  @Resource
  private MultiTaskThreadPool taskThreadPool;

  @Scheduled(fixedDelay = 61 * 1000, initialDelay = 23000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      List<ScenarioMonitor> monitorsDb = scenarioMonitorRepo.findRunByNow(COUNT);
      if (isNotEmpty(monitorsDb)) {
        for (ScenarioMonitor monitorDb : monitorsDb) {
          taskThreadPool.submit((Callable<Void>) () -> {
            scenarioMonitorCmd.runNow(monitorDb.getId());
            return null;
          }).get(); // Waiting for task execution to complete
        }
      }
    });
  }

  @Bean
  public MultiTaskThreadPool multiTaskThreadPool() {
    return new MultiTaskThreadPool("ScenarioMonitorThread",
        5, 10, 60, 500);
  }

}
