package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.task;

import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintRepo;
import org.springframework.stereotype.Repository;

@Repository("taskSprintRepo")
public interface TaskSprintRepoPostgres extends TaskSprintRepo {

}
