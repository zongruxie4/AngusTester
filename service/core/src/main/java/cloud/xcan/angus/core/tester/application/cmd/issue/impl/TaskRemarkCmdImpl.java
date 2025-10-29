package cloud.xcan.angus.core.tester.application.cmd.issue.impl;


import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskRemarkCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemark;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemarkRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task remark command operations for task comments.
 *
 * <p>This class provides functionality for managing task remarks,
 * allowing users to add comments and notes to tasks.</p>
 *
 * <p>It handles the complete lifecycle of task remarks from creation
 * to deletion, including quota management and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Task remark creation with quota validation</li>
 *   <li>Remark deletion with activity logging</li>
 *   <li>Task modification notification events</li>
 *   <li>Permission management for remark operations</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class TaskRemarkCmdImpl extends CommCmd<TaskRemark, Long> implements TaskRemarkCmd {

  @Resource
  private TaskRemarkRepo taskRemarkRepo;
  @Resource
  private TaskRemarkQuery taskRemarkQuery;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a remark to a task with quota validation.
   *
   * <p>This method creates a remark for a task after verifying
   * the task exists and checking quota limits.</p>
   *
   * <p>The method logs remark creation activity and sends
   * modification notification events.</p>
   *
   * @param remark the remark to add
   * @return the ID key of the created remark
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public IdKey<Long, Object> add(TaskRemark remark) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFindInfo(remark.getTaskId());

        // Verify remark quota limits are not exceeded
        taskRemarkQuery.checkAddQuota(remark.getTaskId(), 1);

        // Allow any user to add remarks
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(remark);

        Activity activity = toActivity(TASK, taskDb, ActivityType.TASK_REMARK_ADD);
        activityCmd.add(activity);

        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        return idKey;
      }
    }.execute();
  }

  /**
   * Deletes a task remark with activity logging.
   *
   * <p>This method removes a remark from a task and logs
   * the deletion activity for audit purposes.</p>
   *
   * @param id the remark ID to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      TaskRemark taskRemarkDb;
      TaskInfo taskDb;

      @Override
      protected void checkParams() {
        taskRemarkDb = taskRemarkQuery.checkAndFind(id);
        taskDb = taskQuery.checkAndFindInfo(taskRemarkDb.getTaskId());
      }

      @Override
      protected Void process() {
        // Delete remark
        taskRemarkRepo.deleteById(id);

        activityCmd.add(toActivity(TASK, taskDb, ActivityType.TASK_REMARK_DELETE));
        return null;
      }
    }.execute();
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task remark repository
   */
  @Override
  protected BaseRepository<TaskRemark, Long> getRepository() {
    return this.taskRemarkRepo;
  }
}
