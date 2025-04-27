package cloud.xcan.angus.core.tester.infra.persistence.postgres.task;

import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
 */
@Repository("taskRepo")
public interface TaskRepoPostgres extends TaskRepo {

}
