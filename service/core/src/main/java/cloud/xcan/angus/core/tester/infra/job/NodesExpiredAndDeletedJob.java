package cloud.xcan.angus.core.tester.infra.job;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.spring.condition.CloudServiceEditionCondition;
import cloud.xcan.angus.core.tester.application.cmd.config.NodeCmd;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@Conditional(CloudServiceEditionCondition.class)
public class NodesExpiredAndDeletedJob {

  private static final String LOCK_KEY = "tester:job:NodesExpiredAndDeletedJob";

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private NodeCmd nodeCmd;

  @Transactional(rollbackFor = Exception.class)
  @Scheduled(fixedDelay = 61 * 1000, initialDelay = 4000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 30, TimeUnit.MINUTES, () -> {
      nodeCmd.expireAndDeleteAliYunInstances();
    });
  }
}
