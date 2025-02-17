package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.api.search.SearchCriteria.equal;
import static cloud.xcan.sdf.api.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.sdf.api.search.SearchCriteria.in;
import static cloud.xcan.sdf.api.search.SearchCriteria.isNotNull;
import static cloud.xcan.sdf.api.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.sdf.core.angustester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.sdf.spec.utils.DateUtils.asDate;
import static cloud.xcan.sdf.spec.utils.DateUtils.getLocalDateTime;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.convert;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isTrueValue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ProbabilityUtils.randomWithProbability;
import static cloud.xcan.sdf.spec.utils.StringUtils.isNumeric;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.isLastMonth;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.isLastWeek;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.TaskTargetType;
import cloud.xcan.sdf.api.commonlink.user.User;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.services.testing.TestTaskSetting;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartDetail;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskAssigneeProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskLastResourceCreationCount;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingType;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskDetailSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSummary;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionStatus;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.spec.locale.SupportedLanguage;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author xiaolong.liu
 */
public class TaskConverter {

  public static void assembleAddTaskInfo(Task task, @Nullable TaskSprint sprintDb,
      boolean isAgile) {
    if (nonNull(sprintDb)) {
      task.setProjectId(sprintDb.getProjectId());
      if (isNotEmpty(sprintDb.getTaskPrefix())
          && !task.getName().startsWith(sprintDb.getTaskPrefix())) {
        task.setName(sprintDb.getTaskPrefix() + task.getName());
      }
    }
    task.setUnplannedFlag(nonNull(sprintDb) && sprintDb.getStatus().isStarted());
    task.setModuleId(nullSafe(task.getModuleId(), -1L));
    task.setEvalWorkloadMethod(nonNull(sprintDb) ? sprintDb.getEvalWorkloadMethod()
        : isAgile ? EvalWorkloadMethod.STORY_POINT : EvalWorkloadMethod.WORKING_HOURS);
    // task.setActualWorkload(isNull(task.getEvalWorkload()) ? null : task.getActualWorkload()); -> Actual workload cannot be set when adding
    task.setSprintAuthFlag(nonNull(sprintDb) ? sprintDb.getAuthFlag() : false);
    task.setCreatedBy(nullSafe(task.getCreatedBy(), getUserId()));
    task.setCreatedDate(nullSafe(task.getCreatedDate(), LocalDateTime.now()));
  }

  public static void assembleUpdateTask(Task task, Task taskDb) {
    if (taskDb.getTaskType().isTestTask()) {
      copyPropertiesIgnoreNull(task, taskDb, "taskType");
    } else {
      copyPropertiesIgnoreNull(task, taskDb);
    }

    if (nonNull(task.getTaskType()) && !task.getTaskType().equals(taskDb.getTaskType())) {
      if (taskDb.getTaskType().isBug()) {
        taskDb.setBugLevel(null);
      }
      if (task.getTaskType().isBug() && isNull(taskDb.getBugLevel())) {
        taskDb.setBugLevel(BugLevel.DEFAULT);
      }
    }

    if (nonNull(task.getActualWorkload()) && isNull(taskDb.getEvalWorkload())) {
      taskDb.setEvalWorkload(task.getActualWorkload());
    }

    taskDb.setOverdueFlag(nonNull(taskDb.getDeadlineDate())
        && taskDb.getDeadlineDate().isBefore(LocalDateTime.now()));
  }

  public static void assembleReplaceTask(Task task, Task taskDb) {
    taskDb.setModuleId(nullSafe(task.getModuleId(), -1L))
        .setName(task.getName())
        .setSoftwareVersion(task.getSoftwareVersion())
        .setPriority(task.getPriority())
        .setAssigneeId(task.getAssigneeId())
        .setConfirmorId(task.getConfirmorId())
        .setDeadlineDate(task.getDeadlineDate())
        .setAttachments(task.getAttachments())
        .setDescription(task.getDescription())
        .setEvalWorkload(task.getEvalWorkload())
        .setActualWorkload(task.getActualWorkload())
        .setParentTaskId(task.getParentTaskId())
        .setRefTaskIds(task.getRefTaskIds())
        .setRefCaseIds(task.getRefCaseIds());
    if (!taskDb.getTaskType().isTestTask() && nonNull(task.getTaskType())) {
      taskDb.setTaskType(task.getTaskType());
    }
    taskDb.setBugLevel(task.getBugLevel());
    if (!task.getTaskType().equals(taskDb.getTaskType())) {
      if (taskDb.getTaskType().isBug()) {
        taskDb.setBugLevel(null);
      }
      if (task.getTaskType().isBug() && isNull(taskDb.getBugLevel())) {
        taskDb.setBugLevel(BugLevel.DEFAULT);
      }
    }
    if (task.getTaskType().isBug()) {
      taskDb.setMissingBugFlag(task.getMissingBugFlag());
    } else {
      taskDb.setMissingBugFlag(false);
    }
    if (nonNull(task.getActualWorkload()) && isNull(taskDb.getEvalWorkload())) {
      taskDb.setEvalWorkload(task.getActualWorkload());
    }
    taskDb.setOverdueFlag(nonNull(taskDb.getDeadlineDate())
        && taskDb.getDeadlineDate().isBefore(LocalDateTime.now()));
  }

  public static void assembleExampleTask(Project projectDb, Long id, Task task,
      TaskSprint sprint, List<User> users) {
    Random userRandom = new Random();
    task.setId(id).setCode(getTaskCode())
        .setProjectId(projectDb.getId()).setModuleId(-1L)
        .setSprintId(nonNull(sprint) ? sprint.getId(): null).setSprintAuthFlag(false)
        // Set 1/3 of the pending tasks in the agile project as backlog
        .setBacklogFlag(projectDb.isAgile() && task.getStatus().isPending()
            && randomWithProbability(3))
        .setAssigneeId(users.get(userRandom.nextInt(users.size())).getId())
        .setConfirmorId(users.get(userRandom.nextInt(users.size())).getId())
        .setTenantId(projectDb.getTenantId()).setDeletedFlag(false)
        .setCreatedBy(users.get(userRandom.nextInt(users.size())).getId())
        .setCreatedDate(nonNull(task.getDeadlineDate())
            ? task.getDeadlineDate().minusMonths(1) : LocalDateTime.now())
        .setLastModifiedBy(users.get(userRandom.nextInt(users.size())).getId())
        .setLastModifiedDate(nonNull(task.getDeadlineDate())
            ? task.getDeadlineDate().minusMonths(1) : LocalDateTime.now());
  }

