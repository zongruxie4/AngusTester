package cloud.xcan.angus.core.tester.infra.job;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Start tasks according to priority. If scheduling is unsuccessful for more than 1 day (Timeout by
 * ExecTimeoutJob), it will automatically fail. After the failure, it needs to be manually started.
 * <p>
 * Limit: The number of executing tasks cannot exceed the quota for concurrent tasks.
 */
@Slf4j
@Component
public class ExecStartJob {

  private static final String LOCK_KEY = "tester:job:ExecStartJob";
  private static final int COUNT = 10/*50 -> Prevent locking for too long when the quantity is too large. */;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ExecRepo execRepo;

  @Resource
  private ExecCmd execCmd;

  @Scheduled(fixedDelay = 3 * 1300, initialDelay = 1210)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 3, TimeUnit.MINUTES, () -> {
      // Submitted transaction by repo
      List<Exec> execDbs = execRepo.findSchedulingTask(COUNT);
      if (isNotEmpty(execDbs)) {
        for (Exec execDb : execDbs) {
          try {
            PrincipalContext.set(new Principal().setOptTenantId(execDb.getTenantId()));
            execCmd.start0(execDb, new ExecStartDto().setId(execDb.getId()).setBroadcast(true));
          } catch (Exception e) {
            log.error("ExecStartJob#inner execute fail: ", e);
          } finally {
            PrincipalContext.remove();
          }
        }
      }
    });
  }

}
