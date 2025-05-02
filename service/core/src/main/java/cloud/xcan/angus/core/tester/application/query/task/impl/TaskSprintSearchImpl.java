package cloud.xcan.angus.core.tester.application.query.task.impl;


import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintSearch;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TaskSprintSearchImpl implements TaskSprintSearch {

  @Resource
  private TaskSprintSearchRepo taskSprintSearchRepo;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<TaskSprint> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<TaskSprint> clz, String... matches) {
    return new BizTemplate<Page<TaskSprint>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<TaskSprint> process() {
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // taskSprintQuery.checkAndSetAuthObjectIdCriteria(criteria); -> -> All project members are visible

        Page<TaskSprint> page = taskSprintSearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          Set<Long> sprintIds = page.getContent().stream().map(TaskSprint::getId)
              .collect(Collectors.toSet());
          taskSprintQuery.setTaskNum(page.getContent(), sprintIds);
          taskSprintQuery.setProgress(page.getContent(), sprintIds);
          taskSprintQuery.setMembers(page.getContent(), sprintIds);
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

}
