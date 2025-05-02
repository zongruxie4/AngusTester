package cloud.xcan.angus.core.tester.infra.job;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Clear nodes that have stopped execution
 */
@Slf4j
@Component
public class ExecNodeClearJob {

  private static final String LOCK_KEY = "tester:job:ExecNodeClearJob";

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ExecNodeRepo execNodeRepo;

  @Scheduled(fixedDelay = 10 * 1300, initialDelay = 1310)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 1, TimeUnit.MINUTES, () -> {
      // Submitted transaction by repo
      execNodeRepo.deleteFinishNode();
      // Fix: When directly delete running tasks
      execNodeRepo.deleteClearNode();
      log.debug("ExecNodeClearJob execute successfully");
    });
  }
}
