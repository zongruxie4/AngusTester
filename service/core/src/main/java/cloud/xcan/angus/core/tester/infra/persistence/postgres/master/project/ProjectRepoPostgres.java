package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.project;

import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import org.springframework.stereotype.Repository;

@Repository("projectRepo")
public interface ProjectRepoPostgres extends ProjectRepo {

}
