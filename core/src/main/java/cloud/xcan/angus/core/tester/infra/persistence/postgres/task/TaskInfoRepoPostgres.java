package cloud.xcan.angus.core.tester.infra.persistence.postgres.task;

import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoPostgres extends TaskInfoRepo {

}
