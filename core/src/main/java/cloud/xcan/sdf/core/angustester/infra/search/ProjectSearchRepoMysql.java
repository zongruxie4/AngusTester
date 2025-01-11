package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectListRepo;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectSearchRepoMysql extends AbstractSearchRepository<Project> implements
    ProjectSearchRepo {

  @Resource
  private ProjectListRepo projectListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return projectListRepo.getSqlTemplate0(getSearchMode(), step, criterias, "project",
        matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return projectListRepo.getReturnFieldsCondition(criterias, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
