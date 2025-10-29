package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintRepo;
import org.springframework.stereotype.Repository;

@Repository("taskSprintRepo")
public interface TaskSprintRepoMysql extends TaskSprintRepo {

}
