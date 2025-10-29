package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository("taskRepo")
public interface TaskRepoPostgres extends TaskRepo {

}
