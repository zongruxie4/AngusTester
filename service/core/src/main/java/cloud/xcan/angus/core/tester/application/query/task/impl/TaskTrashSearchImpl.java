package cloud.xcan.angus.core.tester.application.query.task.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashSearch;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TaskTrashSearchImpl implements TaskTrashSearch {

  @Resource
  private TaskTrashSearchRepo taskTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<TaskTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<TaskTrash> clz, String... matches) {
    return new BizTemplate<Page<TaskTrash>>() {

      @Override
      protected Page<TaskTrash> process() {
        Page<TaskTrash> page = taskTrashSearchRepo.find(criteria, pageable, clz, matches);

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
