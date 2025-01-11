package cloud.xcan.sdf.core.angustester.domain.task.sprint;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskSprintSearchRepo extends CustomBaseRepository<TaskSprint> {


}
