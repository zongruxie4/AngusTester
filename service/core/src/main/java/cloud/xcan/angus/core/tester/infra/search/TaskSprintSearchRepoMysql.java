package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class TaskSprintSearchRepoMysql extends SimpleSearchRepository<TaskSprint> implements
    TaskSprintSearchRepo {

}

