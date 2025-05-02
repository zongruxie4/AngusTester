package cloud.xcan.angus.core.tester.infra.job;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeCmd;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Conditional(CloudServiceEditionCondition.class)
public class NodeInstanceInfoSyncJob {

  private static final String LOCK_KEY = "tester:job:NodeInstanceInfoSyncJob";

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private NodeCmd nodeCmd;

  @Scheduled(fixedDelay = 11 * 1000, initialDelay = 3000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      nodeCmd.syncInstanceInfo();
    });
  }
}
