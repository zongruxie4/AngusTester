package cloud.xcan.angus.core.tester.application.cmd.issue.impl;


import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_SPRINT_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_TASK_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.TASK_BID_KEY;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertEnumOf;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toModifyTaskActivity;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleExampleTask;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleExampleTaskSoftwareVersion;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleExampleTaskSprint;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.assembleMoveTask;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.importToDomain;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_EVAL_WORKLOAD_NOT_SET;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_IMPORT_COLUMNS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_IMPORT_REQUIRED_COLUMNS;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_NO_CONFIRMING;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_NO_CONFIRMING_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_NO_PROCESSING;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_NO_PROCESSING_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_START_NO_PENDING;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_START_NO_PENDING_CODE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ACTUAL_WORKLOAD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ACTUAL_WORKLOAD_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_CASE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_CASE_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_TASK;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.ASSOC_TASK_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.BUG_LEVEL_UPDATED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DEADLINE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DESCRIPTION_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.DESCRIPTION_UPDATED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.EVAL_WORKLOAD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.EVAL_WORKLOAD_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MISSING_BUG_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MISSING_BUG_SET;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.NAME_UPDATED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.PRIORITY;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SOFTWARE_VERSION_UPDATE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_ASSIGNEE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_ASSIGNEE_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_COMPLETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_CONFIRMER;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_CONFIRMER_CLEAR;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_CONFIRM_RESULT;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_PROCESSED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_REOPEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_RESTART;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_SUB_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TASK_SUB_SET;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TYPE_UPDATED;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.DateUtils.DATE_TIME_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.collectionEquals;
import static cloud.xcan.angus.spec.utils.ObjectUtils.duplicateByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.commonlink.user.User;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskFuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.SoftwareVersionCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.converter.TaskConverter;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.project.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemarkRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.module.Module;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of task command operations for comprehensive task management.
 *
 * <p>This class provides extensive functionality for managing tasks throughout
 * their complete lifecycle, including creation, modification, status management, and various task
 * operations.</p>
 *
 * <p>It handles the complete task lifecycle from creation to completion,
 * including sprint management, assignment, confirmation, workload tracking, and activity logging
 * for audit purposes.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Task CRUD operations with comprehensive validation</li>
 *   <li>Task status management (start, cancel, process, confirm, restart, reopen)</li>
 *   <li>Sprint management and task movement</li>
 *   <li>Assignment and confirmation management</li>
 *   <li>Workload tracking (evaluated and actual)</li>
 *   <li>Task associations (subtasks, related tasks, cases)</li>
 *   <li>Import/export functionality with example templates</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Permission and authorization management</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
public class TaskCmdImpl extends CommCmd<Task, Long> implements TaskCmd {

