package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.toAddTask;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.toReplaceTask;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.toTaskDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.toTaskListExportResource;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler.toUpdateTask;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_REPORT_ROWS;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_CODE;
import static cloud.xcan.angus.remote.CommonMessage.EXPORT_ROW_OVERT_LIMIT_T;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagTargetCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAssigneeReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskAttachmentReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskConfirmerReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskDescriptionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskImportDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskMoveDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskTagReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.TaskWorkloadReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskListExportVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public IdKey<Long, Object> add(TaskAddDto dto) {
    return taskCmd.add(toAddTask(dto));
  }

  @Override
  public void update(Long id, TaskUpdateDto dto) {
    taskCmd.update(toUpdateTask(id, dto));
  }

  @Override
  public void replace(Long id, TaskReplaceDto dto) {
    taskCmd.replace(toReplaceTask(id, dto));
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
  public void replaceMissingBug(Long id, Boolean missingBug) {
    taskCmd.replaceMissingBug(id, missingBug);
  }

  @Override
  public void replaceAssignee(Long id, TaskAssigneeReplaceDto dto) {
    taskCmd.replaceAssignees(id, isNull(dto) ? null : dto.getAssigneeId());
  }

  @Override
  public void replaceConfirmer(Long id, TaskConfirmerReplaceDto dto) {
    taskCmd.replaceConfirmers(id, isNull(dto) ? null : dto.getConfirmerId());
  }

  @Override
  public void replaceTag(Long id, TaskTagReplaceDto dto) {
    tagTargetCmd.replaceTaskTags(id, isNull(dto) ? null : dto.getTagIds());
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
  public void replaceSoftwareVersion(Long id, SoftwareVersionRefReplaceDto dto) {
    taskCmd.replaceSoftwareVersion(id,  Objects.isNull(dto) ? null : dto.getSoftwareVersion());
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
        .toList();
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
        .toList();
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
        .toList();
  }

  @Override
  public void restart(Long id) {
    taskCmd.restart(Collections.singletonList(id));
  }

  @Override
  public void reopen(Long id) {
    taskCmd.reopen(Collections.singletonList(id));
  }

  @Override
  public List<IdKey<Long, Object>> imports(TaskImportDto dto) {
    return taskCmd.imports(dto.getProjectId(), dto.getSprintId(),
        dto.getStrategyWhenDuplicated(), dto.getFile());
  }

  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return taskCmd.importExample(projectId);
  }

  @Override
  public void delete(Collection<Long> ids) {
    taskCmd.delete(new ArrayList<>(ids));
  }

  @NameJoin
  @Override
  public TaskDetailVo detail(Long id) {
    TaskDetailVo detailVo = toTaskDetailVo(taskQuery.detail(id));
    detailVo.setSubTaskInfos(joinSupplier.execute(() -> joinSubTaskInfos(detailVo)));
    return detailVo;
  }

  @NameJoin
  public List<TaskInfoVo> joinSubTaskInfos(TaskDetailVo detailVo) {
    return detailVo.getSubTaskInfos();
  }

  @NameJoin
  @Override
  public PageResult<TaskListVo> list(boolean export, TaskFindDto dto) {
    Page<Task> page = taskQuery.list(export, getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskAssembler::toListVo);
  }

  /**
   * Note: The default maximum number of exported records, which needs to be exported in batches
   * according to conditions(created_date) when exceeding the limit.
   */
  @DoInFuture("Limit the number of export tasks")
  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(TaskFindDto dto,
      HttpServletResponse response) {
    List<TaskListExportVo> data = getExportTaskData(dto);
    String fileName = "TaskListExport-" + System.currentTimeMillis() + ".xlsx";
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName, 0, toTaskListExportResource(data, fileName));
  }

  @NotNull
  private List<TaskListExportVo> getExportTaskData(TaskFindDto dto) {
    dto.setPageSize(500);
    PageResult<TaskListVo> page = joinSupplier.execute(() -> list(true, dto));
    // 500 reads per time, with a maximum support for exporting MAX_REPORT_ROWS.
    BizAssert.assertTrue(page.getTotal() <= MAX_REPORT_ROWS,
        EXPORT_ROW_OVERT_LIMIT_CODE, EXPORT_ROW_OVERT_LIMIT_T, new Object[]{MAX_REPORT_ROWS});
    List<TaskListExportVo> data = page.getList().stream().map(TaskAssembler::toTaskVo)
        .collect(Collectors.toList());
    while (page.getList().size() >= 500) {
      dto.setPageNo(dto.getPageNo() + 1);
      page = list(true, dto);
      if (!page.isEmpty()) {
        data.addAll(page.getList().stream().map(TaskAssembler::toTaskVo).toList());
      }
    }
    return data;
  }

}
