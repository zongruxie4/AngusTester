package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.task;

import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoPostgres extends TaskInfoRepo {

}
