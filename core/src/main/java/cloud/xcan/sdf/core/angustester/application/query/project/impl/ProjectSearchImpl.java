package cloud.xcan.sdf.core.angustester.application.query.project.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectSearch;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
  public Page<Project> search(Set<SearchCriteria> criterias, Pageable pageable, Class<Project> clz,
      String... matches) {
    return new BizTemplate<Page<Project>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Project> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);
        Page<Project> projectPage = projectSearchRepo.find(criterias, pageable, Project.class,
            matches);
        if (projectPage.hasContent()) {
          projectMemberQuery.setMembers(projectPage.getContent());
        }
        return projectPage;
      }
    }.execute();
  }
}




