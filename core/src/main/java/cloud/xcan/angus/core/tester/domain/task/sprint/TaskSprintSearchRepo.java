package cloud.xcan.angus.core.tester.domain.task.sprint;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskSprintSearchRepo extends CustomBaseRepository<TaskSprint> {


}
