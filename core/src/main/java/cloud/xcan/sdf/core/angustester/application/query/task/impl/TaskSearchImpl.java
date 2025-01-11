package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSearch;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
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
  public Page<Task> search(boolean exportFlag, Set<SearchCriteria> criterias,
      PageRequest pageable, String... matches) {
    return new BizTemplate<Page<Task>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<Task> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));
        criterias.add(SearchCriteria.equal("sprintDeletedFlag", false));

        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);
        Page<Task> page = taskSearchRepo.find(criterias, pageable, Task.class, matches);
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
          // Set assignee name and avatar
          userManager.setUserNameAndAvatar(page.getContent(),
              "assigneeId", "assigneeName", "assigneeAvatar");

          if (exportFlag) {
            // Set reference tasks and cases
            taskFuncCaseQuery.setAssocForTask(page.getContent());
          }
        }
        return page;
      }
    }.execute();
  }
}
