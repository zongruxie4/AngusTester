package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.project;

import cloud.xcan.sdf.core.angustester.domain.project.ProjectRepo;
import org.springframework.stereotype.Repository;

@Repository("projectRepo")
public interface ProjectRepoMysql extends ProjectRepo {

}