  @Resource
  private TaskRepo taskRepo;
  @Resource
  private TaskRemarkRepo taskRemarkRepo;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private SoftwareVersionQuery softwareVersionQuery;
  @Resource
  private SoftwareVersionCmd softwareVersionCmd;
  @Resource
  private TagQuery tagQuery;
  @Resource
  private ModuleQuery moduleQuery;
  @Resource
  private TaskFuncCaseCmd taskFuncCaseCmd;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private TaskSprintQuery taskSprintQuery;
  @Resource
  private TaskSprintCmd taskSprintCmd;
  @Resource
  private TaskSprintAuthQuery taskSprintAuthQuery;
  @Resource
  private TagTargetCmd tagTargetCmd;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private TaskTrashCmd trashTaskCmd;
  @Resource
  private UserManager userManager;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new task with comprehensive validation and setup.
   *
   * <p>This method creates a new task with extensive validation including
   * parent task relationships, sprint assignment, project membership, and permission checks.</p>
   *
   * <p>The method automatically handles parent-child task relationships,
   * sprint inheritance, and backlog management.</p>
   *
   * @param task the task to add
   * @return the ID key of the created task
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Task task) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;
      TaskInfo taskParentDb;

      @Override
      protected void checkParams() {
        // Verify parent task exists and handle inheritance
        if (nonNull(task.getParentTaskId())) {
          taskParentDb = taskQuery.checkAndFindInfo(task.getParentTaskId());
          // TODO: Verify no circular references in parent-child relationships to prevent errors in progress calculation and Gantt chart display
          // Inherit sprint and backlog settings from parent task
          task.setSprintId(taskParentDb.getSprintId())
              .setBacklog(taskParentDb.getBacklog());
        }

        if (task.getBacklog() || isNull(task.getSprintId())) {
          // Verify project ID is provided for backlog tasks
          assertTrue(nonNull(task.getProjectId()), "Backlog project id is required");
          // Verify user has project membership permissions
          projectMemberQuery.checkMember(getUserId(), task.getProjectId());
        } else {
          // Verify sprint exists for non-backlog tasks
          sprintDb = taskSprintQuery.checkAndFind(task.getSprintId());

          // Verify user has task creation permissions
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), sprintDb.getId());
        }

        // Verify module exists if specified
        if (nonNull(task.getModuleId()) && !Objects.equals(task.getModuleId(), -1L)) {
          moduleQuery.checkAndFind(task.getModuleId());
        }

        // TODO: Verify targetId is required for API or scenario test tasks
        // if (task.isTestTask()) {
        //  assertNotNull(task.getTargetId(), TASK_ASSOC_TARGET_ID_REQUIRED);
        // }

        // Verify assignee exists if specified
        if (nonNull(task.getAssigneeId())) {
          userManager.checkExists(task.getAssigneeId());
        }

        // Verify confirmer exists if specified
        if (nonNull(task.getConfirmerId())) {
          userManager.checkExists(task.getConfirmerId());
        }

        // Verify reference tasks exist if specified
        if (isNotEmpty(task.getRefTaskIds())) {
          taskQuery.checkAndFind(task.getRefTaskIds());
        }

        // Verify tags exist if specified
        tagQuery.checkExists(task.getTagTargets());

        // Verify task name is unique within project/sprint
        taskQuery.checkAddNameExists(task.getProjectId(), sprintDb, task.getName());

        // TODO: Add evaluation workload validation if needed
        // ProtocolAssert.assertTrue(isNull(task.getEvalWorkload())
        //    || nonNull(task.getActualWorkload()), "Evaluation workload not set");

        // Verify quota limits are not exceeded
        taskQuery.checkQuota(task.getSprintId(), 1);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add tag associations if specified
        if (isNotEmpty(task.getTagTargets())) {
          tagTargetCmd.add0(task.getTagTargets());
        }

        // Assemble task information and save
        boolean isAgile = nonNull(sprintDb) || projectQuery.isAgile(task.getProjectId());
        TaskConverter.assembleAddTaskInfo(task, sprintDb, isAgile);
        IdKey<Long, Object> idKey = insert(task);

        // Save related tasks and use cases associations
        taskFuncCaseCmd.addAssoc(TASK, idKey.getId(), task.getRefTaskIds(), task.getRefCaseIds());

        // Log task creation activity
        activityCmd.add(toActivity(TASK, task, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates a task with comprehensive validation and change tracking.
   *
   * <p>This method updates a task with extensive validation including
   * permission checks, user verification, and change detection for activity logging.</p>
   *
   * <p>The method tracks various changes including assignments, confirmations,
   * descriptions, and other task properties for audit purposes.</p>
   *
   * @param task the task to update
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Task task) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase assigneeDb;
      UserBase confirmerDb;
      List<Tag> taskTagsDb;
      List<TaskInfo> refTasks;
      List<FuncCaseInfo> refCases;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFind(task.getId());

        // Verify user has task modification permissions
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // TODO: Verify no circular references in parent-child task relationships to prevent errors in progress calculation and Gantt chart display

        // Verify module exists if specified
        if (nonNull(task.getModuleId())) {
          moduleQuery.checkAndFind(task.getModuleId());
        }

        // Verify task name is unique within project/sprint
        taskQuery.checkUpdateNameExists(taskDb.getProjectId(), taskDb.getSprintId(),
            task.getName(), task.getId());

        // TODO: Add evaluation workload validation if needed
        // assertTrue(isNull(task.getActualWorkload())
        //    || nonNull(task.getEvalWorkload()), "Evaluation workload not set");

        // Verify assignee exists if specified
        assigneeDb = userManager.checkValidAndFindUserBase(task.getAssigneeId());

        // Verify confirmer exists if specified
        confirmerDb = userManager.checkValidAndFindUserBase(task.getConfirmerId());

        // Verify no circular references in parent-child relationships
        taskQuery.checkUpdateParentNotCircular(taskDb.getProjectId(), List.of(task));

        // Verify reference tasks exist if specified
        if (isNotEmpty(task.getRefTaskIds())) {
          refTasks = taskQuery.checkAndFindInfo(task.getRefTaskIds());
        }

        // Verify reference cases exist if specified
        if (isNotEmpty(task.getRefCaseIds())) {
          refCases = funcCaseQuery.checkAndFindInfo(task.getRefCaseIds());
        }

        // Verify tags exist if specified
        if (isNotEmpty(task.getTagTargets())) {
          taskTagsDb = tagQuery.checkAndFind(task.getTagTargets().stream()
              .map(TagTarget::getTagId).toList());
        }
      }

      @Override
      protected Void process() {
        // Detect changes before updating
        boolean hasModAssigness = nonNull(task.getAssigneeId())
            && !Objects.equals(taskDb.getAssigneeId(), task.getAssigneeId());
        boolean hasModConfirmer = nonNull(taskDb.getConfirmerId())
            && !Objects.equals(taskDb.getConfirmerId(), task.getConfirmerId());
        boolean hasModTags = task.hasTag()
            && tagQuery.hasModifyTag(task.getId(), task.getTagTargets());
        boolean hasModAttachments = isNotEmpty(task.getAttachments())
            && taskQuery.hasModifyAttachments(task.getAttachments(), taskDb);
        boolean hasModRefTasks = isNotEmpty(task.getRefTaskIds())
            && !collectionEquals(taskDb.getRefTaskIds(), task.getRefTaskIds());
        boolean hasModRefCases = isNotEmpty(task.getRefCaseIds())
            && !collectionEquals(taskDb.getRefCaseIds(), task.getRefCaseIds());

        // Update task tags if changed
        if (hasModTags) {
          tagTargetCmd.replaceTaskTags0(taskDb, task.getTagTargets());
        }

        // Create activity log for changes (null activity will be ignored if no changes)
        Activity activity = toModifyTaskActivity(false, hasModAssigness, hasModConfirmer,
            hasModTags, hasModAttachments, task, taskDb, assigneeDb, confirmerDb,
            taskTagsDb, hasModRefTasks, refTasks, hasModRefCases, refCases);
        activityCmd.add(activity);

        // Assemble and save task updates
        TaskConverter.assembleUpdateTask(task, taskDb);
        taskRepo.save(taskDb);

        // Update related tasks and use cases associations
        taskFuncCaseCmd.updateAssoc(TASK, taskDb.getId(), task.getRefTaskIds(),
            task.getRefCaseIds());

        // Send modification notification events
        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        if (hasModAssigness) {
          taskQuery.assembleAndSendModifyAssigneeNoticeEvent(taskDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces a task with comprehensive validation and change tracking.
   *
   * <p>This method performs a complete replacement of a task with extensive
   * validation including permission checks, user verification, and change detection for activity
   * logging.</p>
   *
   * <p>The method tracks various changes including assignments, confirmations,
   * descriptions, and other task properties for audit purposes.</p>
   *
   * @param task the task to replace
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Task task) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase assigneeDb;
      UserBase confirmerDb;
      List<Tag> taskTagsDb;
      List<TaskInfo> refTasks;
      List<FuncCaseInfo> refCases;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(task.getId());

        // Check the add task permission when none backlog
        if (nonNull(taskDb.getSprintId())) {
          taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
        }

        // Check the module exists
        if (nonNull(task.getModuleId())) {
          moduleQuery.checkAndFind(task.getModuleId());
        }

        // Check the update name exist
        taskQuery.checkUpdateNameExists(taskDb.getProjectId(), taskDb.getSprintId(),
            task.getName(), task.getId());

        // Evaluation workload not set
        // assertTrue(isNull(task.getActualWorkload())
        //    || nonNull(task.getEvalWorkload()), "Evaluation workload not set");

        // Check the assigner exists
        assigneeDb = userManager.checkValidAndFindUserBase(task.getAssigneeId());

        // Check the confirmers exists
        confirmerDb = userManager.checkValidAndFindUserBase(task.getConfirmerId());

        // Check the parent is not a circular reference
        taskQuery.checkUpdateParentNotCircular(taskDb.getProjectId(), List.of(task));

        // Check the reference task exists
        if (isNotEmpty(task.getRefTaskIds())) {
          refTasks = taskQuery.checkAndFindInfo(task.getRefTaskIds());
        }

        // Check the reference case exists
        if (isNotEmpty(task.getRefCaseIds())) {
          refCases = funcCaseQuery.checkAndFindInfo(task.getRefCaseIds());
        }

        // Check the tags exists
        if (isNotEmpty(task.getTagTargets())) {
          taskTagsDb = tagQuery.checkAndFind(task.getTagTargets().stream()
              .map(TagTarget::getTagId).toList());
        }
      }

      @Override
      protected Void process() {
        // Get existed status before replace
        boolean hasModAssigness = !Objects.equals(taskDb.getAssigneeId(), task.getAssigneeId());
        boolean hasModConfirmer = !Objects.equals(taskDb.getConfirmerId(), task.getConfirmerId());
        boolean hasModTags = tagQuery.hasModifyTag(task.getId(), task.getTagTargets());
        boolean hasModAttachments = taskQuery.hasModifyAttachments(task.getAttachments(), taskDb);
        boolean hasModRefTasks = !collectionEquals(taskDb.getRefTaskIds(), task.getRefTaskIds());
        boolean hasModRefCases = !collectionEquals(taskDb.getRefCaseIds(), task.getRefCaseIds());

        // Change task tags
        tagTargetCmd.replaceTaskTags0(taskDb, task.getTagTargets());

        // Do not log when parameter has not changed !!!  Null activity will ignored.
        Activity activity = toModifyTaskActivity(true, hasModAssigness, hasModConfirmer,
            hasModTags, hasModAttachments, task, taskDb, assigneeDb, confirmerDb,
            taskTagsDb, hasModRefTasks, refTasks, hasModRefCases, refCases);
        activityCmd.add(activity);

        // Save task
        TaskConverter.assembleReplaceTask(task, taskDb);
        taskRepo.save(taskDb);

        // Save related tasks and use cases
        taskFuncCaseCmd.replaceAssoc(TASK, taskDb.getId(), taskDb.getRefTaskIds(),
            taskDb.getRefCaseIds());

        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        if (hasModAssigness) {
          taskQuery.assembleAndSendModifyAssigneeNoticeEvent(taskDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Renames a task with validation and activity logging.
   *
   * <p>This method changes the name of a task after verifying
   * user permissions and name uniqueness within the project/sprint.</p>
   *
   * <p>The method logs name update activities for audit purposes.</p>
   *
   * @param id   the task ID to rename
   * @param name the new name for the task
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        assertNotNull(name, "Name is required");

        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        if (!taskDb.getName().equals(name)) {
          // Check the update name exist
          taskQuery.checkUpdateNameExists(taskDb.getProjectId(), taskDb.getSprintId(), name, id);
        }
      }

      @Override
      protected Void process() {
        if (!name.equals(taskDb.getName())) {
          taskDb.setName(name);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, NAME_UPDATED, name);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Moves tasks to a different sprint with comprehensive validation.
   *
   * <p>This method moves multiple tasks to a target sprint after verifying
   * user permissions for both source and target sprints.</p>
   *
   * <p>The method logs task movement activities and sends modification
   * notification events.</p>
   *
   * @param taskIds        the list of task IDs to move
   * @param targetSprintId the target sprint ID (null to move to backlog)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(List<Long> taskIds, Long targetSprintId) {
    new BizTemplate<Void>() {
      TaskSprint targetSprintDb;
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        if (nonNull(targetSprintId)) {
          targetSprintDb = taskSprintQuery.checkAndFind(targetSprintId);
          // Check the add task permission
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), targetSprintDb.getId());
        }

        tasksDb = taskQuery.checkAndFind(taskIds);

        // Check the modify task permission
        taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
            .filter(Objects::nonNull) // Backlog sprint id is empty
            .collect(Collectors.toSet()), TaskSprintPermission.MODIFY_TASK);
      }

      @Override
      protected Void process() {
        for (Task taskDb : tasksDb) {
          assembleMoveTask(targetSprintDb, taskDb);
        }
        taskRepo.saveAll(tasksDb);

        // Add move task activities
        List<Activity> activities = getActivities();
        activityCmd.addAll(activities);

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }

      @NotNull
      private List<Activity> getActivities() {
        List<Activity> activities;
        if (nonNull(targetSprintId)) {
          activities = toActivities(TASK, tasksDb, MOVED_TO,
              tasksDb.stream().map(s -> new Object[]{targetSprintDb.getName()})
                  .toList());
        } else {
          activities = toActivities(TASK, tasksDb, MOVED,
              tasksDb.stream().map(s -> new Object[]{TASK_SPRINT})
                  .toList());
        }
        return activities;
      }
    }.execute();
  }

  /**
   * Replaces task type with validation and activity logging.
   *
   * <p>This method changes the type of a task after verifying
   * user permissions. For bug type tasks, it automatically sets default bug level and missing bug
   * status.</p>
   *
   * <p>The method logs type update activities for audit purposes.</p>
   *
   * @param id   the task ID to change type for
   * @param type the new task type
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceType(Long id, String type) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        TaskType taskType = assertEnumOf(type, TaskType.class);

        if (!taskType.equals(taskDb.getTaskType())) {
          taskDb.setTaskType(taskType);
          if (taskType.isBug()) {
            taskDb.setBugLevel(BugLevel.DEFAULT).setMissingBug(false);
          } else {
            taskDb.setBugLevel(null).setMissingBug(null);
          }
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, TYPE_UPDATED, taskType);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces bug level for bug type tasks with validation.
   *
   * <p>This method changes the bug level of a task after verifying
   * the task is of bug type and user has modification permissions.</p>
   *
   * <p>The method logs bug level update activities for audit purposes.</p>
   *
   * @param id       the task ID to change bug level for
   * @param bugLevel the new bug level
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public void replaceBugLevel(Long id, String bugLevel) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        assertTrue(taskDb.getTaskType().isBug(), "Not a bug type task");

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        BugLevel level = assertEnumOf(bugLevel, BugLevel.class);

        if (!level.equals(taskDb.getBugLevel())) {
          taskDb.setBugLevel(level);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, BUG_LEVEL_UPDATED, level);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces missing bug status for bug type tasks with validation.
   *
   * <p>This method changes the missing bug status of a task after verifying
   * the task is of bug type and user has modification permissions.</p>
   *
   * <p>The method logs missing bug status update activities for audit purposes.</p>
   *
   * @param id         the task ID to change missing bug status for
   * @param missingBug the new missing bug status
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public void replaceMissingBug(Long id, Boolean missingBug) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        assertTrue(taskDb.getTaskType().isBug(), "Not a bug type task");

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (!missingBug.equals(taskDb.getMissingBug())) {
          taskDb.setMissingBug(missingBug);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb,
              missingBug ? MISSING_BUG_SET : MISSING_BUG_CLEAR);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task assignee with validation and activity logging.
   *
   * <p>This method changes the assignee of a task after verifying
   * user permissions and assignee existence.</p>
   *
   * <p>The method logs assignee update activities and sends
   * modification notification events.</p>
   *
   * @param id         the task ID to change assignee for
   * @param assigneeId the new assignee ID (null to clear assignee)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAssignees(Long id, Long assigneeId) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase userDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Check the assignee
        userDb = userManager.checkValidAndFindUserBase(assigneeId);
      }

      @Override
      protected Void process() {
        if (!Objects.equals(assigneeId, taskDb.getAssigneeId())) {
          taskDb.setAssigneeId(assigneeId);
          taskRepo.save(taskDb);

          Activity activity = nonNull(assigneeId)
              ? toActivity(TASK, taskDb, TASK_ASSIGNEE, userDb.getFullName())
              : toActivity(TASK, taskDb, TASK_ASSIGNEE_CLEAR);
          activityCmd.add(activity);

          // Add modification event
          //taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          taskQuery.assembleAndSendModifyAssigneeNoticeEvent(taskDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task confirmer with validation and activity logging.
   *
   * <p>This method changes the confirmer of a task after verifying
   * user permissions and confirmer existence.</p>
   *
   * <p>The method logs confirmer update activities and sends
   * modification notification events.</p>
   *
   * @param id          the task ID to change confirmer for
   * @param confirmerId the new confirmer ID (null to clear confirmer)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceConfirmers(Long id, Long confirmerId) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase userDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Check the assignee
        userDb = userManager.checkValidAndFindUserBase(confirmerId);
      }

      @Override
      protected Void process() {
        if (!Objects.equals(confirmerId, taskDb.getConfirmerId())) {
          taskDb.setConfirmerId(confirmerId);
          taskRepo.save(taskDb);

          Activity activity = nonNull(confirmerId)
              ? toActivity(TASK, taskDb, TASK_CONFIRMER, userDb.getFullName())
              : toActivity(TASK, taskDb, TASK_CONFIRMER_CLEAR);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task deadline with validation and activity logging.
   *
   * <p>This method changes the deadline of a task after verifying
   * user permissions. It automatically calculates overdue status based on the current time.</p>
   *
   * <p>The method logs deadline update activities and sends
   * modification notification events.</p>
   *
   * @param id       the task ID to change deadline for
   * @param deadline the new deadline (null to clear deadline)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceDeadline(Long id, LocalDateTime deadline) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (!Objects.equals(deadline, taskDb.getDeadlineDate())) {
          taskDb.setDeadlineDate(deadline)
              .setOverdue(nonNull(deadline) && deadline.isBefore(LocalDateTime.now()));
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, DEADLINE, deadline.format(DATE_TIME_FMT));
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task priority with validation and activity logging.
   *
   * <p>This method changes the priority of a task after verifying
   * user permissions.</p>
   *
   * <p>The method logs priority update activities and sends
   * modification notification events.</p>
   *
   * @param id       the task ID to change priority for
   * @param priority the new priority
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replacePriority(Long id, Priority priority) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (!priority.equals(taskDb.getPriority())) {
          taskDb.setPriority(priority);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, PRIORITY, priority);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task software version with validation and activity logging.
   *
   * <p>This method changes the software version of a task after verifying
   * user permissions and version uniqueness within the project.</p>
   *
   * <p>The method logs software version update activities for audit purposes.</p>
   *
   * @param id      the task ID to change software version for
   * @param version the new software version (null to clear version)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceSoftwareVersion(Long id, String version) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the version not exists
        if (isNotEmpty(version)) {
          softwareVersionQuery.checkNotExits(taskDb.getProjectId(), version);
        }

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (isEmpty(version)) {
          taskDb.setSoftwareVersion(null);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, SOFTWARE_VERSION_CLEAR, version);
          activityCmd.add(activity);

          // Add modification event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);

          return null;
        }
        if (!version.equals(taskDb.getSoftwareVersion())) {
          taskDb.setSoftwareVersion(version);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, SOFTWARE_VERSION_UPDATE, version);
          activityCmd.add(activity);

          // Add modification event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task evaluated workload with validation and activity logging.
   *
   * <p>This method changes the evaluated workload of a task after verifying
   * user permissions. It handles both setting and clearing workload values.</p>
   *
   * <p>The method logs workload update activities for audit purposes.</p>
   *
   * @param id           the task ID to change evaluated workload for
   * @param evalWorkload the new evaluated workload (null to clear)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceEvalWorkload(Long id, BigDecimal evalWorkload) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        // Clear story point
        if (isNull(evalWorkload)) {
          if (nonNull(taskDb.getEvalWorkload())) {
            // Record activity before modifying taskDb.setStoryPoint(null)
            Activity activity = toActivity(TASK, taskDb, EVAL_WORKLOAD_CLEAR,
                taskDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            taskDb.setEvalWorkload(null).setActualWorkload(null);
            taskRepo.save(taskDb);

            // Add modification event -> Non-primary event
            // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          }
          return null;
        }

        // Change value
        if (!evalWorkload.equals(taskDb.getEvalWorkload())) {
          taskDb.setEvalWorkload(evalWorkload);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, EVAL_WORKLOAD,
              taskDb.getEvalWorkloadMethod(), evalWorkload);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task actual workload with validation and activity logging.
   *
   * <p>This method changes the actual workload of a task after verifying
   * user permissions. It automatically sets evaluated workload if not present.</p>
   *
   * <p>The method logs workload update activities for audit purposes.</p>
   *
   * @param id             the task ID to change actual workload for
   * @param actualWorkload the new actual workload (null to clear)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceActualWorkload(Long id, BigDecimal actualWorkload) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Evaluation workload not set
        // ProtocolAssert.assertTrue(isNull(actualWorkload)
        //    || nonNull(taskDb.getEvalWorkload()), "Evaluation workload not set");
      }

      @Override
      protected Void process() {
        if (nonNull(actualWorkload) && isNull(taskDb.getEvalWorkload())) {
          taskDb.setEvalWorkload(actualWorkload);
        }

        // Clear story point
        if (Objects.isNull(actualWorkload)) {
          if (nonNull(taskDb.getActualWorkload())) {
            // Record activity before modifying taskDb.setStoryPoint(null)
            Activity activity = toActivity(TASK, taskDb, ACTUAL_WORKLOAD_CLEAR,
                taskDb.getEvalWorkloadMethod());
            activityCmd.add(activity);

            taskDb.setActualWorkload(null);
            taskRepo.save(taskDb);

            // Add modification event -> Non-primary event
            // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          }
          return null;
        }

        // Change value
        if (!actualWorkload.equals(taskDb.getActualWorkload())) {
          taskDb.setActualWorkload(actualWorkload);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, ACTUAL_WORKLOAD,
              taskDb.getEvalWorkloadMethod(), actualWorkload);
          activityCmd.add(activity);

          // Add modification event -> Non-primary event
          // taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task description with validation and activity logging.
   *
   * <p>This method changes the description of a task after verifying
   * user permissions. It handles both setting and clearing description.</p>
   *
   * <p>The method logs description update activities and sends
   * modification notification events.</p>
   *
   * @param id          the task ID to change description for
   * @param description the new description (null to clear)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceDescription(Long id, String description) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        // Clear story point
        if (isEmpty(description)) {
          if (isNotEmpty(taskDb.getDescription())) {
            // Record activity before modifying taskDb.setDescription(null)
            Activity activity = toActivity(TASK, taskDb, DESCRIPTION_CLEAR);
            activityCmd.add(activity);

            taskDb.setDescription(null);
            taskRepo.save(taskDb);

            // Add modification event
            taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          }
          return null;
        }

        // Change value
        if (!description.equals(taskDb.getDescription())) {
          taskDb.setDescription(description);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, DESCRIPTION_UPDATED);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  /**
   * Replaces task attachments with validation and activity logging.
   *
   * <p>This method changes the attachments of a task after verifying
   * user permissions. It handles both setting and clearing attachments.</p>
   *
   * <p>The method logs attachment update activities and sends
   * modification notification events.</p>
   *
   * @param id          the task ID to change attachments for
   * @param attachments the new attachments list (null to clear)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceAttachment(Long id, List<Attachment> attachments) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        // Clear attachments
        if (isEmpty(attachments)) {
          if (isNotEmpty(taskDb.getAttachments())) {
            // Record activity before modifying taskDb.setAttachmentsData(null)
            Activity activity = toActivity(TASK, taskDb,
                ActivityType.ATTACHMENT_CLEAR, taskDb.getAttachments().stream().map(x ->
                    "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                        + "</a>").collect(Collectors.joining(",")));
            activityCmd.add(activity);

            taskDb.setAttachments(null);
            taskRepo.save(taskDb);

            // Add modification event
            taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          }
          return null;
        }

        // Change value
        if (taskQuery.hasModifyAttachments(attachments, taskDb)) {
          taskDb.setAttachments(attachments);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb,
              ActivityType.ATTACHMENT_UPDATED, attachments.stream().map(x ->
                  "<a data-type=\"attachment\" data-href=\"" + x.getUrl() + "\">" + x.getName()
                      + "</a>").collect(Collectors.joining(",")));
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

  /**
   * Starts a task with status validation and activity logging.
   *
   * <p>This method changes a task status to IN_PROGRESS after verifying
   * the current status allows starting and user has modification permissions.</p>
   *
   * <p>The method increments the total execution count and logs
   * task start activities for audit purposes.</p>
   *
   * @param id the task ID to start
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void start(Long id) {
    new BizTemplate<Void>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFind(id);

        // Verify user has task modification permissions
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Verify task is in pending status
        if (!taskDb.getStatus().isPending()) {
          throw BizException.of(TASK_START_NO_PENDING_CODE, TASK_START_NO_PENDING);
        }
      }

      @Override
      protected Void process() {
        taskDb.setId(id).setStatus(TaskStatus.IN_PROGRESS)
            .setStartDate(LocalDateTime.now())
            // Increment 1 when the task enters the start processing status.
            .setTotalNum(taskDb.getTotalNum() + 1);
        taskRepo.save(taskDb);

        Activity activity = toActivity(TASK, taskDb, ActivityType.TASK_START);
        activityCmd.add(activity);

        // Add modification event
        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancels a task with status validation and activity logging.
   *
   * <p>This method changes a task status to CANCELED after verifying
   * user has modification permissions.</p>
   *
   * <p>The method logs task cancellation activities and sends
   * modification notification events.</p>
   *
   * @param id the task ID to cancel
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancel(Long id) {
    new BizTemplate<Void>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the add task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (!taskDb.getStatus().isCanceled()) {
          taskDb.setId(id).setStatus(TaskStatus.CANCELED).setCanceledDate(LocalDateTime.now());
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, ActivityType.TASK_CANCEL);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Marks a task as processed with comprehensive validation.
   *
   * <p>This method changes a task status based on whether confirmation
   * is required, after verifying user permissions and task status.</p>
   *
   * <p>The method handles both confirmation-required and direct completion
   * scenarios with appropriate status transitions.</p>
   *
   * @param id the task ID to mark as processed
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void processed(Long id) {
    new BizTemplate<Void>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFind(id);

        // Verify user has task modification permissions
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Verify assignee has permission to process the task
        taskQuery.checkAssigneeUserPermission(taskDb);

        // Verify task is in progress status
        if (!taskDb.getStatus().isInProgress()) {
          throw BizException.of(TASK_NO_PROCESSING_CODE, TASK_NO_PROCESSING);
        }
      }

      @Override
      protected Void process() {
        if (taskDb.isConfirmTask() && !taskDb.getStatus().isConfirming()) {
          taskDb.setStatus(TaskStatus.CONFIRMING).setProcessedDate(LocalDateTime.now());
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, TASK_PROCESSED);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
          taskQuery.assembleAndSendPendingConfirmationNoticeEvent(taskDb);
          return null;
        }

        if (!taskDb.isConfirmTask() && !taskDb.getStatus().isCompleted()) {
          LocalDateTime now = LocalDateTime.now();
          taskDb.setStatus(TaskStatus.COMPLETED).setProcessedDate(now).setCompletedDate(now);
          taskDb.setEvalWorkload(nullSafe(taskDb.getEvalWorkload(), taskDb.getActualWorkload()));
          taskDb.setActualWorkload(nullSafe(taskDb.getActualWorkload(), taskDb.getEvalWorkload()));
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, TASK_COMPLETED);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Confirms a task with result and workload validation.
   *
   * <p>This method confirms a task with the specified result and workload
   * values after verifying user permissions and task status.</p>
   *
   * <p>The method handles success and failure scenarios with appropriate
   * status transitions and failure count management.</p>
   *
   * @param id             the task ID to confirm
   * @param result         the confirmation result (success/failure)
   * @param evalWorkload   the evaluated workload value
   * @param actualWorkload the actual workload value
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void confirm(Long id, Result result, BigDecimal evalWorkload, BigDecimal actualWorkload) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Verify task exists and retrieve task info
        taskDb = taskQuery.checkAndFind(id);

        // Verify user has task modification permissions
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Verify evaluation workload is set when actual workload is provided
        assertTrue(isNull(actualWorkload)
            || nonNull(evalWorkload), TASK_EVAL_WORKLOAD_NOT_SET);

        // Verify subtasks are completed for successful results
        if (result.isSuccess()) {
          taskQuery.checkSubTasksIsCompleted(taskDb.getProjectId(), id);
        }

        boolean isConfirmTask = taskDb.isConfirmTask();
        if (isConfirmTask) {
          // Verify confirmer has permission to confirm the task
          taskQuery.checkConfirmerUserPermission(taskDb);
        }

        // Verify non-confirmation tasks must be in progress
        if (!isConfirmTask && !taskDb.getStatus().isInProgress()) {
          throw BizException.of(TASK_NO_PROCESSING_CODE, TASK_NO_PROCESSING);
        }

        // Verify confirmation tasks must be in confirming status
        if (isConfirmTask && !taskDb.getStatus().isConfirming()) {
          throw BizException.of(TASK_NO_CONFIRMING_CODE, TASK_NO_CONFIRMING);
        }
      }

      @Override
      protected Void process() {
        LocalDateTime now = LocalDateTime.now();
        if (result.isSuccess()) {
          taskDb.setStatus(TaskStatus.COMPLETED).setCompletedDate(now);
        } else {
          // Increment 1 when the task confirmation fails;
          taskDb.setStatus(TaskStatus.PENDING).setFailNum(taskDb.getFailNum() + 1);
        }
        taskDb.setEvalWorkload(evalWorkload);
        taskDb.setActualWorkload(isNull(evalWorkload) ? null : actualWorkload);
        taskDb.setConfirmedDate(now);
        taskRepo.save(taskDb);

        Activity activity = toActivity(TASK, taskDb, TASK_CONFIRM_RESULT, result);
        activityCmd.add(activity);

        // Add modification event
        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        return null;
      }
    }.execute();
  }

  /**
   * Restarts tasks with comprehensive cleanup and status reset.
   *
   * <p>This method updates test tasks to pending status based on existing
   * test records and cleans up historical test statistics and status.</p>
   *
   * <p>The method resets task execution data and logs restart activities
   * for audit purposes.</p>
   *
   * @param taskIds the list of task IDs to restart
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void restart(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Verify test tasks exist and retrieve task info
        tasksDb = taskRepo.findAllById(taskIds);
        taskQuery.checkTaskExists(taskIds, tasksDb);

        // Verify user has restart task permissions
        taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.RESTART_TASK);
      }

      @Override
      protected Void process() {
        tasksDb = tasksDb.stream().map(TaskConverter::toRestartTask).toList();
        taskRepo.saveAll(tasksDb);

        List<Activity> activities = toActivities(TASK, tasksDb, TASK_RESTART);
        activityCmd.addAll(activities);

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Reopens tasks without clearing historical data.
   *
   * <p>This method updates test tasks to pending status based on existing
   * test records without clearing historical test statistics and status.</p>
   *
   * <p>The method preserves execution history and logs reopen activities
   * for audit purposes.</p>
   *
   * @param taskIds the list of task IDs to reopen
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reopen(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Verify test tasks exist and retrieve task info
        tasksDb = taskRepo.findAllById(taskIds);
        taskQuery.checkTaskExists(taskIds, tasksDb);

        // Verify user has reopen task permissions
        taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.REOPEN_TASK);

        // Verify tasks are not already opened
        taskQuery.checkTaskOpenStatus(tasksDb);
      }

      @Override
      protected Void process() {
        tasksDb = tasksDb.stream().map(TaskConverter::toReopenTask).toList();
        taskRepo.saveAll(tasksDb);

        List<Activity> activities = toActivities(TASK, tasksDb, TASK_REOPEN);
        activityCmd.addAll(activities);

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Sets subtasks for a task with validation and activity logging.
   *
   * <p>This method establishes parent-child relationships between tasks
   * after verifying user permissions and task existence.</p>
   *
   * <p>The method logs subtask association activities for audit purposes.</p>
   *
   * @param id         the parent task ID
   * @param subTaskIds the set of subtask IDs to associate
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void subtaskSet(Long id, HashSet<Long> subTaskIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<TaskInfo> subTasksDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the subtasks exists
        subTasksDb = taskQuery.checkAndFindInfo(subTaskIds);
        // Check the update task permission
        taskSprintAuthQuery.batchCheckPermission(subTasksDb.stream().map(TaskInfo::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.MODIFY_TASK);
      }

      @Override
      protected Void process() {
        taskRepo.updateTaskParent(id, subTaskIds);

        Activity activity = toActivity(TASK, taskDb, TASK_SUB_SET,
            subTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancels subtask associations for a task with validation and activity logging.
   *
   * <p>This method removes parent-child relationships between tasks
   * after verifying user permissions and task existence.</p>
   *
   * <p>The method logs subtask disassociation activities for audit purposes.</p>
   *
   * @param id         the parent task ID
   * @param subTaskIds the set of subtask IDs to disassociate
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void subtaskCancel(Long id, HashSet<Long> subTaskIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<TaskInfo> subTasksDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the subtasks exists
        subTasksDb = taskQuery.checkAndFindInfo(subTaskIds);
        // Check the update task permission
        taskSprintAuthQuery.batchCheckPermission(subTasksDb.stream().map(TaskInfo::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.MODIFY_TASK);
      }

      @Override
      protected Void process() {
        taskRepo.cancelTaskParent(id, subTaskIds);

        Activity activity = toActivity(TASK, taskDb, TASK_SUB_CANCEL,
            subTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Adds task associations with validation and activity logging.
   *
   * <p>This method establishes associations between tasks
   * after verifying user permissions and task existence.</p>
   *
   * <p>The method logs task association activities for audit purposes.</p>
   *
   * @param id           the task ID to add associations for
   * @param assocTaskIds the set of associated task IDs to add
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocAdd(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the association tasks exists
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);
        // Check the update task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.addAssoc(TASK, id, assocTaskIds, null);

        Activity activity = toActivity(TASK, taskDb, ASSOC_TASK,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancels task associations with validation and activity logging.
   *
   * <p>This method removes associations between tasks
   * after verifying user permissions and task existence.</p>
   *
   * <p>The method logs task disassociation activities for audit purposes.</p>
   *
   * @param id           the task ID to remove associations for
   * @param assocTaskIds the set of associated task IDs to remove
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void taskAssocCancel(Long id, HashSet<Long> assocTaskIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<TaskInfo> assocTasksDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the association tasks exists
        assocTasksDb = taskQuery.checkAndFindInfo(assocTaskIds);
        // Check the update task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.deleteAssoc(TASK, id, assocTaskIds, null);

        Activity activity = toActivity(TASK, taskDb, ASSOC_TASK_CANCEL,
            assocTasksDb.stream().map(TaskInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Adds functional case associations with validation and activity logging.
   *
   * <p>This method establishes associations between tasks and functional cases
   * after verifying user permissions and case existence.</p>
   *
   * <p>The method logs case association activities for audit purposes.</p>
   *
   * @param id           the task ID to add case associations for
   * @param assocCaseIds the set of associated case IDs to add
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocAdd(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the association cases exists
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);
        // Check the update task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.addAssoc(TASK, id, null, assocCaseIds);

        Activity activity = toActivity(TASK, taskDb, ASSOC_CASE,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Cancels functional case associations with validation and activity logging.
   *
   * <p>This method removes associations between tasks and functional cases
   * after verifying user permissions and case existence.</p>
   *
   * <p>The method logs case disassociation activities for audit purposes.</p>
   *
   * @param id           the task ID to remove case associations for
   * @param assocCaseIds the set of associated case IDs to remove
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void caseAssocCancel(Long id, HashSet<Long> assocCaseIds) {
    new BizTemplate<Void>() {
      TaskInfo taskDb;
      List<FuncCaseInfo> assocCasesDb;

      @Override
      protected void checkParams() {
        // Check the task exists
        taskDb = taskQuery.checkAndFindInfo(id);
        // Check the association cases exists
        assocCasesDb = funcCaseQuery.checkAndFindInfo(assocCaseIds);
        // Check the update task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        taskFuncCaseCmd.deleteAssoc(TASK, id, null, assocCaseIds);

        Activity activity = toActivity(TASK, taskDb, ASSOC_CASE_CANCEL,
            assocCasesDb.stream().map(FuncCaseInfo::getName).collect(Collectors.joining(",")));
        activityCmd.add(activity);
        return null;
      }
    }.execute();
  }

  /**
   * Imports tasks from Excel file with comprehensive validation.
   *
   * <p>This method imports tasks from an Excel file with extensive validation
   * including required columns, data integrity, and duplicate handling.</p>
   *
   * <p>The method supports different import strategies and handles
   * both agile and general project management scenarios.</p>
   *
   * @param projectId              the project ID to import tasks into
   * @param sprintId               the sprint ID to assign tasks to (can be null for backlog)
   * @param strategyWhenDuplicated the strategy for handling duplicate tasks
   * @param file                   the Excel file containing task data
   * @return the list of created task ID keys
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long projectId, @Nullable Long sprintId,
      StrategyWhenDuplicated strategyWhenDuplicated, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Project projectDb;
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify project exists and retrieve project info
        projectDb = projectQuery.checkAndFind(projectId);
        if (projectDb.isAgile() && nonNull(sprintId)) { // Agile Project Management
          // Verify task sprint exists and retrieve sprint info
          sprintDb = taskSprintQuery.checkAndFind(sprintId);
          // Verify user has add task permissions
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), sprintId);
        } else { // General Project Management
          // Verify user has project member permissions
          projectMemberQuery.checkMember(getUserId(), projectId);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Parsing imported file
        List<String[]> rows;
        try {
          rows = PoiUtils.readExcel(file.getInputStream());
        } catch (IOException e) {
          throw ProtocolException.of("Failed to read excel file, cause: " + e.getMessage());
        }
        assertNotEmpty(rows, "Read excel content is empty");

        // Check the for empty header fields
        List<String> titles = Stream.of(rows.get(0))
            .map(x -> StringUtils.remove(stringSafe(x), "*")).toList();
        assertTrue(titles.stream().noneMatch(ObjectUtils::isEmpty), "Title has empty value name");

        // Check the required import columns exist
        String missingRequiredField = TASK_IMPORT_REQUIRED_COLUMNS.stream()
            .filter(x -> !titles.contains(x)).findFirst().orElse(null);
        assertTrue(isEmpty(missingRequiredField),
            String.format("The required field %s is missing", missingRequiredField));

        List<String[]> data = rows.subList(1, rows.size());
        assertNotEmpty(data, "Read task data is empty");

        int nameIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(0));
        int taskTypeIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(1));
        int bugLevelIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(2));
        int assigneeIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(3));
        int confirmerIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(4));
        int testerIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(5));
        int missingBugIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(6));
        int unplannedIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(7));
        int priorityIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(8));
        int deadlineIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(9));
        int moduleIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(10));
        int descriptionIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(11));
        int evalWorkloadIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(12));
        int actualWorkloadIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(13));
        int statusIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(14));
        int softwareVersionIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(15));
        int startDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(16));
        int processedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(17));
        int canceledDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(18));
        int conformedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(19));
        int completedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(20));
        int tagsIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(21));
        int tasksIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(22));
        int casesIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(23));
        int creatorIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(24));
        int createdDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(25));
        //int targetIdIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(26));

        // Check the required import column values exist

        // Check the for duplicate task names
        assertTrue(nameIdx != -1, "Task name is required");
        List<String> names = data.stream().map(x -> x[nameIdx]).toList();
        List<String> duplicateNames = names.stream().filter(duplicateByKey(x -> x))
            .toList();
        assertTrue(isEmpty(duplicateNames),
            String.format("There are duplicates in the import task, duplicate task name: %s",
                duplicateNames));
        boolean hasEmptyName = names.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyName, "The import task name cannot be empty");

        assertTrue(taskTypeIdx != -1, "Task type is required");
        List<String> taskTypes = data.stream().map(x -> x[taskTypeIdx])
            .toList();
        boolean hasEmptyTaskTypes = taskTypes.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyTaskTypes, "The import task type cannot be empty");

        // Check the assignee exist
        Set<String> assignees = data.stream()
            .filter(x -> assigneeIdx != -1 && isNotBlank(x[assigneeIdx]))
            .map(x -> x[assigneeIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> assigneeMap = userManager.checkValidAndFindUserBasesByName(
            assignees);

        //assertTrue(deadlineIdx != -1, "Task deadline date is required");
        //List<String> deadlines = data.stream().map(x -> x[deadlineIdx])
        //    .toList();
        //boolean hasEmptyDeadlines = deadlines.stream().anyMatch(ObjectUtils::isEmpty);
        //assertTrue(!hasEmptyDeadlines, "The import deadline date cannot be empty");

        // Check the confirmer exist
        Set<String> confirmers = data.stream()
            .filter(x -> confirmerIdx != -1 && isNotBlank(x[confirmerIdx]))
            .map(x -> x[confirmerIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> confirmerMap = userManager.checkValidAndFindUserBasesByName(
            confirmers);
        // Check the tester exist
        Set<String> testers = data.stream()
            .filter(x -> testerIdx != -1 && isNotBlank(x[testerIdx]))
            .map(x -> x[testerIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> testersMap = userManager.checkValidAndFindUserBasesByName(
            testers);
        // Check the module exist
        Set<String> modules = data.stream()
            .filter(x -> moduleIdx != -1 && isNotBlank(x[moduleIdx]))
            .map(x -> x[moduleIdx]).collect(Collectors.toSet());
        Map<String, Module> modulesMap = moduleQuery.checkAndFindByName(projectId, modules);
        // Check the creators exist
        Set<String> creators = data.stream()
            .filter(x -> creatorIdx != -1 && isNotBlank(x[creatorIdx]))
            .map(x -> x[creatorIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> creatorsMap = userManager.checkValidAndFindUserBasesByName(
            creators);
        // Check the associated tags exist
        Set<String> tags = data.stream().filter(x -> tagsIdx != -1 && isNotBlank(x[tagsIdx]))
            .map(x -> List.of(x[tagsIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<Tag>> tagsMap = tagQuery.checkAndFindByName(projectId, tags);
        // Check the associated tasks exist
        Set<String> taskNames = data.stream()
            .filter(x -> tasksIdx != -1 && isNotBlank(x[tasksIdx]))
            .map(x -> List.of(x[tasksIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<TaskInfo>> tasksMap = taskQuery.checkAndFindByPlanAndName(
            sprintId, taskNames);
        // Check the associated cases exist
        Set<String> caseNames = data.stream()
            .filter(x -> casesIdx != -1 && isNotBlank(x[casesIdx]))
            .map(x -> List.of(x[casesIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<FuncCaseInfo>> casesMap = funcCaseQuery.checkAndFindByPlanAndName(
            sprintId, caseNames);

        // Check the associated testing targets exist

        // Format import fields and convert them into task objects
        List<Task> tasks = importToDomain(uidGenerator, projectDb, sprintDb,
            data, nameIdx, taskTypeIdx, bugLevelIdx,
            assigneeMap, assigneeIdx, confirmerIdx, confirmerMap, testerIdx, testersMap,
            missingBugIdx, unplannedIdx, priorityIdx, deadlineIdx, descriptionIdx,
            evalWorkloadIdx, actualWorkloadIdx, statusIdx, softwareVersionIdx,
            startDateIdx, processedDateIdx, canceledDateIdx, conformedDateIdx, completedDateIdx,
            creatorIdx, creatorsMap, createdDateIdx, tagsIdx, tagsMap, tasksIdx, tasksMap, casesIdx,
            casesMap, moduleIdx, modulesMap);

        // @DoInFuture("Check if the associated testing targets exist")
        // @DoInFuture("Check if the associated task targets exist")

        // When using an `COVER` strategy, delete existing tasks, otherwise ignore duplicate import tasks
        Set<String> safePrefixNames = isNull(sprintDb) || isEmpty(sprintDb.getTaskPrefix())
            ? new HashSet<>(names) : names.stream().map(x -> sprintDb.getTaskPrefix() + x)
            .collect(Collectors.toSet());
        if (strategyWhenDuplicated.isCover()) {
          taskRepo.deleteBySprintIdAndNameIn(sprintId, safePrefixNames);
        } else {
          List<String> namesDb = taskRepo.findNameBySprintIdAndNameIn(sprintId, safePrefixNames);
          tasks = tasks.stream().filter(x -> !namesDb.contains(x.getName()))
              .toList();
        }

        if (isEmpty(tasks)) {
          return null;
        }

        // Save imported tasks
        return tasks.stream().map(x -> add(x)).toList();
      }
    }.execute();
  }

  /**
   * Imports example tasks with sample data for project setup.
   *
   * <p>This method creates example tasks with sample data including
   * sprints, software versions, and task templates for project initialization.</p>
   *
   * <p>Note: When API calls are not user-initiated, tenant and user information
   * must be injected into the PrincipalContext.</p>
   *
   * @param projectId the project ID to import example tasks into
   * @return the list of created task ID keys
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Verify project exists and retrieve project info
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // 0. Query all tenant users
        List<User> users = userManager.findValidByTenantId(getOptTenantId());
        Assert.assertNotEmpty(users, "Tenant users are empty");

        // 1. If it is an agile project, create sprint by sample file
        TaskSprint sprint = null;
        if (projectDb.isAgile()) {
          URL resourceUrl = this.getClass().getResource("/samples/sprint/"
              + getDefaultLanguage().getValue() + "/" + SAMPLE_SPRINT_FILE);
          sprint = parseSample(Objects.requireNonNull(resourceUrl),
              new TypeReference<TaskSprint>() {
              }, SAMPLE_SPRINT_FILE);
          assembleExampleTaskSprint(projectDb, users, sprint);
          taskSprintCmd.add(sprint);
        }

        // 2. Create version: v1.0
        SoftwareVersion version = assembleExampleTaskSoftwareVersion(projectDb,
            uidGenerator.getUID(), users, sprint);
        softwareVersionCmd.add(version);

        // 3. Create task by sample file
        URL resourceUrl = this.getClass().getResource("/samples/task/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_TASK_FILE);
        List<Task> tasks = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<List<Task>>() {
            }, SAMPLE_TASK_FILE);
        for (Task task : tasks) {
          assembleExampleTask(projectDb, task, sprint, users);
        }
        return tasks.stream().map(x -> add(x)).toList();
      }
    }.execute();
  }

  /**
   * Deletes tasks with logical deletion and cleanup.
   *
   * <p>This method performs logical deletion of tasks after verifying
   * user has deletion permissions. It moves tasks to trash and updates deletion status.</p>
   *
   * <p>The method logs deletion activities and sends modification
   * notification events.</p>
   *
   * @param taskIds the list of task IDs to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Verify test tasks exist and retrieve task info
        tasksDb = taskRepo.findAllById(taskIds);
        // taskQuery.checkTaskExists(taskIds, tasksDb);

        // Verify user has delete task permissions
        if (isNotEmpty(tasksDb)) {
          taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
                  .filter(Objects::nonNull).collect(Collectors.toSet()),
              TaskSprintPermission.DELETE_TASK);
        }
      }

      @Override
      protected Void process() {
        if (isEmpty(tasksDb)) {
          return null;
        }

        // Update task deletion status
        taskRepo.updateDeleteStatus(taskIds, true, getUserId(), LocalDateTime.now());

        // Log deletion activities
        List<Activity> activities = toActivities(TASK, tasksDb, DELETED, activityParams(tasksDb));
        activityCmd.addAll(activities);

        // Move deleted tasks to trash
        trashTaskCmd.add0(tasksDb.stream().map(TaskConverter::toTaskTrash)
            .toList());

        // Send modification notification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Retests tasks by target with optional restart functionality.
   *
   * <p>This method updates test tasks to pending status based on existing
   * test records, with optional clearing of historical test statistics and status depending on the
   * restart parameter.</p>
   *
   * @param restart whether to restart (clear history) or reopen (preserve history)
   * @param tasksDb the list of tasks to retest
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void retest0ByTarget(Boolean restart, List<Task> tasksDb) {
    List<Task> retestTaskDbs;
    if (restart) {
      retestTaskDbs = tasksDb.stream().map(TaskConverter::toRestartTask).toList();
    } else {
      retestTaskDbs = tasksDb.stream().map(o -> o.setStatus(TaskStatus.PENDING)).toList();
    }

    taskRepo.saveAll(retestTaskDbs);

    activityCmd.addAll(toActivities(TASK, tasksDb, restart ? TASK_RESTART : TASK_REOPEN));
  }

  /**
   * Permanently deletes tasks by target with cascade cleanup (internal use).
   *
   * <p>This method performs permanent deletion of tasks and all associated
   * data including remarks, tags, and functional case associations.</p>
   *
   * <p>Note: This method is intended for internal use and should be called
   * after proper validation and permission checks.</p>
   *
   * @param taskIds the list of task IDs to permanently delete
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByTarget(List<Long> taskIds) {
    List<Task> tasksDb = taskRepo.findAllById(taskIds);

    taskRemarkRepo.deleteByTaskIdIn(taskIds);
    tagTargetCmd.deleteByTargetIdIn(taskIds);
    taskFuncCaseCmd.deleteByTargetIds(taskIds);

    taskRepo.deleteByIdIn(taskIds);

    // Save delete activity
    activityCmd.addAll(toActivities(TASK, tasksDb, DELETED));
  }

  /**
   * Permanently deletes tasks with cascade cleanup (internal use).
   *
   * <p>This method performs permanent deletion of tasks and all associated
   * data including remarks, tags, and functional case associations.</p>
   *
   * <p>Note: This method is intended for internal use and should be called
   * after proper validation and permission checks.</p>
   *
   * @param taskIds the list of task IDs to permanently delete
   */
  @Override
  public void delete0(List<Long> taskIds) {
    // Note: Delete associated data before deleting tasks
    taskRemarkRepo.deleteByTaskIdIn(taskIds);
    tagTargetCmd.deleteByTargetIdIn(taskIds);
    taskFuncCaseCmd.deleteByTargetIds(taskIds);

    taskRepo.deleteByIdIn(taskIds);
  }

  @Override
  public void add0(Task task) {
    TaskConverter.assembleAddTaskInfo(task, null, true);
    insert(task);
  }

  /**
   * Generates a unique task code for the current tenant.
   *
   * @return the generated task code
   */
  public static String getTaskCode() {
    return getBean(BidGenerator.class).getId(TASK_BID_KEY, getTenantId());
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task repository
   */
  @Override
  protected BaseRepository<Task, Long> getRepository() {
    return this.taskRepo;
  }
}
