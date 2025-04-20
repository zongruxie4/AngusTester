package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectListRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Project> mainClz,
      Object[] params, String... matches) {
    return projectListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria, "project", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return projectListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
