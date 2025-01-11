package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrashSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectTrashSearchRepoMysql extends SimpleSearchRepository<ProjectTrash> implements
    ProjectTrashSearchRepo {

}
