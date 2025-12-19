package cloud.xcan.angus.core.tester.application.cmd.issue.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_FOLLOW_REPEATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskFollowCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollow;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task follow command operations for task tracking.
 *
 * <p>This class provides functionality for managing task follows,
 * allowing users to track and receive updates on specific tasks.</p>
 *
 * <p>It handles the complete lifecycle of task follows from creation
 * to cancellation, including duplicate prevention and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Task follow creation with duplicate prevention</li>
 *   <li>Individual and bulk follow cancellation</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Project-level follow management</li>
 * </ul></p>
 */
@Service
public class TaskFollowCmdImpl extends CommCmd<TaskFollow, Long> implements TaskFollowCmd {

  @Resource
  private TaskFollowRepo taskFollowRepo;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a task follow with duplicate prevention.
   *
   * <p>This method creates a follow entry for a task after verifying
   * the task exists and preventing duplicate follows from the same user.</p>
   *
   * <p>The method logs follow creation activity for audit purposes.</p>
   *
   * @param follow the follow entry to create
   * @return the ID key of the created follow
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskFollow follow) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskInfo taskDb = null;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFindInfo(follow.getTaskId());
        // Verify no duplicate follow exists for this user and task
        if (taskFollowRepo.countByTaskIdAndCreatedBy(follow.getTaskId(), getUserId()) > 0) {
          throw ResourceExisted.of(TASK_FOLLOW_REPEATED, new Object[]{});
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        follow.setProjectId(taskDb.getProjectId());
        IdKey<Long, Object> idKey = insert(follow);

        // Add follow task activity
        activityCmd.add(toActivity(TASK, taskDb, ActivityType.FOLLOW));
        return idKey;
      }
    }.execute();
  }

  /**
   * Cancels a task follow with activity logging.
   *
   * <p>This method removes a task follow and logs the cancellation
   * activity for audit purposes.</p>
   *
   * @param id the task ID to stop following
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancel(Long id) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFindInfo(id);
      }

      @Override
      protected Void process() {
        if (taskFollowRepo.deleteByTaskIdAndCreatedBy(id, getUserId()) > 0) {
          // Add cancel follow api activity
          activityCmd.add(toActivity(TASK, taskDb, ActivityType.FOLLOW_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Cancels all task follows for a user, optionally filtered by project.
   *
   * <p>This method removes all follows for the current user, either
   * globally or within a specific project scope.</p>
   *
   * @param projectId the project ID to filter by (null for all projects)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (isNull(projectId)) {
          taskFollowRepo.deleteByCreatedBy(getUserId());
        } else {
          taskFollowRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task follow repository
   */
  @Override
  protected BaseRepository<TaskFollow, Long> getRepository() {
    return this.taskFollowRepo;
  }
}
