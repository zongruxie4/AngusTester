package cloud.xcan.sdf.core.angustester.application.cmd.task.impl;


import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.TASK_BID_KEY;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toModifyTaskActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.assembleMoveTask;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.importToDomain;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.toAddApisOrScenarioTask;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_IMPORT_COLUMNS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_IMPORT_REQUIRED_COLUMNS;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_NO_CONFIRMING;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_NO_CONFIRMING_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_NO_PROCESSING;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_NO_PROCESSING_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_START_NO_PENDING;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_START_NO_PENDING_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_WEBSOCKET_NOT_SUPPORT_GEN_TASK;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ACTUAL_WORKLOAD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ACTUAL_WORKLOAD_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ASSOC_CASE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ASSOC_CASE_CANCEL;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ASSOC_TASK;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.ASSOC_TASK_CANCEL;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.BUG_LEVEL_UPDATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DEADLINE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DESCRIPTION_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DESCRIPTION_UPDATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.EVAL_WORKLOAD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.EVAL_WORKLOAD_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.MISSING_BUG_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.MISSING_BUG_SET;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.MOVED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.NAME_UPDATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.PRIORITY;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_ASSIGNEE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_ASSIGNEE_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_COMPLETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_CONFIRMOR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_CONFIRMOR_CLEAR;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_CONFIRM_RESULT;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_PROCESSED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_REOPEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_RESTART;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_SUB_CANCEL;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TASK_SUB_SET;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TYPE_UPDATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.VERSION;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.VERSION_CLEAR;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertEnumOf;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.DateUtils.DATE_TIME_FMT;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.collectionEquals;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskFuncCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskTrashCmd;
import cloud.xcan.sdf.core.angustester.application.converter.TaskConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSprintQuery;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemarkRepo;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.idgen.BidGenerator;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import cloud.xcan.sdf.spec.utils.StringUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Biz
public class TaskCmdImpl extends CommCmd<Task, Long> implements TaskCmd {

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private TaskRemarkRepo taskRemarkRepo;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private SoftwareVersionQuery taskVersionQuery;

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
  private TaskSprintAuthQuery taskSprintAuthQuery;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisAuthQuery apisAuthQuery;

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Task task) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;
      TaskInfo taskParentDb;

      @Override
      protected void checkParams() {
        // Check parent task exists
        if (nonNull(task.getParentTaskId())) {
          taskParentDb = taskQuery.checkAndFindInfo(task.getParentTaskId());
          // TODO Check if there is a loop in the parent-child task, prevent errors in progress calculation and Gantt chart display.
          // Safe sprint and backlog flag
          task.setSprintId(taskParentDb.getSprintId())
              .setBacklogFlag(taskParentDb.getBacklogFlag());
        }

        if (task.getBacklogFlag()) {
          // Check the project id is required to create a backlog
          assertTrue(nonNull(task.getProjectId()), "Backlog project id is required");
          // Check the project member permission
          projectMemberQuery.checkMember(task.getProjectId(), getUserId());
        } else {
          // Check the task sprint exists
          sprintDb = taskSprintQuery.checkAndFind(task.getSprintId());

          // Check the add task permission
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), sprintDb.getId());
        }

        // Check the module exists
        if (nonNull(task.getModuleId())) {
          moduleQuery.checkAndFind(task.getModuleId());
        }

        // Check the if the task is apis or scenario type, the parameter targetId is required
        // if (task.isTestTask()) {
        //  assertNotNull(task.getTargetId(), TASK_ASSOC_TARGET_ID_REQUIRED);
        // }

        if (task.isApiTest()) {
          // Check the associated apis exists
          // apisQuery.check(task.getTargetId());
          Long serviceId = apisQuery.checkAndFindBaseInfo(task.getTargetId()).getServiceId();
          task.setTargetParentId(serviceId);
        }

        // Check the assigner exists
        if (nonNull(task.getAssigneeId())) {
          userManager.checkExists(task.getAssigneeId());
        }

        // Check the confirmors exists
        if (nonNull(task.getConfirmorId())) {
          userManager.checkExists(task.getConfirmorId());
        }

        // Check the reference task exists
        if (isNotEmpty(task.getRefTaskIds())) {
          taskQuery.checkAndFind(task.getRefTaskIds());
        }

        // Check the tags exists
        tagQuery.checkExists(task.getTagTargets());

        // Check the name exists
        taskQuery.checkAddNameExists(sprintDb, task.getName());

        // NOOP:: Evaluation workload not set
        // ProtocolAssert.assertTrue(isNull(task.getEvalWorkload())
        //    || nonNull(task.getActualWorkload()), "Evaluation workload not set");

        // Check the target task exists
        if (nonNull(task.getTargetId())) {
          taskQuery.checkTargetTaskExists(task.getTargetId(), task.getTestType(),
              task.getTaskType());
        }

        // Check the quota limit
        taskQuery.checkQuota(task.getSprintId(), 1);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Report is not required
        if (isNotEmpty(task.getTagTargets())) {
          tagTargetCmd.add0(task.getTagTargets());
        }

        // Save tasks
        boolean isAgile = nonNull(sprintDb) || projectQuery.isAgile(task.getProjectId());
        TaskConverter.assembleAddTaskInfo(task, sprintDb, isAgile);
        IdKey<Long, Object> idKey = insert(task);

        // Save related tasks and use cases
        taskFuncCaseCmd.addAssoc(TASK, idKey.getId(), task.getRefTaskIds(), task.getRefCaseIds());

        activityCmd.add(toActivity(TASK, task, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Start and generate test tasks and update test info when it exists
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public Object generate(@Nullable Long sprintId, TaskType taskType, Long targetId,
      List<Task> tasks, boolean ignoreApisOrScenarioPermission) {
    return new BizTemplate<>() {
      Scenario scenarioDb = null;
      ApisBaseInfo apisDb = null;
      TaskSprint sprintDb = null;
      Long projectId = null;

      @Override
      protected void checkParams() {
        if (taskType.isApiTest()) {
          // Check the associated apis exists
          apisDb = apisQuery.checkAndFindBaseInfo(targetId);
          projectId = apisDb.getProjectId();
          BizAssert.assertTrue(!apisDb.isWebSocket(), TASK_WEBSOCKET_NOT_SUPPORT_GEN_TASK);
          // Check the apis test permission
          if (!ignoreApisOrScenarioPermission) { // Generate by apis services
            apisAuthQuery.checkTestAuth(getUserId(), targetId);
          }
        }

        if (taskType.isScenarioTest()) {
          // Check the associated scenario exists
          scenarioDb = scenarioQuery.checkAndFind0(targetId);
          projectId = scenarioDb.getProjectId();
          // Check the scenario test permission
          if (!ignoreApisOrScenarioPermission) { // Generate by scenario dir
            scenarioAuthQuery.checkTestAuth(getUserId(), targetId);
          }
        }

        if (nonNull(sprintId)) { // Agile Project Management
          // Check the task sprint exists
          sprintDb = taskSprintQuery.checkAndFind(sprintId);
          projectId = sprintDb.getProjectId();
          // Check the add task permission
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), sprintId);
        } else { // General Project Management
          // Check the add task permission
          projectMemberQuery.checkMember(projectId, getUserId());
        }
      }

      @Override
      protected Object process() {
        Map<TestType, Task> existedTaskMap = taskRepo.findAllBySprintIdAndTargetId(
            sprintId, targetId).stream().collect(Collectors.toMap(Task::getTestType, x -> x));
        List<Task> newTasks = new ArrayList<>();
        // No target test task
        if (existedTaskMap.isEmpty()) {
          newTasks = tasks.stream()
              .map(o -> toAddApisOrScenarioTask(projectId, sprintDb, apisDb, scenarioDb, o))
              .collect(Collectors.toList());
          activityCmd.batchAdd(toActivities(TASK, newTasks, ActivityType.TASK_GEN));
          batchInsert0(newTasks);
          return nonNull(apisDb) ? apisDb : scenarioDb;
        }

        // There are existing target test tasks
        List<Task> existedTasks = new ArrayList<>();
        for (Task task : tasks) {
          Task existedTask = existedTaskMap.get(task.getTestType());
          if (nonNull(existedTask)) {
            // Existing tasks can only be modified in the pending state
            if (existedTask.getStatus().isPending()) {
              // Override the configuration when the task exists
              // Update test information: priority, startDate, endDate
              existedTasks.add(copyPropertiesIgnoreNull(task, existedTask));
            }
            // Important Used by assignee associate task ID
            task.setId(existedTask.getId()).setName(existedTask.getName());
          } else {
            newTasks.add(toAddApisOrScenarioTask(projectId, sprintDb, apisDb, scenarioDb, task));
          }
        }

        // Save new task
        if (isNotEmpty(newTasks)) {
          activityCmd.batchAdd(toActivities(TASK, newTasks, ActivityType.TASK_GEN));
          batchInsert0(newTasks);
        }

        // Update existed task
        if (isNotEmpty(existedTasks)) {
          activityCmd.batchAdd(toActivities(TASK, newTasks, ActivityType.UPDATED));
          taskRepo.saveAll(existedTasks);
        }
        return nonNull(apisDb) ? apisDb : scenarioDb;
      }
    }.execute();
  }

  /**
   * Test task information that supports modification: name、priority、result、startDate、endDate.
   * <p>
   * Only assignees , creator, confirmor and admins are allowed to modify.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Task task) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase assigneeDb;
      UserBase confirmorDb;
      List<Tag> taskTagsDb;
      List<TaskInfo> refTasks;
      List<FuncCaseInfo> refCases;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(task.getId());

        // Check the add task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // TODO Check if there is a loop in the parent-child task, prevent errors in progress calculation and Gantt chart display.

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

        // Check the confirmors exists
        confirmorDb = userManager.checkValidAndFindUserBase(task.getConfirmorId());

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
              .map(TagTarget::getTagId).collect(Collectors.toList()));
        }
      }

      @Override
      protected Void process() {
        // Get existed status before replace
        boolean hasModAssigness = nonNull(task.getAssigneeId())
            && !Objects.equals(taskDb.getAssigneeId(), task.getAssigneeId());
        boolean hasModConfirmor = nonNull(taskDb.getConfirmorId())
            && !Objects.equals(taskDb.getConfirmorId(), task.getConfirmorId());
        boolean hasModTags = task.hasTag()
            && tagQuery.hasModifyTag(task.getId(), task.getTagTargets());
        boolean hasModAttachments = isNotEmpty(task.getAttachments())
            && taskQuery.hasModifyAttachments(task.getAttachments(), taskDb);
        boolean hasModRefTasks = isNotEmpty(task.getRefTaskIds())
            && !collectionEquals(taskDb.getRefTaskIds(), task.getRefTaskIds());
        boolean hasModRefCases = isNotEmpty(task.getRefCaseIds())
            && !collectionEquals(taskDb.getRefCaseIds(), task.getRefCaseIds());

        // Change task tags
        if (hasModTags) {
          tagTargetCmd.replaceTaskTags0(taskDb, task.getTagTargets());
        }

        // Do not log when parameter has not changed !!!  Null activity will ignored.
        Activity activity = toModifyTaskActivity(false, hasModAssigness, hasModConfirmor,
            hasModTags, hasModAttachments, task, taskDb, assigneeDb, confirmorDb,
            taskTagsDb, hasModRefTasks, refTasks, hasModRefCases, refCases);
        activityCmd.add(activity);

        // Save task
        TaskConverter.assembleUpdateTask(task, taskDb);
        taskRepo.save(taskDb);

        // Save related tasks and use cases
        taskFuncCaseCmd.updateAssoc(TASK, taskDb.getId(), task.getRefTaskIds(),
            task.getRefCaseIds());

        taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        if (hasModAssigness) {
          taskQuery.assembleAndSendModifyAssigneeNoticeEvent(taskDb);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Test task information that supports modification: name、priority、result、startDate、endDate.
   * <p>
   * Only assignees , creator, confirmor and admins are allowed to modify.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Task task) {
    new BizTemplate<Void>() {
      Task taskDb = null;
      UserBase assigneeDb;
      UserBase confirmorDb;
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

        // Check the confirmors exists
        confirmorDb = userManager.checkValidAndFindUserBase(task.getConfirmorId());

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
              .map(TagTarget::getTagId).collect(Collectors.toList()));
        }
      }

      @Override
      protected Void process() {
        // Get existed status before replace
        boolean hasModAssigness = !Objects.equals(taskDb.getAssigneeId(), task.getAssigneeId());
        boolean hasModConfirmor = !Objects.equals(taskDb.getConfirmorId(), task.getConfirmorId());
        boolean hasModTags = tagQuery.hasModifyTag(task.getId(), task.getTagTargets());
        boolean hasModAttachments = taskQuery.hasModifyAttachments(task.getAttachments(), taskDb);
        boolean hasModRefTasks = !collectionEquals(taskDb.getRefTaskIds(), task.getRefTaskIds());
        boolean hasModRefCases = !collectionEquals(taskDb.getRefCaseIds(), task.getRefCaseIds());

        // Change task tags
        tagTargetCmd.replaceTaskTags0(taskDb, task.getTagTargets());

        // Do not log when parameter has not changed !!!  Null activity will ignored.
        Activity activity = toModifyTaskActivity(true, hasModAssigness, hasModConfirmor,
            hasModTags, hasModAttachments, task, taskDb, assigneeDb, confirmorDb,
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long projectId, @Nullable Long sprintId,
      StrategyWhenDuplicated strategyWhenDuplicated, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        if (nonNull(sprintId)) { // Agile Project Management
          // Check the task sprint exists
          sprintDb = taskSprintQuery.checkAndFind(sprintId);
          // Check the add task permission
          taskSprintAuthQuery.checkAddTaskAuth(getUserId(), sprintId);
        } else { // General Project Management
          // Check the add task permission
          projectMemberQuery.checkMember(projectId, getUserId());
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Parsing imported file
        List<String[]> rows;
        try {
          rows = PoiUtils.readExcel(file.getInputStream());
        } catch (IOException e) {
          throw CommProtocolException.of("Failed to read excel file, cause: " + e.getMessage());
        }
        assertNotEmpty(rows, "Read excel content is empty");

        // Check the for empty header fields
        List<String> titles = Stream.of(rows.get(0))
            .map(x -> StringUtils.remove(stringSafe(x), "*")).collect(Collectors.toList());
        assertTrue(titles.stream().noneMatch(ObjectUtils::isEmpty), "Title has empty value name");

        // Check the if the required import columns exist
        String missingRequiredField = TASK_IMPORT_REQUIRED_COLUMNS.stream()
            .filter(x -> !titles.contains(x)).findFirst().orElse(null);
        assertTrue(isEmpty(missingRequiredField),
            String.format("The required field %s is missing", missingRequiredField));

        List<String[]> data = rows.subList(1, rows.size());
        assertNotEmpty(data, "Read task data is empty");

        int nameIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(0));
        int taskTypeIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(1));
        int bugLevelIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(2));
        int testTypeIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(3));
        int assigneeIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(4));
        int confirmorIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(5));
        int priorityIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(6));
        int deadlineIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(7));
        int moduleIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(8));
        int descriptionIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(9));
        int evalWorkloadIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(10));
        int actualWorkloadIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(11));
        int statusIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(12));
        int startDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(13));
        int processedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(14));
        int canceledDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(15));
        int conformedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(16));
        int completedDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(17));
        int tagsIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(18));
        int tasksIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(19));
        int casesIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(20));
        int creatorIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(21));
        int createdDateIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(22));
        //int targetIdIdx = titles.indexOf(TASK_IMPORT_COLUMNS.get(22));

        // Check the if the required import column values exist

        // Check the for duplicate task names
        assertTrue(nameIdx != -1, "Task name is required");
        List<String> names = data.stream().map(x -> x[nameIdx]).collect(Collectors.toList());
        List<String> duplicateNames = names.stream().filter(ObjectUtils.duplicateByKey(x -> x))
            .collect(Collectors.toList());
        assertTrue(isEmpty(duplicateNames),
            String.format("There are duplicates in the import task, duplicate task name: %s",
                duplicateNames));
        boolean hasEmptyName = names.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyName, "The import task name cannot be empty");

        assertTrue(taskTypeIdx != -1, "Task type is required");
        List<String> taskTypes = data.stream().map(x -> x[taskTypeIdx])
            .collect(Collectors.toList());
        boolean hasEmptyTaskTypes = taskTypes.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyTaskTypes, "The import task type cannot be empty");

        assertTrue(assigneeIdx != -1, "Task assignee is required");
        Set<String> assignees = data.stream().map(x -> x[assigneeIdx])
            .collect(Collectors.toSet());
        boolean hasEmptyAssignees = assignees.stream().anyMatch(ObjectUtils::isEmpty);
        assertTrue(!hasEmptyAssignees, "The import assignee cannot be empty");
        // Check the if the assignee exist
        Map<String, List<UserBase>> assigneeMap = userManager.checkValidAndFindUserBasesByName(
            assignees);

        //assertTrue(deadlineIdx != -1, "Task deadline date is required");
        //List<String> deadlines = data.stream().map(x -> x[deadlineIdx])
        //    .collect(Collectors.toList());
        //boolean hasEmptyDeadlines = deadlines.stream().anyMatch(ObjectUtils::isEmpty);
        //assertTrue(!hasEmptyDeadlines, "The import deadline date cannot be empty");

        // Check the if the confirmor exist
        Set<String> confirmors = data.stream()
            .filter(x -> confirmorIdx != -1 && isNotEmpty(x[confirmorIdx]))
            .map(x -> x[confirmorIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> confirmorMap = userManager.checkValidAndFindUserBasesByName(
            confirmors);
        // Check the if the module exist
        Set<String> modules = data.stream()
            .filter(x -> moduleIdx != -1 && isNotEmpty(x[moduleIdx]))
            .map(x -> x[moduleIdx]).collect(Collectors.toSet());
        Map<String, Module> modulesMap = moduleQuery.checkAndFindByName(projectId, modules);
        // Check the if the creators exist
        Set<String> creators = data.stream()
            .filter(x -> creatorIdx != -1 && isNotEmpty(x[creatorIdx]))
            .map(x -> x[creatorIdx]).collect(Collectors.toSet());
        Map<String, List<UserBase>> creatorsMap = userManager.checkValidAndFindUserBasesByName(
            creators);
        // Check the if the associated tags exist
        Set<String> tags = data.stream().filter(x -> tagsIdx != -1 && isNotEmpty(x[tagsIdx]))
            .map(x -> List.of(x[tagsIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<Tag>> tagsMap = tagQuery.checkAndFindByName(projectId, tags);
        // Check the if the associated tasks exist
        Set<String> taskNames = data.stream()
            .filter(x -> tasksIdx != -1 && isNotEmpty(x[tasksIdx]))
            .map(x -> List.of(x[tasksIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<TaskInfo>> tasksMap = taskQuery.checkAndFindByPlanAndName(
            sprintId, taskNames);
        // Check the if the associated cases exist
        Set<String> caseNames = data.stream()
            .filter(x -> casesIdx != -1 && isNotEmpty(x[casesIdx]))
            .map(x -> List.of(x[casesIdx].split("##"))).flatMap((Collection::stream))
            .collect(Collectors.toSet());
        Map<String, List<FuncCaseInfo>> casesMap = funcCaseQuery.checkAndFindByPlanAndName(
            sprintId, caseNames);

        // Check the if the associated testing targets exist

        // Format import fields and convert them into task objects
        List<Task> tasks = importToDomain(uidGenerator, projectId, sprintDb,
            data, nameIdx, taskTypeIdx, bugLevelIdx, testTypeIdx,
            assigneeMap, assigneeIdx, confirmorIdx, confirmorMap, priorityIdx, deadlineIdx,
            descriptionIdx, evalWorkloadIdx, actualWorkloadIdx, statusIdx, startDateIdx,
            processedDateIdx, canceledDateIdx, conformedDateIdx, completedDateIdx, creatorIdx,
            creatorsMap, createdDateIdx, tagsIdx, tagsMap, tasksIdx, tasksMap, casesIdx, casesMap,
            moduleIdx, modulesMap);

        // @DoInFuture("Check if the associated testing targets exist")
        // @DoInFuture("Check if the associated task targets exist")

        // When using an `COVER` strategy, delete existing tasks, otherwise ignore duplicate import tasks
        Set<String> safePrefixNames = nonNull(sprintDb) && isEmpty(sprintDb.getTaskPrefix())
            ? new HashSet<>(names) : names.stream().map(x -> sprintDb.getTaskPrefix() + x)
            .collect(Collectors.toSet());
        if (strategyWhenDuplicated.isCover()) {
          taskRepo.deleteBySprintIdAndNameIn(sprintId, safePrefixNames);
        } else {
          List<String> namesDb = taskRepo.findNameBySprintIdAndNameIn(sprintId, safePrefixNames);
          tasks = tasks.stream().filter(x -> !namesDb.contains(x.getName()))
              .collect(Collectors.toList());
        }

        if (isEmpty(tasks)) {
          return null;
        }

        // Save imported tasks
        return tasks.stream().map(x -> add(x)).collect(Collectors.toList());
      }
    }.execute();
  }

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

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

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
        activityCmd.batchAdd(activities);

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
                  .collect(Collectors.toList()));
        } else {
          activities = toActivities(TASK, tasksDb, MOVED,
              tasksDb.stream().map(s -> new Object[]{TASK_SPRINT})
                  .collect(Collectors.toList()));
        }
        return activities;
      }
    }.execute();
  }

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
            taskDb.setBugLevel(BugLevel.DEFAULT).setMissingBugFlag(false);
          } else {
            taskDb.setBugLevel(null).setMissingBugFlag(null);
          }
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, TYPE_UPDATED, taskType);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

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

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void replaceMissingBugFlag(Long id, Boolean missingBugFlag) {
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
        if (!missingBugFlag.equals(taskDb.getMissingBugFlag())) {
          taskDb.setMissingBugFlag(missingBugFlag);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb,
              missingBugFlag ? MISSING_BUG_SET : MISSING_BUG_CLEAR);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

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
              ? toActivity(TASK, taskDb, TASK_ASSIGNEE, userDb.getFullname())
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceConfirmors(Long id, Long confirmorId) {
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
        userDb = userManager.checkValidAndFindUserBase(confirmorId);
      }

      @Override
      protected Void process() {
        if (!Objects.equals(confirmorId, taskDb.getConfirmorId())) {
          taskDb.setConfirmorId(confirmorId);
          taskRepo.save(taskDb);

          Activity activity = nonNull(confirmorId)
              ? toActivity(TASK, taskDb, TASK_CONFIRMOR, userDb.getFullname())
              : toActivity(TASK, taskDb, TASK_CONFIRMOR_CLEAR);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

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
              .setOverdueFlag(nonNull(deadline) && deadline.isBefore(LocalDateTime.now()));
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replaceVersion(Long id, String version) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the version not exists
        if (isNotEmpty(version)) {
          taskVersionQuery.checkNotExits(taskDb.getProjectId(), version);
        }

        // Check the modify task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());
      }

      @Override
      protected Void process() {
        if (isEmpty(version)) {
          taskDb.setSoftwareVersion(null);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, VERSION_CLEAR, version);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);

          return null;
        }
        if (!version.equals(taskDb.getSoftwareVersion())) {
          taskDb.setSoftwareVersion(version);
          taskRepo.save(taskDb);

          Activity activity = toActivity(TASK, taskDb, VERSION, version);
          activityCmd.add(activity);

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }
        return null;
      }
    }.execute();
  }

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

            // Add modification event
            taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
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

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

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

            // Add modification event
            taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
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

          // Add modification event
          taskQuery.assembleAndSendModifyNoticeEvent(taskDb, activity);
        }

        // Value has not changed -> noop
        return null;
      }
    }.execute();
  }

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
        if (ObjectUtils.isEmpty(description)) {
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
        if (ObjectUtils.isEmpty(attachments)) {
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void start(Long id) {
    new BizTemplate<Void>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the add task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Check the pending status
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void processed(Long id) {
    new BizTemplate<Void>() {
      Task taskDb;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the add task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Check the whether there is assignee permission
        taskQuery.checkAssigneeUserPermission(taskDb);

        // Check the in_progress status
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void confirm(Long id, Result result, BigDecimal evalWorkload, BigDecimal actualWorkload) {
    new BizTemplate<Void>() {
      Task taskDb = null;

      @Override
      protected void checkParams() {
        // Check and find task
        taskDb = taskQuery.checkAndFind(id);

        // Check the add task permission
        taskSprintAuthQuery.checkModifyTaskAuth(getUserId(), taskDb.getSprintId());

        // Evaluation workload not set
        assertTrue(isNull(actualWorkload)
            || nonNull(evalWorkload), "Evaluation workload not set");

        boolean isConfirmTask = taskDb.isConfirmTask();
        if (isConfirmTask) {
          // Check the whether there is confirmor permission
          taskQuery.checkConfirmorUserPermission(taskDb);
        }

        // Check the non-confirmed task must be in progress
        if (!isConfirmTask && !taskDb.getStatus().isInProgress()) {
          throw BizException.of(TASK_NO_PROCESSING_CODE, TASK_NO_PROCESSING);
        }

        // Check the confirmed task must be in the processed status
        if (isConfirmTask && !taskDb.getStatus().isConfirming()) {
          throw BizException.of(TASK_NO_CONFIRMING_CODE, TASK_NO_CONFIRMING);
        }
      }

      @Override
      protected Void process() {
        LocalDateTime now = LocalDateTime.now();
        if (result.isSuccess()) {
          taskDb.setStatus(TaskStatus.COMPLETED).setCompletedDate(now);
          // Decrement 1 when the test type task execution result fails and if the confirmation succeeds.
          if (taskDb.getTaskType().isTestTask() && nonNull(taskDb.getExecResult())
              && taskDb.getExecResult().equals(Result.FAIL)) {
            taskDb.setFailNum(taskDb.getFailNum() - 1);
          }
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
   * Retest task
   *
   * <pre>
   *  Update the test status to pending test based on the existing test records, and clean up the historical test statistics and status
   * </pre>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void restart(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Check the test task exists
        tasksDb = taskRepo.findAllById(taskIds);
        taskQuery.checkTaskExists(taskIds, tasksDb);

        // Check the update task permission
        taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.RESTART_TASK);
      }

      @Override
      protected Void process() {
        tasksDb = tasksDb.stream().map(TaskConverter::toRestartTask).collect(Collectors.toList());
        taskRepo.saveAll(tasksDb);

        List<Activity> activities = toActivities(TASK, tasksDb, TASK_RESTART);
        activityCmd.batchAdd(activities);

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Reopen task
   *
   * <pre>
   * Based on the existing test records, update the test status as pending, without clearing the historical test statistics and status
   * </pre>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reopen(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Check the test task exists
        tasksDb = taskRepo.findAllById(taskIds);
        taskQuery.checkTaskExists(taskIds, tasksDb);

        // Check the update task permission
        taskSprintAuthQuery.batchCheckPermission(tasksDb.stream().map(Task::getSprintId)
                .filter(Objects::nonNull).collect(Collectors.toSet()),
            TaskSprintPermission.REOPEN_TASK);

        // Check the task is opened repeatedly
        taskQuery.checkTaskOpenStatus(tasksDb);
      }

      @Override
      protected Void process() {
        tasksDb = tasksDb.stream().map(TaskConverter::toReopenTask).collect(Collectors.toList());
        taskRepo.saveAll(tasksDb);

        List<Activity> activities = toActivities(TASK, tasksDb, TASK_REOPEN);
        activityCmd.batchAdd(activities);

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(List<Long> taskIds) {
    new BizTemplate<Void>() {
      List<Task> tasksDb;

      @Override
      protected void checkParams() {
        // Check the test task exists
        tasksDb = taskRepo.findAllById(taskIds);
        // taskQuery.checkTaskExists(taskIds, tasksDb);

        // Check the delete task permission
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

        // Update delete status
        taskRepo.updateDeleteStatus(taskIds, true, getUserId(), LocalDateTime.now());

        // Add delete activity
        List<Activity> activities = toActivities(TASK, tasksDb, DELETED, activityParams(tasksDb));
        activityCmd.batchAdd(activities);

        // Add deleted api to trash
        trashTaskCmd.add0(tasksDb.stream().map(TaskConverter::toTaskTrash)
            .collect(Collectors.toList()));

        // Add modification events
        taskQuery.assembleAndSendModifyNoticeEvent(tasksDb, activities);
        return null;
      }
    }.execute();
  }

  /**
   * Reopen or restart task
   *
   * <pre>
   * Based on the existing test records, update the test status as pending, without clearing the historical test statistics and status
   * </pre>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void retest0ByTarget(Boolean restartFlag, List<Task> tasksDb) {
    List<Task> retestTaskDbs;
    if (restartFlag) {
      retestTaskDbs = tasksDb.stream().map(TaskConverter::toRestartTask)
          .collect(Collectors.toList());
    } else {
      retestTaskDbs = tasksDb.stream().map(o -> o.setStatus(TaskStatus.PENDING))
          .collect(Collectors.toList());
    }

    taskRepo.saveAll(retestTaskDbs);

    activityCmd.batchAdd(toActivities(TASK, tasksDb, restartFlag ? TASK_RESTART : TASK_REOPEN));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0ByTarget(List<Long> taskIds) {
    List<Task> tasksDb = taskRepo.findAllById(taskIds);

    taskRemarkRepo.deleteByTaskIdIn(taskIds);
    tagTargetCmd.deleteByTargetIdIn(taskIds);
    taskFuncCaseCmd.deleteByTargetIds(taskIds);

    taskRepo.deleteByIdIn(taskIds);

    // Save delete activity
    activityCmd.batchAdd(toActivities(TASK, tasksDb, DELETED));
  }

  @Override
  public void delete0(List<Long> taskIds) {
    // Notice: Delete assignees before deleting tasks
    taskRemarkRepo.deleteByTaskIdIn(taskIds);
    tagTargetCmd.deleteByTargetIdIn(taskIds);
    taskFuncCaseCmd.deleteByTargetIds(taskIds);

    taskRepo.deleteByIdIn(taskIds);
  }

  public static String getTaskCode() {
    return getBean(BidGenerator.class).getId(TASK_BID_KEY, getTenantId());
  }

  @Override
  protected BaseRepository<Task, Long> getRepository() {
    return this.taskRepo;
  }
}
