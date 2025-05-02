package cloud.xcan.angus.core.tester.infra.job;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecTestResultCmd;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecTestResultGenJob {

  private static final String LOCK_KEY = "tester:job:ExecTestResultGenJob";
  private static final int COUNT = 30;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ExecRepo execRepo;

  @Resource
  private ExecTestResultCmd execTestResultCmd;

  @Scheduled(fixedDelay = 2 * 1200, initialDelay = 1310)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 3, TimeUnit.MINUTES, () -> {
      // Submitted transaction by repo
      List<Exec> execDbs = execRepo.findUpdateResultTask(COUNT);
      if (isNotEmpty(execDbs)) {
        for (Exec execDb : execDbs) {
          String syncTestResultFailure = null;
          try {
            PrincipalContext.set(new Principal().setOptTenantId(execDb.getTenantId()));
            execTestResultCmd.generateResult(execDb);
          } catch (Exception e) {
            syncTestResultFailure = e.getMessage();
            log.error("ExecTestResultGenJob#inner updateSyncTestResult fail: ", e);
            e.printStackTrace();
          } finally {
            try {
              // Note: Only synchronize once when synchronization fails and prevent job continuous polling.
              execRepo.updateSyncTestResult(execDb.getId(), syncTestResultFailure);
            } catch (Exception e) {
              e.printStackTrace();
            }
            PrincipalContext.remove();
          }
        }
      }
    });
  }

}
