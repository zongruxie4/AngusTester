package cloud.xcan.angus.core.tester.infra.persistence.mysql.project;

import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import org.springframework.stereotype.Repository;

@Repository("projectRepo")
public interface ProjectRepoMysql extends ProjectRepo {

}
