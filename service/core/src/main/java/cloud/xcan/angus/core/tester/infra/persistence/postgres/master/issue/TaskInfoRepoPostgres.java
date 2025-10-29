package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoPostgres extends TaskInfoRepo {

}
