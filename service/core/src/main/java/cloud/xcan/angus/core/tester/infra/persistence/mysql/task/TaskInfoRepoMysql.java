package cloud.xcan.angus.core.tester.infra.persistence.mysql.task;

import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoMysql extends TaskInfoRepo {

}
