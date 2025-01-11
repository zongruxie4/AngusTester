package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrashSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TaskTrashSearchRepoMysql extends SimpleSearchRepository<TaskTrash> implements
    TaskTrashSearchRepo {

}
