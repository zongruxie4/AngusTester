package cloud.xcan.sdf.core.angustester.application.query.project.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrashSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ProjectTrashSearchImpl implements ProjectTrashSearch {

  @Resource
  private ProjectTrashSearchRepo projectTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<ProjectTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ProjectTrash> clz, String... matches) {
    return new BizTemplate<Page<ProjectTrash>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ProjectTrash> process() {
        Page<ProjectTrash> page = projectTrashSearchRepo.find(criteria, pageable, clz, matches);

        if (!page.isEmpty()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
              "createdByAvatar");
          userManager.setUserNameAndAvatar(page.getContent(), "deletedBy", "deletedByName",
              "deletedByAvatar");
        }
        return page;
      }
    }.execute();
  }

}
