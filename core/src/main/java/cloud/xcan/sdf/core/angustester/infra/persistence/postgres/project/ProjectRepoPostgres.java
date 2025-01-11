package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.project;

import cloud.xcan.sdf.core.angustester.domain.project.ProjectRepo;
import org.springframework.stereotype.Repository;

@Repository("projectRepo")
public interface ProjectRepoPostgres extends ProjectRepo {


}
