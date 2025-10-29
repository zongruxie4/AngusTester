package cloud.xcan.angus.core.tester.domain.issue.trash;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskTrashSearchRepo extends CustomBaseRepository<TaskTrash> {


}
