package cloud.xcan.angus.core.tester.infra.job;


import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeCmd;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Automatically install the agent on the online purchase node.
 *
 * @author XiaoLong Liu
 */
@Slf4j
@Component
public class NodeAgentAutoInstallJob {

  private static final String LOCK_KEY = "tester:job:NodeAgentAutoInstallJob";

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private NodeCmd nodeCmd;

  @Scheduled(fixedDelay = 15 * 1000, initialDelay = 3000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 30, TimeUnit.MINUTES, () -> {
      nodeCmd.agentAutoInstall();
    });
  }

}
