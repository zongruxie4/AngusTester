package cloud.xcan.sdf.core.angustester.application.query.task.impl;


import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintSearch;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
  public Page<TaskSprint> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<TaskSprint> clz, String... matches) {
    return new BizTemplate<Page<TaskSprint>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<TaskSprint> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        // taskSprintQuery.checkAndSetAuthObjectIdCriteria(criterias); -> -> All project members are visible

        Page<TaskSprint> page = taskSprintSearchRepo.find(criterias, pageable, clz, matches);
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
