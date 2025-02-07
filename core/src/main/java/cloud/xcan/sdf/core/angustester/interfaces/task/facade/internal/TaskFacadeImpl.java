package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.api.ApiConstant.RLimit.MAX_REPORT_ROWS;
import static cloud.xcan.sdf.api.CommonMessage.EXPORT_ROW_OVERT_LIMIT_CODE;
import static cloud.xcan.sdf.api.CommonMessage.EXPORT_ROW_OVERT_LIMIT_T;
import static cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskAssembler.toTaskListExportResource;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.sdf.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskSearch;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAssigneeReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskAttachmentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskConfirmorReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskDescriptionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskMoveDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskTagReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskVersionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListExportVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskFacadeImpl implements TaskFacade {

  @Resource
  private TaskCmd taskCmd;

  @Resource
  private TagTargetCmd tagTargetCmd;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private TaskSearch taskSearch;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public IdKey<Long, Object> add(TaskAddDto dto) {
    return taskCmd.add(TaskAssembler.toAddTask(dto));
  }

  @Override
  public void update(Long id, TaskUpdateDto dto) {
    taskCmd.update(TaskAssembler.toUpdateTask(id, dto));
  }

  @Override
  public void replace(Long id, TaskReplaceDto dto) {
    taskCmd.replace(TaskAssembler.toReplaceTask(id, dto));
  }

  @Override
  public void rename(Long id, String name) {
    taskCmd.rename(id, name);
  }

  @Override
  public void move(TaskMoveDto dto) {
    taskCmd.move(dto.getTaskIds(), dto.getTargetSprintId());
  }

  @Override
  public void replaceType(Long id, String type) {
    taskCmd.replaceType(id, type);
  }

  @Override
  public void replaceBugLevel(Long id, String bugLevel) {
    taskCmd.replaceBugLevel(id, bugLevel);
  }

  @Override
  public void replaceMissingBugFlag(Long id, Boolean missingBugFlag) {
    taskCmd.replaceMissingBugFlag(id, missingBugFlag);
  }

  @Override
  public void replaceAssignee(Long id, TaskAssigneeReplaceDto dto) {
    taskCmd.replaceAssignees(id, Objects.isNull(dto) ? null : dto.getAssigneeId());
  }

  @Override
  public void replaceConfirmor(Long id, TaskConfirmorReplaceDto dto) {
    taskCmd.replaceConfirmors(id, Objects.isNull(dto) ? null : dto.getConfirmorId());
  }

  @Override
  public void replaceTag(Long id, TaskTagReplaceDto dto) {
    tagTargetCmd.replaceTaskTags(id, Objects.isNull(dto) ? null : dto.getTagIds());
  }

  @Override
  public void replaceDeadline(Long id, LocalDateTime deadlineDate) {
    taskCmd.replaceDeadline(id, deadlineDate);
  }

  @Override
  public void replacePriority(Long id, Priority priority) {
    taskCmd.replacePriority(id, priority);
  }

  @Override
  public void replaceVersion(Long id, TaskVersionReplaceDto dto) {
    taskCmd.replaceVersion(id,  Objects.isNull(dto) ? null : dto.getSoftwareVersion());
  }

  @Override
  public void replaceEvalWorkload(Long id, TaskWorkloadReplaceDto dto) {
    taskCmd.replaceEvalWorkload(id, Objects.isNull(dto) ? null : dto.getWorkload());
  }

  @Override
  public void replaceActualWorkload(Long id, TaskWorkloadReplaceDto dto) {
    taskCmd.replaceActualWorkload(id, Objects.isNull(dto) ? null : dto.getWorkload());
  }

  @Override
  public void replaceDescription(Long id, TaskDescriptionReplaceDto dto) {
    taskCmd.replaceDescription(id, Objects.isNull(dto) ? null : dto.getDescription());
  }

  @Override
  public void replaceAttachment(Long id, TaskAttachmentReplaceDto dto) {
    taskCmd.replaceAttachment(id, Objects.isNull(dto) ? null : dto.getAttachments());
  }

  @Override
  public void start(Long id) {
    taskCmd.start(id);
  }

  @Override
  public void cancel(Long id) {
    taskCmd.cancel(id);
  }

  @Override
  public void processed(Long id) {
    taskCmd.processed(id);
  }

  @Override
  public void confirm(Long id, Result result, BigDecimal evalWorkload,
      BigDecimal actualWorkload) {
    taskCmd.confirm(id, result, evalWorkload, actualWorkload);
  }

  @Override
  public void complete(Long id, BigDecimal evalWorkload, BigDecimal actualWorkload) {
    taskCmd.confirm(id, Result.SUCCESS, evalWorkload, actualWorkload);
  }

  @Override
  public void subtaskSet(Long id, HashSet<Long> subTaskIds) {
    taskCmd.subtaskSet(id, subTaskIds);
  }

  @Override
  public void subtaskCancel(Long id, HashSet<Long> subTaskIds) {
    taskCmd.subtaskCancel(id, subTaskIds);
  }

  @Override
  public List<TaskInfoVo> notAssociatedSubtask(Long id, Long moduleId) {
    List<TaskInfo> caseInfos = taskQuery.notAssociatedSubtask(id, moduleId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(TaskAssembler::toInfoVo)
        .collect(Collectors.toList());
  }

  @Override
  public void taskAssocAdd(Long id, HashSet<Long> assocTaskIds) {
    taskCmd.taskAssocAdd(id, assocTaskIds);
  }

  @Override
  public void taskAssocCancel(Long id, HashSet<Long> assocTaskIds) {
    taskCmd.taskAssocCancel(id, assocTaskIds);
  }

  @NameJoin
  @Override
  public List<TaskInfoVo> notAssociatedTask(Long id, Long moduleId, TaskType taskType) {
    List<TaskInfo> caseInfos = taskQuery.notAssociatedTaskInTask(id, moduleId, taskType);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(TaskAssembler::toInfoVo)
        .collect(Collectors.toList());
  }

  @Override
  public void caseAssocAdd(Long id, HashSet<Long> assocCaseIds) {
    taskCmd.caseAssocAdd(id, assocCaseIds);
  }

  @Override
  public void caseAssocCancel(Long id, HashSet<Long> assocCaseIds) {
    taskCmd.caseAssocCancel(id, assocCaseIds);
  }

  @NameJoin
  @Override
  public List<FuncCaseListVo> notAssociatedCase(Long id, Long moduleId) {
    List<FuncCaseInfo> caseInfos = funcCaseQuery.notAssociatedCaseInTask(id, moduleId);
    return isEmpty(caseInfos) ? null : caseInfos.stream().map(FuncCaseAssembler::toListVo)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Collection<Long> ids) {
    taskCmd.delete(new ArrayList<>(ids));
  }

  @Override
  public void restart(Long id) {
    taskCmd.restart(Collections.singletonList(id));
  }

  @Override
  public void reopen(Long id) {
    taskCmd.reopen(Collections.singletonList(id));
  }

  @NameJoin
  @Override
  public TaskDetailVo detail(Long id) {
    TaskDetailVo detailVo = TaskAssembler.toTaskDetailVo(taskQuery.detail(id));
    detailVo.setSubTaskInfos(joinSupplier.execute(() -> joinSubTaskInfos(detailVo)));
    return detailVo;
  }

  @NameJoin
  public List<TaskInfoVo> joinSubTaskInfos(TaskDetailVo detailVo) {
    return detailVo.getSubTaskInfos();
  }

  @NameJoin
  @Override
  public PageResult<TaskListVo> list(TaskFindDto dto) {
    Page<Task> page = taskQuery.find(TaskAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TaskAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<TaskListVo> search(boolean exportFlag, TaskSearchDto dto) {
    Page<Task> page = taskSearch.search(exportFlag, TaskAssembler.getSearchCriteria(dto),
        dto.tranPage(), getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskAssembler::toListVo);
  }

  @Override
  public List<IdKey<Long, Object>> sampleImport(Long projectId) {
    return taskCmd.sampleImport(projectId);
  }

  @Override
  public List<IdKey<Long, Object>> imports(TaskImportDto dto) {
    return taskCmd.imports(dto.getProjectId(), dto.getSprintId(),
        dto.getStrategyWhenDuplicated(), dto.getFile());
  }

  /**
   * Note: The default maximum number of exported records, which needs to be exported in batches
   * according to conditions(created_date) when exceeding the limit.
   */
  @DoInFuture("Limit the number of export tasks")
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(TaskSearchDto dto,
      HttpServletResponse response) {
    List<TaskListExportVo> data = getExportTaskData(dto);
    String fileName = "TaskListExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName, 0, toTaskListExportResource(data, fileName));
  }

  @NotNull
  private List<TaskListExportVo> getExportTaskData(TaskSearchDto dto) {
    dto.setPageSize(500);
    PageResult<TaskListVo> page = joinSupplier.execute(() -> search(true, dto));
    // 500 reads per time, with a maximum support for exporting MAX_REPORT_ROWS.
    BizAssert.assertTrue(page.getTotal() <= MAX_REPORT_ROWS,
        EXPORT_ROW_OVERT_LIMIT_CODE, EXPORT_ROW_OVERT_LIMIT_T, new Object[]{MAX_REPORT_ROWS});
    List<TaskListExportVo> data = page.getList().stream().map(TaskAssembler::toTaskVo)
        .collect(Collectors.toList());
    while (page.getList().size() >= 500) {
      dto.setPageNo(dto.getPageNo() + 1);
      page = search(true, dto);
      if (!page.isEmpty()) {
        data.addAll(page.getList().stream().map(TaskAssembler::toTaskVo)
            .collect(Collectors.toList()));
      }
    }
    return data;
  }

}
