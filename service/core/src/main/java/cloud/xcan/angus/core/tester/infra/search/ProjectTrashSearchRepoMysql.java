package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectTrashSearchRepoMysql extends SimpleSearchRepository<ProjectTrash> implements
    ProjectTrashSearchRepo {

}
