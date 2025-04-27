package cloud.xcan.angus.core.tester.application.query.project.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectSearch;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ProjectSearchImpl implements ProjectSearch {

  @Resource
  private ProjectSearchRepo projectSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<Project> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Project> clz,
      String... matches) {
    return new BizTemplate<Page<Project>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Project> process() {
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Project> projectPage = projectSearchRepo.find(criteria, pageable, Project.class,
            matches);
        if (projectPage.hasContent()) {
          projectMemberQuery.setMembers(projectPage.getContent());
        }
        return projectPage;
      }
    }.execute();
  }
}