  public static void assembleExampleTaskSprint(Project projectDb, Long id,
      List<User> users, TaskSprint sprint) {
    Long currentUserId = isUserAction() ? getUserId() : users.get(0).getId();
    sprint.setId(id).setProjectId(projectDb.getId()).setAuthFlag(false)
        .setOwnerId(currentUserId).setTenantId(projectDb.getTenantId()).setDeletedFlag(false)
        .setCreatedBy(currentUserId).setCreatedDate(sprint.getStartDate())
        .setLastModifiedBy(currentUserId).setLastModifiedDate(sprint.getStartDate());
  }

  public static @NotNull SoftwareVersion assembleExampleTaskSoftwareVersion(Project projectDb,
      Long id, List<User> users, TaskSprint sprint) {
    SoftwareVersion version = new SoftwareVersion();
    Long currentUserId = isUserAction() ? getUserId() : users.get(0).getId();
    version.setId(id).setName("v1.0").setProjectId(projectDb.getId())
        .setStatus(SoftwareVersionStatus.DEFAULT).setDescription("Release1.0")
        .setTenantId(projectDb.getTenantId())
        .setCreatedBy(currentUserId).setCreatedDate(projectDb.getCreatedDate())
        .setLastModifiedBy(currentUserId).setLastModifiedDate(projectDb.getCreatedDate());
    if (nonNull(sprint)) {
      version.setStartDate(sprint.getStartDate())
          .setReleaseDate(sprint.getDeadlineDate());
    } else {
      version.setStartDate(projectDb.getStartDate())
          .setReleaseDate(projectDb.getDeadlineDate());
    }
    return version;
  }

  public static Task toAddApisOrScenarioTask(Long projectId, @Nullable TaskSprint sprintDb,
      ApisBaseInfo apis, Scenario scenario, Task task) {
    task.setId(getBean(CachedUidGenerator.class).getUID())
        .setProjectId(projectId)
        .setSprintId(nonNull(sprintDb) ? sprintDb.getId() : null)
        .setSprintAuthFlag(nonNull(sprintDb) ? sprintDb.getAuthFlag() : false)
        .setModuleId(nullSafe(task.getModuleId(), -1L))
        .setBacklogFlag(false)
        .setTargetId(nonNull(apis) ? apis.getId() : scenario.getId())
        .setTaskType(nonNull(apis) ? TaskType.API_TEST : TaskType.SCENARIO_TEST)
        .setName((nonNull(apis) ? apis.getName()
            : scenario.getName()) + "-" + task.getTestType().getMessage())
        .setSoftwareVersion(null)
        .setTargetParentId(nonNull(apis) ? apis.getServiceId() : scenario.getProjectId())
        .setStatus(TaskStatus.PENDING)
        .setUnplannedFlag(nonNull(sprintDb) && sprintDb.getStatus().isStarted())
        .setFailNum(0)
        .setTotalNum(0)
        .setEvalWorkloadMethod(nonNull(sprintDb) ? sprintDb.getEvalWorkloadMethod()
            : EvalWorkloadMethod.WORKING_HOURS) // General Project Management
        .setSprintDeletedFlag(false)
        .setDeletedFlag(false)
        .setCreatedBy(getUserId())
        .setCreatedDate(LocalDateTime.now());
    if (nonNull(sprintDb) && isNotEmpty(sprintDb.getTaskPrefix())
        && !task.getName().startsWith(sprintDb.getTaskPrefix())) {
      task.setName(sprintDb.getTaskPrefix() + task.getName());
    }
    return task;
  }

  public static List<Task> generateToServicesTask(Long apisId, List<TestTaskSetting> testings) {
    return testings.stream().map(testing -> new Task()
        .setModuleId(-1L)
        .setTargetId(apisId)
        .setTaskType(TaskType.API_TEST)
        .setTestType(testing.getTestType())
        .setPriority(testing.getPriority())
        .setAssigneeId(testing.getAssigneeId())
        .setStartDate(testing.getStartDate())
        .setDeadlineDate(testing.getDeadlineDate())
        .setBacklogFlag(false) // Assign sprint is required or is general project management
        .setOverdueFlag(false)
        .setCode(getTaskCode())
    ).collect(Collectors.toList());
  }

  public static void assembleMoveTask(TaskSprint targetSprintDb, Task taskDb) {
    taskDb.setSprintId(isNull(targetSprintDb) ? null : targetSprintDb.getId());
    taskDb.setBacklogFlag(isNull(targetSprintDb));
    taskDb.setUnplannedFlag(nonNull(targetSprintDb) && targetSprintDb.getStatus().isStarted());
    if (nonNull(targetSprintDb)) {
      taskDb.setProjectId(targetSprintDb.getProjectId())
          // Overwrite backlog default
          .setEvalWorkloadMethod(targetSprintDb.getEvalWorkloadMethod())
          .setSprintDeletedFlag(targetSprintDb.getDeletedFlag())
          .setSprintAuthFlag(targetSprintDb.getAuthFlag())
          .setDeadlineDate(nullSafe(taskDb.getDeadlineDate(),
              targetSprintDb.getDeadlineDate()))
          .setOverdueFlag(taskDb.getDeadlineDate().isBefore(LocalDateTime.now()));
    }
  }

  public static Task toRestartTask(Task task) {
    return task.setTotalNum(0).setFailNum(0)
        .setCompletedDate(null).setCanceledDate(null).setProcessedDate(null)
        .setExecBy(null).setExecId(null).setCanceledDate(null)
        .setStartDate(null).setStatus(TaskStatus.PENDING);
  }

