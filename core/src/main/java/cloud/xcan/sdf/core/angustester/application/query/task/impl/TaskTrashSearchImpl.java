package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrashSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TaskTrashSearchImpl implements TaskTrashSearch {

  @Resource
  private TaskTrashSearchRepo taskTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<TaskTrash> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<TaskTrash> clz, String... matches) {
    return new BizTemplate<Page<TaskTrash>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<TaskTrash> process() {
        Page<TaskTrash> page = taskTrashSearchRepo.find(criterias, pageable, clz, matches);

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
