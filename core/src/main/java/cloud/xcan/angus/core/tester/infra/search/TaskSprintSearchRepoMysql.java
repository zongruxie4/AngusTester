package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class TaskSprintSearchRepoMysql extends SimpleSearchRepository<TaskSprint> implements
    TaskSprintSearchRepo {

}

