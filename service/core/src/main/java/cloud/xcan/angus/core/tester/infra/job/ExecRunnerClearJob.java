package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.config.NodeInfoCmd;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfoRepo;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Clear runners that have not stopped properly.
 */
@DoInFuture("Conflict with AngusTester scenario, script debug and ScenarioMonitorJob")
@Slf4j
@Component
public class ExecRunnerClearJob {

  private static final String LOCK_KEY = "tester:job:ExecRunnerClearJob";

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private NodeInfoRepo nodeInfoRepo;

  @Resource
  private NodeInfoCmd nodeInfoCmd;

  @Scheduled(fixedDelay = 33 * 60 * 1000, initialDelay = 1200)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 1, TimeUnit.MINUTES, () -> {
      // @DoInFuture("Non executing nodes will also be triggered for cleaning tasks")
      List<NodeInfo> nodeInfo = nodeInfoRepo.findNodeIdsByNotInExec();
      if (isNotEmpty(nodeInfo)) {
        nodeInfoCmd.clearRunner(nodeInfo);
      }
    });
  }
}