  public static Task toReopenTask(Task task) {
    return task.setCompletedDate(null).setCanceledDate(null).setProcessedDate(null)
        .setExecBy(null).setExecId(null).setCanceledDate(null)
        .setStartDate(null).setStatus(TaskStatus.PENDING);
  }

  public static TaskTrash toTaskTrash(Task taskDb) {
    return new TaskTrash()
        .setProjectId(taskDb.getProjectId())
        .setSprintId(taskDb.getSprintId())
        .setTargetType(TaskTargetType.TASK)
        .setTargetId(taskDb.getId())
        .setTargetName(taskDb.getName())
        .setTaskType(taskDb.getTaskType())
        .setCreatedBy(taskDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static TaskSummary toTaskSummary(TaskInfo task) {
    TaskSummary summary = new TaskSummary();
    summary.setId(task.getId());
    summary.setName(task.getName());
    summary.setCode(task.getCode());
    summary.setTaskType(task.getTaskType());
    summary.setTestType(task.getTestType());
    summary.setProjectId(task.getProjectId());
    summary.setSprintId(task.getSprintId());
    summary.setModuleId(task.getModuleId());
    summary.setBacklogFlag(task.getBacklogFlag());
    summary.setPriority(task.getPriority());
    summary.setStatus(task.getStatus());
    summary.setAssigneeId(task.getAssigneeId());
    summary.setConfirmorId(task.getConfirmorId());
    summary.setStartDate(task.getStartDate());
    summary.setDeadlineDate(task.getDeadlineDate());
    summary.setCompletedDate(task.getCompletedDate());
    summary.setCanceledDate(task.getCanceledDate());
    summary.setConfirmedDate(task.getConfirmedDate());
    summary.setProcessedDate(task.getProcessedDate());
    summary.setEvalWorkloadMethod(task.getEvalWorkloadMethod());
    summary.setEvalWorkload(task.getEvalWorkload());
    summary.setActualWorkload(task.getActualWorkload());
    summary.setFailNum(task.getFailNum());
    summary.setTotalNum(task.getTotalNum());
    summary.setConfirmTaskFlag(nonNull(task.getConfirmorId()));
    summary.setOverdueFlag(task.getOverdueFlag());
    //.setDescription(task.getDescription());
    summary.setTargetId(task.getTargetId());
    //.setTargetName(task.getTargetName());
    summary.setTargetParentId(task.getTargetParentId());
    //.setTargetParentName(task.getTargetParentName())
    //.setScriptId(task.getScriptId())
    summary.setExecResult(task.getExecResult());
    summary.setExecFailureMessage(task.getExecFailureMessage());
    summary.setExecTestNum(task.getExecTestNum());
    summary.setExecTestFailureNum(task.getExecTestFailureNum());
    summary.setExecId(task.getExecId());
    summary.setExecName(task.getExecName());
    summary.setExecBy(task.getExecBy());
    summary.setExecDate(task.getExecDate());
    //.setFavouriteFlag(task.getFavouriteFlag())
    //.setFollowFlag(task.getFollowFlag())
    //.setCommentNum(task.getCommentNum())
    summary.setCreatedBy(task.getCreatedBy());
    summary.setCreatedDate(task.getCreatedDate());
    summary.setLastModifiedBy(task.getLastModifiedBy());
    summary.setLastModifiedDate(task.getLastModifiedDate());
    return summary;
  }

  public static TaskDetailSummary toTaskDetailSummary(Task task) {
    return new TaskDetailSummary().setId(task.getId())
        .setName(task.getName())
        .setCode(task.getCode())
        .setTaskType(task.getTaskType())
        .setBugLevel(task.getBugLevel())
        .setTestType(task.getTestType())
        .setProjectId(task.getProjectId())
        .setSprintId(task.getSprintId())
        .setModuleId(task.getModuleId())
        .setBacklogFlag(task.getBacklogFlag())
        .setPriority(task.getPriority())
        .setStatus(task.getStatus())
        .setAssigneeId(task.getAssigneeId())
        .setConfirmorId(task.getConfirmorId())
        .setTesterId(task.getTesterId())
        .setMissingBugFlag(task.getMissingBugFlag())
        .setUnplannedFlag(task.getUnplannedFlag())
        .setStartDate(task.getStartDate())
        .setDeadlineDate(task.getDeadlineDate())
        .setCompletedDate(task.getCompletedDate())
        .setCanceledDate(task.getCanceledDate())
        .setConfirmedDate(task.getConfirmedDate())
        .setProcessedDate(task.getProcessedDate())
        .setEvalWorkloadMethod(task.getEvalWorkloadMethod())
        .setEvalWorkload(task.getEvalWorkload())
        .setActualWorkload(task.getActualWorkload())
        .setFailNum(task.getFailNum())
        .setTotalNum(task.getTotalNum())
        .setSubTaskProgress(isEmpty(task.getSubTasks()) ? new Progress()
            : new Progress().setCompleted(
                    task.getSubTasks().stream().filter(x -> x.getStatus().isFinished()).count())
                .setTotal(task.getSubTasks().size()))
        .setSubTaskInfos(ObjectUtils.isNotEmpty(task.getSubTasks()) ? task.getSubTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefTaskInfos(
            isNotEmpty(task.getAssocTasks()) ? task.getAssocTasks().stream()
                .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefCaseInfos(
            isNotEmpty(task.getAssocCases()) ? task.getAssocCases().stream()
                .map(FuncCaseAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setTags(isNotEmpty(task.getTagTargets()) ? task.getTagTargets().stream()
            .map(TaskAssembler::toTagVo).collect(Collectors.toList()) : emptyList())
        .setCurrentAssociateType(task.getCurrentAssociateType())
        .setConfirmTaskFlag(task.isConfirmTask())
        .setOverdueFlag(task.getOverdueFlag())
        .setDescription(task.getDescription())
        .setTargetId(task.getTargetId())
        .setTargetName(task.getTargetName())
        .setTargetParentId(task.getTargetParentId())
        .setTargetParentName(task.getTargetParentName())
        .setScriptId(task.getScriptId())
        .setExecResult(task.getExecResult())
        .setExecFailureMessage(task.getExecFailureMessage())
        .setExecTestNum(task.getExecTestNum())
        .setExecTestFailureNum(task.getExecTestFailureNum())
        .setExecId(task.getExecId())
        .setExecName(task.getExecName())
        .setExecBy(task.getExecBy())
        .setExecDate(task.getExecDate())
        //.setFavouriteFlag(task.getFavouriteFlag())
        //.setFollowFlag(task.getFollowFlag())
        //.setCommentNum(task.getCommentNum())
        .setCreatedBy(task.getCreatedBy())
        .setCreatedDate(task.getCreatedDate())
        .setLastModifiedBy(task.getLastModifiedBy())
        .setLastModifiedDate(task.getLastModifiedDate());
  }

  public static TaskRemarkSummary toTaskRemarkSummary(TaskRemark remark) {
    return new TaskRemarkSummary().setId(remark.getId())
        .setTaskId(remark.getTaskId())
        .setCreatedBy(remark.getCreatedBy())
        .setCreatedDate(remark.getCreatedDate())
        .setContent(remark.getContent());
  }

  public static TaskCount objectArrToTaskCount(List<Object[]> groupByResult,
      List<Object[]> overdueResult, List<Object[]> oneTimePassResult,
      List<Object[]> sumNumResult, List<Object[]> completedWorkloadResult) {
    if (isEmpty(groupByResult) && isEmpty(overdueResult) && isEmpty(oneTimePassResult)) {
      return new TaskCount();
    }

    TaskCount statistics = new TaskCount();
    // Statistics by status
    Map<String, Integer> statusMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[0], String.class), x -> convert(x[4], Integer.class), Integer::sum));
    statistics.setPendingNum(nullSafe(statusMap.get(TaskStatus.PENDING.name()), 0))
        .setInProgressNum(nullSafe(statusMap.get(TaskStatus.IN_PROGRESS.name()), 0))
        .setConfirmingNum(nullSafe(statusMap.get(TaskStatus.CONFIRMING.name()), 0))
        .setCompletedNum(nullSafe(statusMap.get(TaskStatus.COMPLETED.name()), 0))
        .setCanceledNum(nullSafe(statusMap.get(TaskStatus.CANCELED.name()), 0))
        .setTotalStatusNum(statistics.getPendingNum() + statistics.getInProgressNum()
            + statistics.getConfirmingNum() + statistics.getCompletedNum()
            + statistics.getCanceledNum());

    statistics.setTotalTaskNum(statistics.getTotalStatusNum());
    statistics.setValidTaskNum(statistics.getTotalStatusNum() - statistics.getCanceledNum());

    // Statistics by testType
    Map<String, Integer> testTypeMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[1], String.class), x -> convert(x[4], Integer.class), Integer::sum));
    statistics.setPerfNum(nullSafe(testTypeMap.get(TestType.PERFORMANCE.name()), 0))
        .setFunctionalNum(nullSafe(testTypeMap.get(TestType.FUNCTIONAL.name()), 0))
        .setStabilityNum(nullSafe(testTypeMap.get(TestType.STABILITY.name()), 0))
        .setTotalTestTypeNum(statistics.getPerfNum() + statistics.getFunctionalNum()
            + statistics.getStabilityNum());

    // Statistics by taskType
    Map<String, Integer> taskTypeMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[2], String.class), x -> convert(x[4], Integer.class), Integer::sum));
    statistics.setRequirementNum(nullSafe(taskTypeMap.get(TaskType.REQUIREMENT.name()), 0))
        .setStoryNum(nullSafe(taskTypeMap.get(TaskType.STORY.name()), 0))
        .setTaskNum(nullSafe(taskTypeMap.get(TaskType.TASK.name()), 0))
        .setBugNum(nullSafe(taskTypeMap.get(TaskType.BUG.name()), 0))
        .setApiTestNum(nullSafe(taskTypeMap.get(TaskType.API_TEST.name()), 0))
        .setScenarioTestNum(nullSafe(taskTypeMap.get(TaskType.SCENARIO_TEST.name()), 0))
        .setTotalTaskTypeNum(statistics.getStoryNum() + statistics.getRequirementNum()
            + statistics.getTaskNum() + statistics.getBugNum() + statistics.getApiTestNum()
            + statistics.getScenarioTestNum());

    // Statistics by result
    Map<String, Integer> resultMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[3], String.class), x -> convert(x[4], Integer.class), Integer::sum));
    statistics.setTestSuccessNum(nullSafe(resultMap.get(Result.SUCCESS.name()), 0))
        .setTestFailNum(nullSafe(resultMap.get(Result.FAIL.name()), 0));

    // Statistics are overdue
    if (!overdueResult.isEmpty()) {
      Integer overdues = convert(overdueResult.get(0), Integer.class);
      statistics.setOverdueNum(nullSafe(overdues, 0));
    }

    // Statistics one-time pass number
    if (!oneTimePassResult.isEmpty()) {
      Integer oneTimePass = convert(oneTimePassResult.get(0), Integer.class);
      statistics.setOneTimePassedNum(nullSafe(oneTimePass, 0));
      if (!oneTimePass.equals(0)) {
        // Statistics one-time pass rate
        BigDecimal rate = BigDecimal
            .valueOf((double) statistics.getOneTimePassedNum() / statistics.getCompletedNum())
            // X 100 -> %
            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        statistics.setOneTimePassedRate(rate);
      }
    }

    // Statistics task workload and process times
    if (!sumNumResult.isEmpty()) {
      statistics.setProcessTimes(nullSafe(convert(sumNumResult.get(0)[0], Integer.class), 0));
      statistics.setProcessTimes(nullSafe(convert(sumNumResult.get(0)[1], Integer.class), 0));
      Double evalWorkload = convert(sumNumResult.get(0)[2], Double.class);
      statistics.setEvalWorkload(
          nonNull(evalWorkload) ? BigDecimal.valueOf(evalWorkload) : BigDecimal.ZERO);
      Double actualWorkload = convert(sumNumResult.get(0)[3], Double.class);
      statistics.setActualWorkload(
          nonNull(actualWorkload) ? BigDecimal.valueOf(actualWorkload) : BigDecimal.ZERO);
    }
    if (!completedWorkloadResult.isEmpty()) {
      Double completedWorkload = convert(completedWorkloadResult.get(0), Double.class);
      statistics.setCompletedWorkload(
          nonNull(completedWorkload) ? BigDecimal.valueOf(completedWorkload) : BigDecimal.ZERO);
    }
    return statistics;
  }

  public static @NotNull List<Task> importToDomain(
      CachedUidGenerator uidGenerator, Project projectDb, @Nullable TaskSprint sprintDb,
      List<String[]> data, int nameIdx, int taskTypeIdx, int bugLevelIdx,
      int testTypeIdx, Map<String, List<UserBase>> assigneeMap, int assigneeIdx,
      int confirmorIdx, Map<String, List<UserBase>> confirmorMap, int testerIdx,
      Map<String, List<UserBase>> testerMap, int missingBugIdx, int unplannedIdx, int priorityIdx,
      int deadlineIdx, int descIdx, int evalWorkloadIdx, int actualWorkloadIdx, int statusIdx,
      int softwareVersionIdx, int startDateIdx, int processedDateIdx, int canceledDateIdx,
      int conformedDateIdx, int completedDateIdx, int creatorIdx,
      Map<String, List<UserBase>> creatorsMap, int createdDateIdx, int tagsIdx,
      Map<String, List<Tag>> tagsMap, int tasksIdx, Map<String, List<TaskInfo>> tasksMap,
      int casesIdx, Map<String, List<FuncCaseInfo>> casesMap,
      int moduleIdx, Map<String, Module> modulesMap) {
    List<Task> tasks = new ArrayList<>();
    try {
      Locale zhLocale = SupportedLanguage.zh_CN.toLocale();
      for (String[] row : data) {
        TaskType taskType = TaskType.ofMessage(row[taskTypeIdx], zhLocale);
        Task task = new Task().setId(uidGenerator.getUID())
            .setProjectId(projectDb.getProjectId()).setCode(getTaskCode())
            .setSprintId(nonNull(sprintDb) ? sprintDb.getId() : null)
            .setSprintAuthFlag(nonNull(sprintDb) ? sprintDb.getAuthFlag() : false)
            .setModuleId(moduleIdx != -1 && isNotEmpty(row[moduleIdx])
                ? modulesMap.get(row[moduleIdx]).getId() : -1L)
            .setBacklogFlag(/* Agile Project Management and not in Sprint */
                projectDb.isAgile() && isNull(sprintDb))
            .setName(row[nameIdx]) // Required
            .setTaskType(taskType) // Required
            .setBugLevel(taskType.isBug() ? (bugLevelIdx != -1 && isNotEmpty(row[bugLevelIdx])
                ? BugLevel.ofMessage(row[bugLevelIdx], zhLocale) : BugLevel.DEFAULT) : null)
            .setTestType(testTypeIdx != -1 && isNotEmpty(row[testTypeIdx])
                ? TestType.ofMessage(row[testTypeIdx], zhLocale) : null)
            /*.setTargetId(targetIdIdx != -1 && isDigits(row[targetIdIdx])
                ? Long.parseLong(row[targetIdIdx]) : null)*/
            .setAssigneeId(assigneeIdx != -1 && nonNull(assigneeMap.get(row[assigneeIdx]))
                ? assigneeMap.get(row[assigneeIdx]).get(0).getId() : null)
            .setConfirmorId(confirmorIdx != -1 && nonNull(confirmorMap.get(row[confirmorIdx]))
                ? confirmorMap.get(row[confirmorIdx]).get(0).getId() : null)
            .setTesterId(testerIdx != -1 && nonNull(testerMap.get(row[testerIdx]))
                ? testerMap.get(row[testerIdx]).get(0).getId() : null)
            .setMissingBugFlag(!taskType.isBug() ? null
                : missingBugIdx != -1 && isTrueValue(row[missingBugIdx]))
            .setUnplannedFlag(unplannedIdx != -1 && isTrueValue(row[unplannedIdx]))
            .setPriority(nullSafe(Priority.ofMessage(row[priorityIdx], zhLocale), Priority.DEFAULT))
            .setDeadlineDate(deadlineIdx != -1 && isNotEmpty(row[deadlineIdx])
                ? getLocalDateTime(row[deadlineIdx]) : null)
            .setDescription(descIdx != -1 ? row[descIdx] : null)
            .setEvalWorkloadMethod(nonNull(sprintDb) ? sprintDb.getEvalWorkloadMethod()
                : EvalWorkloadMethod.WORKING_HOURS) // General Project Management
            .setEvalWorkload(evalWorkloadIdx != -1 && isNumeric(row[evalWorkloadIdx])
                ? BigDecimal.valueOf(Double.parseDouble(row[evalWorkloadIdx])) : null)
            .setActualWorkload(actualWorkloadIdx != -1 && isNumeric(row[actualWorkloadIdx])
                ? BigDecimal.valueOf(Double.parseDouble(row[actualWorkloadIdx])) : null)
            .setStatus(statusIdx != -1 && isNotEmpty(row[statusIdx])
                ? TaskStatus.ofMessage(row[statusIdx], zhLocale) : TaskStatus.PENDING)
            .setSoftwareVersion(softwareVersionIdx != -1 ? row[softwareVersionIdx] : null)
            .setStartDate(startDateIdx != -1 && isNotEmpty(row[startDateIdx])
                ? getLocalDateTime(row[startDateIdx]) : null)
            .setProcessedDate(processedDateIdx != -1 && isNotEmpty(row[processedDateIdx])
                ? getLocalDateTime(row[processedDateIdx]) : null)
            .setCanceledDate(canceledDateIdx != -1 && isNotEmpty(row[canceledDateIdx])
                ? getLocalDateTime(row[canceledDateIdx]) : null)
            .setConfirmedDate(conformedDateIdx != -1 && isNotEmpty(row[conformedDateIdx])
                ? getLocalDateTime(row[conformedDateIdx]) : null)
            .setCompletedDate(completedDateIdx != -1 && isNotEmpty(row[completedDateIdx])
                ? getLocalDateTime(row[completedDateIdx]) : null);
        task.setCreatedBy(creatorIdx != -1 && nonNull(creatorsMap.get(row[creatorIdx]))
            ? creatorsMap.get(row[creatorIdx]).get(0).getId() : getUserId());
        task.setCreatedDate(createdDateIdx != -1 && isNotEmpty(row[createdDateIdx])
            ? getLocalDateTime(row[createdDateIdx]) : LocalDateTime.now());
        List<String> taskTags = isNotEmpty(row[tagsIdx])
            ? List.of(row[tagsIdx].split("##")) : null;
        if (ObjectUtils.isNotEmpty(taskTags)) {
          task.setTagTargets(taskTags.stream().filter(x -> nonNull(tagsMap.get(x)))
              .map(x -> new TagTarget().setId(uidGenerator.getUID())
                      .setTargetId(task.getId()).setTargetType(CombinedTargetType.TASK)
                      .setTagId(tagsMap.get(x).get(0).getId())
                  /*.setTaskType(task.getTaskType())*/)
              .collect(Collectors.toList()));
        }
        List<String> taskNames0 = isNotEmpty(row[tasksIdx])
            ? List.of(row[tasksIdx].split("##")) : null;
        if (ObjectUtils.isNotEmpty(taskNames0)) {
          task.setRefTaskIds(new LinkedHashSet<>(
              taskNames0.stream().filter(x -> nonNull(tasksMap.get(x)))
                  .map(x -> tasksMap.get(x).get(0).getId()).collect(Collectors.toList())));
        }
        List<String> caseNames0 = isNotEmpty(row[casesIdx])
            ? List.of(row[casesIdx].split("##")) : null;
        if (ObjectUtils.isNotEmpty(caseNames0)) {
          task.setRefCaseIds(new LinkedHashSet<>(
              caseNames0.stream().filter(x -> nonNull(casesMap.get(x)))
                  .map(x -> casesMap.get(x).get(0).getId()).collect(Collectors.toList())));
        }
        task.setOverdueFlag(false)
            .setTotalNum(0).setFailNum(0)
            .setSprintDeletedFlag(false)
            .setDeletedFlag(false);
        tasks.add(task);
      }
    } catch (Exception e) {
      throw CommProtocolException.of("Data format error: " + e.getMessage());
    }
    return tasks;
  }

  public static void assembleTimeSeriesByFormat(
      Map<BurnDownResourceType, BurnDownChartCount> chartMap,
      List<TaskEfficiencySummary> validTasks, LocalDateTime safeCreatedDateStart,
      LocalDateTime safeCreatedDateEnd) {
    List<String> days = new ArrayList<>();
    LocalDateTime nextDay = safeCreatedDateStart;
    do {
      days.add(format(asDate(nextDay), DEFAULT_DAY_FORMAT));
      nextDay = nextDay.plusDays(1);
    } while (nextDay.isBefore(safeCreatedDateEnd));
    String lastDay = format(asDate(safeCreatedDateEnd), DEFAULT_DAY_FORMAT);
    if (!days.contains(lastDay)) {
      days.add(lastDay);
    }

    Map<String, List<TaskEfficiencySummary>> dayCompletedGroup = validTasks.stream()
        .filter(x -> x.getStatus().isCompleted())
        .collect(groupingBy(x -> format(asDate(x.getCreatedDate()), DEFAULT_DAY_FORMAT),
            Collectors.mapping(Function.identity(), Collectors.toList())));

    String currentDay = format(new Date(), DEFAULT_DAY_FORMAT);

    // Num burndown chart
    BurnDownChartCount numChart = new BurnDownChartCount();
    long total = validTasks.size();
    long completed = validTasks.stream().filter(x -> x.getStatus().isCompleted()).count();
    numChart.setTotal(total).setCompleted(completed).setRemained(total - completed)
        .setStartDate(safeCreatedDateStart).setEndDate(safeCreatedDateEnd);
    double dayExpectedNum = (double) validTasks.size() / days.size();
    List<DataTimeSeries> allExpectedNum = new ArrayList<>();
    List<DataTimeSeries> allRemainingNum = new ArrayList<>();
    int remainingNum = validTasks.size();
    boolean isFuture = true;
    for (int i = 0; i < days.size(); i++) {
      allExpectedNum.add(new DataTimeSeries(days.get(i),
          (int) ((double) validTasks.size() - ((i + 1) * dayExpectedNum))));
      if (isFuture) {
        if (currentDay.equals(days.get(i))) {
          remainingNum = remainingNum - (dayCompletedGroup.containsKey(days.get(i))
              ? dayCompletedGroup.get(days.get(i)).size() : 0);
          allRemainingNum.add(new DataTimeSeries(days.get(i), remainingNum));
          isFuture = false;
          continue;
        }
        remainingNum = remainingNum - (dayCompletedGroup.containsKey(days.get(i))
            ? dayCompletedGroup.get(days.get(i)).size() : 0);
        allRemainingNum.add(new DataTimeSeries(days.get(i), remainingNum));
      }
    }
    numChart.setExpected(allExpectedNum).setRemaining(allRemainingNum);
    chartMap.put(BurnDownResourceType.NUM, numChart);

    // Workload burndown chart
    BurnDownChartCount workloadChart = new BurnDownChartCount();
    BigDecimal totalWorkload = validTasks.stream().map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal completedWorkload = validTasks.stream().filter(x -> x.getStatus().isCompleted())
        .map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    workloadChart.setTotal(totalWorkload).setCompleted(completedWorkload)
        .setRemained(totalWorkload.subtract(completedWorkload))
        .setStartDate(safeCreatedDateStart).setEndDate(safeCreatedDateEnd);
    BigDecimal dayExpectedWorkload = ((BigDecimal) workloadChart.getTotal())
        .divide(BigDecimal.valueOf(days.size()), 2, RoundingMode.HALF_UP);
    List<DataTimeSeries> allExpectedWorkload = new ArrayList<>();
    List<DataTimeSeries> allRemainingWorkload = new ArrayList<>();
    BigDecimal remainingWorkload = (BigDecimal) workloadChart.getTotal();
    isFuture = true;
    for (int i = 0; i < days.size(); i++) {
      BigDecimal expectedWorkload = ((BigDecimal) workloadChart.getTotal())
          .subtract(dayExpectedWorkload.multiply(BigDecimal.valueOf(i + 1)));
      allExpectedWorkload.add(new DataTimeSeries(days.get(i),
          expectedWorkload.doubleValue() > 1 ? expectedWorkload : BigDecimal.ZERO));
      if (isFuture) {
        if (currentDay.equals(days.get(i))) {
          remainingWorkload = remainingWorkload.subtract(dayCompletedGroup.containsKey(days.get(i))
              ? dayCompletedGroup.get(days.get(i)).stream()
              .map(TaskEfficiencySummary::getEvalWorkload)
              .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO);
          allRemainingWorkload.add(new DataTimeSeries(days.get(0), remainingWorkload));
          isFuture = false;
          continue;
        }
        remainingWorkload = remainingWorkload.subtract(dayCompletedGroup.containsKey(days.get(i))
            ? dayCompletedGroup.get(days.get(i)).stream()
            .map(TaskEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO);
        allRemainingWorkload.add(new DataTimeSeries(days.get(0), remainingWorkload));
      }
    }
    workloadChart.setExpected(allExpectedWorkload).setRemaining(allRemainingWorkload);
    chartMap.put(BurnDownResourceType.WORKLOAD, workloadChart);
  }

  public static @NotNull BurnDownChartDetail assembleBurnDownChartDetail(
      String name, Map<BurnDownResourceType, BurnDownChartCount> downChartMap) {
    BurnDownChartDetail detail = new BurnDownChartDetail();
    detail.setName(name);
    BurnDownChartCount num = downChartMap.get(BurnDownResourceType.NUM);
    if (nonNull(num)) {
      detail.setTotalNum((Long) num.getTotal());
      detail.setCompletedNum((Long) num.getCompleted());
      detail.setRemainedNum((Long) num.getRemained());
    }
    BurnDownChartCount workload = downChartMap.get(BurnDownResourceType.WORKLOAD);
    if (nonNull(workload)) {
      detail.setTotalWorkload(((BigDecimal) workload.getTotal()).doubleValue());
      detail.setCompletedWorkload(((BigDecimal) workload.getCompleted()).doubleValue());
      detail.setRemainedWorkload(((BigDecimal) workload.getRemained()).doubleValue());
    }
    return detail;
  }

  public static void countCreationBacklog(TaskLastResourceCreationCount result,
      List<TaskEfficiencySummary> allTasks) {
    List<TaskEfficiencySummary> backlogs = allTasks.stream()
        .filter(TaskEfficiencySummary::getBacklogFlag).collect(Collectors.toList());
    result.setAllBacklog(backlogs.size())
        .setBacklogByLastWeek(backlogs.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setBacklogByLastMonth(
            backlogs.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<TaskType, List<TaskEfficiencySummary>> typeMap = backlogs.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getTaskType));
    for (TaskType value : TaskType.values()) {
      result.getBacklogByType().put(value, typeMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static void countCreationTask(TaskLastResourceCreationCount result,
      List<TaskEfficiencySummary> allTasks) {
    List<TaskEfficiencySummary> tasks = allTasks.stream()
        .filter(x -> !x.getBacklogFlag()).collect(Collectors.toList());
    result.setAllTask(tasks.size())
        .setTaskByOverdue(tasks.stream().filter(TaskEfficiencySummary::getOverdueFlag).count())
        .setTaskByLastWeek(tasks.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setTaskByLastMonth(
            tasks.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<TaskType, List<TaskEfficiencySummary>> typeMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getTaskType));
    for (TaskType value : TaskType.values()) {
      result.getTaskByType().put(value, typeMap.getOrDefault(value, emptyList()).size());
    }
    Map<TaskStatus, List<TaskEfficiencySummary>> statusMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getStatus));
    for (TaskStatus value : TaskStatus.values()) {
      result.getTaskByStatus().put(value, statusMap.getOrDefault(value, emptyList()).size());
    }
    Map<Priority, List<TaskEfficiencySummary>> priorityMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getPriority));
    for (Priority value : Priority.values()) {
      result.getTaskByPriority().put(value, priorityMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static void countCreationSprint(TaskLastResourceCreationCount result,
      List<TaskSprint> sprints) {
    result.setAllSprint(sprints.size())
        .setSprintByLastWeek(sprints.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setSprintByLastMonth(
            sprints.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<TaskSprintStatus, List<TaskSprint>> statusMap = sprints.stream()
        .collect(Collectors.groupingBy(TaskSprint::getStatus));
    for (TaskSprintStatus value : TaskSprintStatus.values()) {
      result.getSprintByStatus().put(value, statusMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static void countCreationMeeting(TaskLastResourceCreationCount result,
      List<TaskMeeting> meetings) {
    result.setAllMeeting(meetings.size())
        .setMeetingByLastWeek(meetings.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setMeetingByLastMonth(
            meetings.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<TaskMeetingType, List<TaskMeeting>> typeMap = meetings.stream()
        .collect(Collectors.groupingBy(TaskMeeting::getType));
    for (TaskMeetingType value : TaskMeetingType.values()) {
      result.getMeetingByType().put(value, typeMap.getOrDefault(value, emptyList()).size());
    }
  }

  public static @NotNull TaskAssigneeCount assembleTaskAssigneeCount(Long assigneeId,
      Map<Long, UserBase> userMaps, Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap) {
    TaskAssigneeCount count = new TaskAssigneeCount();
    UserBase assignee = userMaps.get(assigneeId);
    count.setAssigneeId(assigneeId);
    count.setAssigneeName(nonNull(assignee) ? assignee.getFullname() : null);
    count.setAssigneeAvatar(nonNull(assignee) ? assignee.getAvatar() : null);

    List<TaskEfficiencySummary> assigneeTasks = assigneeTaskMap.get(assigneeId);
    count.setTotalTaskNum(assigneeTasks.size());
    List<TaskEfficiencySummary> validAssigneeTasks = assigneeTasks.stream()
        .filter(x -> !x.getStatus().isCanceled()).collect(Collectors.toList());
    count.setValidTaskNum(validAssigneeTasks.size());

    count.setEvalWorkload(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setActualWorkload(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setSavingWorkload(count.getEvalWorkload() - count.getActualWorkload());
    count.setSavingWorkloadRate(calcRate(count.getSavingWorkload(), count.getEvalWorkload()));

    count.setProcessTimes(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getTotalNum)
            .reduce(0, Integer::sum));
    count.setProcessFailTimes(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getFailNum)
            .reduce(0, Integer::sum));
    count.setProcessFailRate(
        calcRate(count.getProcessFailTimes(), count.getProcessTimes()));

    count.setCompletedNum(
        validAssigneeTasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getValidTaskNum()));

    count.setOneTimePassedNum(validAssigneeTasks.stream()
        .filter(x -> x.getStatus().isCompleted() && x.getFailNum() == 0).count());
    count.setOneTimePassedRate(
        calcRate(count.getOneTimePassedNum(), count.getValidTaskNum()));

    count.setOverdueNum(
        validAssigneeTasks.stream().filter(TaskEfficiencySummary::getOverdueFlag).count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getValidTaskNum()));
    return count;
  }

  public static @NotNull TaskAssigneeProgressCount assembleTaskAssigneeProgressCount(
      Long assigneeId, Map<Long, UserBase> userMaps,
      Map<Long, List<TaskEfficiencySummary>> assigneeTaskMap) {
    TaskAssigneeProgressCount count = new TaskAssigneeProgressCount();
    UserBase assignee = userMaps.get(assigneeId);
    count.setAssigneeId(assigneeId);
    count.setAssigneeName(nonNull(assignee) ? assignee.getFullname() : null);
    count.setAssigneeAvatar(nonNull(assignee) ? assignee.getAvatar() : null);

    List<TaskEfficiencySummary> assigneeTasks = assigneeTaskMap.get(assigneeId);
    count.setTotalTaskNum(assigneeTasks.size());
    List<TaskEfficiencySummary> validAssigneeTasks = assigneeTasks.stream()
        .filter(x -> !x.getStatus().isCanceled()).collect(Collectors.toList());
    count.setValidTaskNum(validAssigneeTasks.size());

    count.setEvalWorkload(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setActualWorkload(
        validAssigneeTasks.stream().map(TaskEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkload(validAssigneeTasks.stream().filter(x -> x.getStatus().isCompleted())
        .map(TaskEfficiencySummary::getActualWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));

    count.setCompletedNum(
        validAssigneeTasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getValidTaskNum()));

    count.setOverdueNum(
        validAssigneeTasks.stream().filter(TaskEfficiencySummary::getOverdueFlag).count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getValidTaskNum()));
    return count;
  }

  public static ProgressCount assembleTaskProgressCount0(List<TaskInfo> tasks) {
    ProgressCount count = new ProgressCount();
    count.setTotalNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    count.setCompletedNum(tasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getTotalNum()));

    count.setEvalWorkload(tasks.stream()
        .filter(x -> !x.getStatus().isCanceled() && nonNull(x.getEvalWorkload()))
        .map(TaskInfo::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkload(
        tasks.stream().filter(x -> x.getStatus().isCompleted() && nonNull(x.getActualWorkload()))
            .map(TaskInfo::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));
    return count;
  }

  public static List<Task> findAllSubTasks(List<Task> tasks, Long parentTaskId) {
    List<Task> allSubTasks = new ArrayList<>();
    List<Task> directSubTasks = findSubTasks(tasks, parentTaskId);

    for (Task subTask : directSubTasks) {
      allSubTasks.add(subTask);
      allSubTasks.addAll(findAllSubTasks(tasks, subTask.getId()));
    }

    return allSubTasks;
  }

  private static List<Task> findSubTasks(List<Task> tasks, Long parentTaskId) {
    List<Task> subTasks = new ArrayList<>();
    for (Task task : tasks) {
      if (parentTaskId.equals(task.getParentTaskId())) {
        subTasks.add(task);
      }
    }
    return subTasks;
  }

  public static List<TaskInfo> findAllSubTaskInfos(List<TaskInfo> tasks, Long parentTaskId) {
    List<TaskInfo> allSubTasks = new ArrayList<>();
    List<TaskInfo> directSubTasks = findSubTaskInfos(tasks, parentTaskId);

    for (TaskInfo subTask : directSubTasks) {
      allSubTasks.add(subTask);
      allSubTasks.addAll(findAllSubTaskInfos(tasks, subTask.getId()));
    }
    return allSubTasks;
  }

  private static List<TaskInfo> findSubTaskInfos(List<TaskInfo> tasks, Long parentTaskId) {
    List<TaskInfo> subTasks = new ArrayList<>();
    for (TaskInfo task : tasks) {
      if (parentTaskId.equals(task.getParentTaskId())) {
        subTasks.add(task);
      }
    }
    return subTasks;
  }

  public static Set<SearchCriteria> getTaskAssigneeResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate, Set<Long> assigneeIds) {
    Set<SearchCriteria> filters = getTaskResourcesFilter(
        projectId, sprintId, startDate, endDate);
    if (isNotEmpty(assigneeIds)) {
      filters.add(in("assigneeId", assigneeIds));
    } else {
      filters.add(isNotNull("assigneeId"));
    }
    return filters;
  }

  public static Set<SearchCriteria> getTaskCreatorResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdIds) {
    Set<SearchCriteria> filters = getTaskResourcesFilter(projectId, sprintId, startDate, endDate);
    if (isNotEmpty(createdIds)) {
      filters.add(in("createdBy", createdIds));
    }
    return filters;
  }

  public static @NotNull Set<SearchCriteria> getTaskResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(sprintId)) {
      filters.add(equal("sprintId", sprintId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    filters.add(equal("deletedFlag", false));
    filters.add(equal("sprintDeletedFlag", false));
    return filters;
  }
}
