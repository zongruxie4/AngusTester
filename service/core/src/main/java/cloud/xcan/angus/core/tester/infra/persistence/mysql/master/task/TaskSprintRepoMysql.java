package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.task;

import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import org.springframework.stereotype.Repository;

@Repository("taskSprintRepo")
public interface TaskSprintRepoMysql extends TaskSprintRepo {

}
