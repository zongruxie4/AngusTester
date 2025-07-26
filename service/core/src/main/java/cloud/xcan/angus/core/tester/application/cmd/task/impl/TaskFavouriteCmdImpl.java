package cloud.xcan.angus.core.tester.application.cmd.task.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_FAVOURITE_REPEATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavourite;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavouriteRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task favorite command operations for task bookmarking.
 * 
 * <p>This class provides functionality for managing task favorites,
 * allowing users to bookmark and track important tasks.</p>
 * 
 * <p>It handles the complete lifecycle of task favorites from creation
 * to cancellation, including duplicate prevention and activity logging.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>Task favorite creation with duplicate prevention</li>
 *   <li>Individual and bulk favorite cancellation</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Project-level favorite management</li>
 * </ul></p>
 */
@Biz
public class TaskFavouriteCmdImpl extends CommCmd<TaskFavourite, Long> implements
    TaskFavouriteCmd {

  @Resource
  private TaskFavouriteRepo taskFavoriteRepo;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a task to user's favorites with duplicate prevention.
   * 
   * <p>This method creates a favorite entry for a task after verifying
   * the task exists and preventing duplicate favorites from the same user.</p>
   * 
   * <p>The method logs favorite creation activity for audit purposes.</p>
   * 
   * @param favorite the favorite entry to create
   * @return the ID key of the created favorite
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public IdKey<Long, Object> add(TaskFavourite favorite) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFindInfo(favorite.getTaskId());

        // Verify no duplicate favorite exists for this user and task
        if (taskFavoriteRepo.countByTaskIdAndCreatedBy(favorite.getTaskId(), getUserId()) > 0) {
          throw ResourceExisted.of(TASK_FAVOURITE_REPEATED, new Object[]{});
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        favorite.setProjectId(taskDb.getProjectId());
        IdKey<Long, Object> idKey = insert(favorite);

        // Add favourite task activity
        activityCmd.add(toActivity(TASK, taskDb, ActivityType.FAVOURITE));
        return idKey;
      }
    }.execute();
  }

  /**
   * Cancels a task favorite with activity logging.
   * 
   * <p>This method removes a task from user's favorites and logs
   * the cancellation activity for audit purposes.</p>
   * 
   * @param id the task ID to remove from favorites
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
        if (taskFavoriteRepo.deleteByTaskIdAndCreatedBy(id, getUserId()) > 0) {
          //Add cancel favourite api activity
          activityCmd.add(toActivity(TASK, taskDb, ActivityType.FAVOURITE_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Cancels all task favorites for a user, optionally filtered by project.
   * 
   * <p>This method removes all favorites for the current user, either
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
          taskFavoriteRepo.deleteByCreatedBy(getUserId());
        } else {
          taskFavoriteRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  /**
   * Returns the repository instance for this command.
   * 
   * @return the task favorite repository
   */
  @Override
  protected BaseRepository<TaskFavourite, Long> getRepository() {
    return this.taskFavoriteRepo;
  }

}
