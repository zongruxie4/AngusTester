package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler;

import static cloud.xcan.sdf.core.angustester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.EXPORT_TASK_LIST;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.arrayToLists;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.vo.IdAndNameVo;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListExportVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.core.utils.SpringAppDirUtils;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.utils.FileUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;

/**
 * @author xiaolong.liu
 */
public class TaskAssembler {

  public static Task toAddTask(TaskAddDto dto) {
    CachedUidGenerator uidGenerator = getBean(CachedUidGenerator.class);
    Task task = new Task()
        .setId(uidGenerator.getUID())
        .setProjectId(isNull(dto.getSprintId()) ? dto.getProjectId() : null)
        .setSprintId(dto.getSprintId())
        .setModuleId(dto.getModuleId())
        .setBacklogFlag(isNull(dto.getSprintId()))
        .setCode(getTaskCode())
        .setName(dto.getName())
        .setSoftwareVersion(dto.getSoftwareVersion())
        .setTargetId(dto.getTargetId())
        .setStatus(TaskStatus.PENDING)
        .setTaskType(dto.getTaskType())
        .setBugLevel(dto.getTaskType().isBug()
            ? nullSafe(dto.getBugLevel(), BugLevel.DEFAULT) : null)
        .setTestType(dto.getTestType())
        .setAssigneeId(dto.getAssigneeId())
        .setConfirmorId(dto.getConfirmorId())
        .setTesterId(dto.getTesterId())
        .setMissingBugFlag(dto.getTaskType().isBug()
            ? nullSafe(dto.getMissingBugFlag(), false) : null)
        .setPriority(nullSafe(dto.getPriority(), Priority.DEFAULT))
        .setDeadlineDate(dto.getDeadlineDate())
        .setOverdueFlag(false)
        .setTotalNum(0).setFailNum(0)
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getEvalWorkload())
        .setParentTaskId(dto.getParentTaskId())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds())
        .setSprintDeletedFlag(false)
        .setDeletedFlag(false);
    if (isNotEmpty(dto.getTagIds())) {
      List<TagTarget> tags = dto.getTagIds().stream()
          .map(tagId -> new TagTarget().setId(uidGenerator.getUID())
              .setTargetType(CombinedTargetType.TASK)
              .setTargetId(task.getId()).setTagId(tagId))
          .collect(Collectors.toList());
      task.setTagTargets(tags);
    }
    return task;
  }

  public static Task toUpdateTask(Long taskId, TaskUpdateDto dto) {
    Task task = new Task().setId(taskId)
        .setModuleId(dto.getModuleId())
        .setName(dto.getName())
        .setSoftwareVersion(dto.getSoftwareVersion())
        .setTaskType(dto.getTaskType())
        .setBugLevel(nonNull(dto.getTaskType()) && dto.getTaskType().isBug()
            ? nullSafe(dto.getBugLevel(), BugLevel.DEFAULT) : null)
        .setAssigneeId(dto.getAssigneeId())
        .setConfirmorId(dto.getConfirmorId())
        .setTesterId(dto.getTesterId())
        .setMissingBugFlag(nonNull(dto.getTaskType()) && dto.getTaskType().isBug()
            ? dto.getMissingBugFlag() : null)
        .setPriority(dto.getPriority())
        .setDeadlineDate(dto.getDeadlineDate())
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getActualWorkload())
        .setParentTaskId(dto.getParentTaskId())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds());
    if (isNotEmpty(dto.getTagIds())) {
      List<TagTarget> tags = dto.getTagIds().stream()
          .map(tagId -> new TagTarget()
              .setTargetType(CombinedTargetType.TASK)
              .setTargetId(task.getId()).setTagId(tagId))
          .collect(Collectors.toList());
      task.setTagTargets(tags);
    }
    return task;
  }

  public static Task toReplaceTask(Long taskId, TaskReplaceDto dto) {
    Task task = new Task()
        .setId(taskId)
        .setModuleId(dto.getModuleId())
        .setName(dto.getName())
        .setSoftwareVersion(dto.getSoftwareVersion())
        .setTaskType(dto.getTaskType())
        .setBugLevel(nonNull(dto.getTaskType()) && dto.getTaskType().isBug()
            ? nullSafe(dto.getBugLevel(), BugLevel.DEFAULT) : null)
        .setAssigneeId(dto.getAssigneeId())
        .setConfirmorId(dto.getConfirmorId())
        .setTesterId(dto.getTesterId())
        .setMissingBugFlag(nonNull(dto.getTaskType()) && dto.getTaskType().isBug()
            ? nullSafe(dto.getMissingBugFlag(), false) : null)
        .setPriority(nullSafe(dto.getPriority(), Priority.DEFAULT))
        .setDeadlineDate(dto.getDeadlineDate())
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription())
        .setEvalWorkload(dto.getEvalWorkload())
        .setActualWorkload(dto.getActualWorkload())
        .setParentTaskId(dto.getParentTaskId())
        .setRefTaskIds(dto.getRefTaskIds())
        .setRefCaseIds(dto.getRefCaseIds());
    if (isNotEmpty(dto.getTagIds())) {
      List<TagTarget> tags = dto.getTagIds().stream()
          .map(tagId -> new TagTarget()
              .setTargetType(CombinedTargetType.TASK)
              .setTargetId(task.getId()).setTagId(tagId))
          .collect(Collectors.toList());
      task.setTagTargets(tags);
    }
    return task;
  }

  public static TaskDetailVo toTaskDetailVo(Task task) {
    return new TaskDetailVo().setId(task.getId())
        .setName(task.getName())
        .setCode(task.getCode())
        .setSoftwareVersion(task.getSoftwareVersion())
        .setTaskType(task.getTaskType())
        .setBugLevel(task.getBugLevel())
        .setTestType(task.getTestType())
        .setProjectId(task.getProjectId())
        .setSprintId(task.getSprintId())
        .setSprintAuthFlag(task.getSprintAuthFlag())
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
        .setParentTaskId(task.getParentTaskId())
        .setSubTaskProgress(isEmpty(task.getSubTasks()) ? new Progress()
            : new Progress().setCompleted(
                    task.getSubTasks().stream().filter(x -> x.getStatus().isFinished()).count())
                .setTotal(task.getSubTasks().size()))
        .setSubTaskInfos(isNotEmpty(task.getSubTasks()) ? task.getSubTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefTaskInfos(isNotEmpty(task.getAssocTasks()) ? task.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefCaseInfos(isNotEmpty(task.getAssocCases()) ? task.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setTags(isNotEmpty(task.getTagTargets()) ? task.getTagTargets().stream()
            .map(TaskAssembler::toTagVo).collect(Collectors.toList()) : emptyList())
        .setCurrentAssociateType(task.getCurrentAssociateType())
        .setConfirmTaskFlag(task.isConfirmTask())
        .setOverdueFlag(task.getOverdueFlag())
        .setAttachments(task.getAttachments())
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
        .setFavouriteFlag(task.getFavouriteFlag())
        .setFollowFlag(task.getFollowFlag())
        .setCommentNum(task.getCommentNum())
        .setRemarkNum(task.getRemarkNum())
        .setCreatedBy(task.getCreatedBy())
        .setCreatedDate(task.getCreatedDate())
        .setLastModifiedBy(task.getLastModifiedBy())
        .setLastModifiedDate(task.getLastModifiedDate());
  }

  public static IdAndNameVo toTagVo(TagTarget o) {
    return new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName());
  }

  public static TaskInfoVo toInfoVo(TaskInfo task) {
    return new TaskInfoVo().setId(task.getId())
        .setName(task.getName())
        .setCode(task.getCode())
        .setVersion(task.getSoftwareVersion())
        .setStatus(task.getStatus())
        .setProjectId(task.getProjectId())
        .setSprintId(task.getSprintId())
        .setPriority(task.getPriority())
        .setTaskType(task.getTaskType())
        .setBugLevel(task.getBugLevel())
        .setTestType(task.getTestType())
        .setEvalWorkload(task.getEvalWorkload())
        .setStartDate(task.getStartDate())
        .setDeadlineDate(task.getDeadlineDate())
        .setAssigneeId(task.getAssigneeId())
        .setAssigneeName(task.getAssigneeName())
        .setAssigneeAvatar(task.getAssigneeAvatar())
        .setConfirmorId(task.getConfirmorId())
        .setTesterId(task.getTesterId())
        .setMissingBugFlag(task.getMissingBugFlag())
        .setUnplannedFlag(task.getUnplannedFlag())
        .setFavouriteFlag(task.getFavouriteFlag())
        .setFollowFlag(task.getFollowFlag())
        .setCreatedBy(task.getCreatedBy())
        .setLastModifiedBy(task.getLastModifiedBy());
  }

  public static TaskListVo toListVo(Task task) {
    return new TaskListVo().setId(task.getId())
        .setName(task.getName())
        .setCode(task.getCode())
        .setSoftwareVersion(task.getSoftwareVersion())
        .setTaskType(task.getTaskType())
        .setBugLevel(task.getBugLevel())
        .setTestType(task.getTestType())
        .setProjectId(task.getProjectId())
        .setSprintId(task.getSprintId())
        .setSprintAuthFlag(task.getSprintAuthFlag())
        .setModuleId(task.getModuleId())
        .setBacklogFlag(task.getBacklogFlag())
        .setPriority(task.getPriority())
        .setStatus(task.getStatus())
        .setAssigneeId(task.getAssigneeId())
        .setAssigneeName(task.getAssigneeName())
        .setAssigneeAvatar(task.getAssigneeAvatar())
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
        .setParentTaskId(task.getParentTaskId())
        .setTags(isNotEmpty(task.getTagTargets()) ? task.getTagTargets().stream()
            .map(TaskAssembler::toTagVo).collect(Collectors.toList()) : emptyList())
        .setRefCaseInfos(isNotEmpty(task.getAssocCases()) ? task.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
        .setRefTaskInfos(isNotEmpty(task.getAssocTasks()) ? task.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).collect(Collectors.toList()) : emptyList())
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
        .setFavouriteFlag(task.getFavouriteFlag())
        .setFollowFlag(task.getFollowFlag())
        .setCreatedBy(task.getCreatedBy())
        .setCreatedDate(task.getCreatedDate())
        .setLastModifiedBy(task.getLastModifiedBy())
        .setLastModifiedDate(task.getLastModifiedDate());
  }

  public static TaskListExportVo toTaskVo(TaskListVo listVo) {
    return new TaskListExportVo()
        .setName(listVo.getName())
        .setCode(listVo.getCode())
        //.setVersion(listVo.getVersion())
        .setTaskType(listVo.getTaskType())
        .setBugLevel(listVo.getBugLevel())
        .setTestType(listVo.getTestType())
        .setProjectName(listVo.getProjectName())
        .setSprintName(listVo.getSprintName())
        .setModuleName(listVo.getModuleName())
        .setTargetId(listVo.getTargetId())
        .setTargetName(listVo.getTargetName())
        .setPriority(listVo.getPriority())
        .setAssigneeName(listVo.getAssigneeName())
        .setConfirmorName(listVo.getConfirmorName())
        .setTesterName(listVo.getTesterName())
        .setMissingBugFlag(listVo.getMissingBugFlag())
        .setUnplannedFlag(listVo.getUnplannedFlag())
        .setStartDate(listVo.getStartDate())
        .setDeadlineDate(listVo.getDeadlineDate())
        .setCompletedDate(listVo.getCompletedDate())
        .setCanceledDate(listVo.getCanceledDate())
        .setConfirmedDate(listVo.getConfirmedDate())
        .setProcessedDate(listVo.getProcessedDate())
        .setEvalWorkloadMethod(listVo.getEvalWorkloadMethod())
        .setEvalWorkload(listVo.getEvalWorkload())
        .setActualWorkload(listVo.getActualWorkload())
        .setStatus(listVo.getStatus())
        .setOverdueFlag(listVo.getOverdueFlag())
        .setExecResult(listVo.getExecResult())
        .setExecFailureMessage(listVo.getExecFailureMessage())
        .setExecTestNum(listVo.getExecTestNum())
        .setExecTestFailureNum(listVo.getExecTestFailureNum())
        //.setExecId(listVo.getExecId())
        .setExecName(listVo.getExecName())
        //.setExecBy(listVo.getExecBy())
        .setExecDate(listVo.getExecDate())
        .setFailNum(listVo.getFailNum())
        .setTotalNum(listVo.getTotalNum())
        .setRefTasks(isNotEmpty(listVo.getRefTaskInfos()) ? listVo.getRefTaskInfos().stream()
            .map(TaskInfoVo::getName).collect(Collectors.toList()) : emptyList())
        .setRefCases(isNotEmpty(listVo.getRefCaseInfos()) ? listVo.getRefCaseInfos().stream()
            .map(FuncCaseInfoVo::getName).collect(Collectors.toList()) : emptyList())
        .setRefTags(isNotEmpty(listVo.getTags()) ? listVo.getTags().stream()
            .map(IdAndNameVo::getName).collect(Collectors.toList()) : emptyList())
        .setDescription(listVo.getDescription())
        .setCreatedByName(listVo.getCreatedByName())
        .setCreatedDate(listVo.getCreatedDate())
        .setLastModifiedByName(listVo.getLastModifiedByName())
        .setLastModifiedDate(listVo.getLastModifiedDate());
  }

  public static TaskAssocVo toTaskAssocVo(TaskInfo task) {
    return new TaskAssocVo().setId(task.getId())
        .setName(task.getName())
        .setCode(task.getCode())
        .setTaskType(task.getTaskType())
        .setTestType(task.getTestType())
        /*.setProjectId(task.getProjectId())*/
        .setSprintId(task.getSprintId())
        .setTargetId(task.getTargetId())
        .setTargetName(task.getTargetName())
        .setStatus(task.getStatus())
        .setAssigneeId(task.getAssigneeId())
        .setConfirmorId(task.getConfirmorId())
        /*.setTags(isNotEmpty(task.getTagTargets()) ? task.getTagTargets().stream().map(
                o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .collect(Collectors.toList()) : Collections.emptyList())*/
        .setCreatedBy(task.getCreatedBy())
        .setCreatedDate(task.getCreatedDate());
  }

  @SneakyThrows
  @NotNull
  public static InputStreamResource toTaskListExportResource(List<TaskListExportVo> counts,
      String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    String headerMessage = MessageHolder.message(EXPORT_TASK_LIST);
    Assert.assertNotEmpty(headerMessage, "TaskListExport message not configured");
    EasyExcel.write(file, TaskListExportVo.class)
        .head(arrayToLists(headerMessage.split(","))).sheet()
        //.head(ApisCaseListVo.class).sheet()
        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
        .doWrite(() -> counts);
    return new InputStreamResource(new FileInputStream(filePath));
  }

  public static GenericSpecification<Task> getSpecification(TaskFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "code")
        .rangeSearchFields("startDate", "deadlineDate", "confirmedDate", "completedDate",
            "processedDate", "canceledDate", "execDate", "createdDate", "lastModifiedDate",
            "failNum", "totalNum", "evalWorkload", "actualWorkload")
        .inAndNotFields("id", "tagId", "status", "assigneeId", "confirmorId", "moduleId")
        .orderByFields("id", "name", "code", "createdDate", "taskType", "testType", "bugLevel",
            "startDate", "deadlineDate", "priority", "status", "evalWorkload", "actualWorkload",
            "failNum", "execResult", "moduleId")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(TaskSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "code")
        .rangeSearchFields("startDate", "deadlineDate", "confirmedDate", "completedDate",
            "processedDate", "canceledDate", "execDate", "createdDate", "lastModifiedDate",
            "failNum", "totalNum", "evalWorkload", "actualWorkload")
        .inAndNotFields("id", "tagId", "status", "assigneeId", "confirmorId", "moduleId")
        .orderByFields("id", "name", "code", "createdDate", "taskType", "testType", "bugLevel",
            "startDate", "deadlineDate", "priority", "status", "evalWorkload", "actualWorkload",
            "failNum", "totalNum", "execStatus", "execResult", "moduleId")
        .build();
  }
}
