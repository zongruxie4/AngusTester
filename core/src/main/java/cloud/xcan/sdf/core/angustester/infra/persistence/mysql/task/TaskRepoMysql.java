package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task;

import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import org.springframework.stereotype.Repository;

/**
 * @author xiaolong.liu
 */
@Repository("taskRepo")
public interface TaskRepoMysql extends TaskRepo {

}
