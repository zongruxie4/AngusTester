package cloud.xcan.angus.core.tester.application.query.project.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashSearch;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
