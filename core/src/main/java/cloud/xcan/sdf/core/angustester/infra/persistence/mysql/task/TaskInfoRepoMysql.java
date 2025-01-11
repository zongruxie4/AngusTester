package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task;

import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoMysql extends TaskInfoRepo {

}
