package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("taskInfoRepo")
public interface TaskInfoRepoMysql extends TaskInfoRepo {

}
