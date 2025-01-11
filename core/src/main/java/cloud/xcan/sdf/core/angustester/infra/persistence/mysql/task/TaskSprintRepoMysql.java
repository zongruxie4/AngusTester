package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task;

import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintRepo;
import org.springframework.stereotype.Repository;

@Repository("taskSprintRepo")
public interface TaskSprintRepoMysql extends TaskSprintRepo {

}
