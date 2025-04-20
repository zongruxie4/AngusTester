package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSearch;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */
@Biz
public class TaskSearchImpl implements TaskSearch {

  @Resource
  private TaskSearchRepo taskSearchRepo;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Task> search(boolean export, Set<SearchCriteria> criteria,
      PageRequest pageable, String... matches) {
    return new BizTemplate<Page<Task>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<Task> process() {
        criteria.add(SearchCriteria.equal("deleted", false));
        criteria.add(SearchCriteria.equal("sprintDeleted", false));

        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Task> page = taskSearchRepo.find(criteria, pageable, Task.class, matches);
        if (page.hasContent()) {
          if (isUserAction()){
            // Set follow flag
            taskQuery.setFollow(page.getContent());
            // Set favourite flag
            taskQuery.setFavourite(page.getContent());
          }
          // Set task tag id and name
          tagQuery.setTags(page.getContent());
          // Set current user role
          taskQuery.setCurrentRoles(page.getContent());
          // Set api target name
          taskQuery.setApiTargetName(page.getContent());
          // Set scenario target name
          taskQuery.setScenarioTargetName(page.getContent());
          // Set task progress
          taskQuery.setTaskProgress(page.getContent());
          // Set assignee name and avatar
          userManager.setUserNameAndAvatar(page.getContent(),
              "assigneeId", "assigneeName", "assigneeAvatar");

          if (export) {
            // Set reference tasks and cases
            taskFuncCaseQuery.setAssocForTask(page.getContent());
          }
        }
        return page;
      }
    }.execute();
  }
}
