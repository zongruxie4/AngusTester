package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
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
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionRefReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.TaskWorkloadReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface TaskFacade {

  IdKey<Long, Object> add(TaskAddDto dto);

  void update(Long id, TaskUpdateDto dto);

  void replace(Long id, TaskReplaceDto dto);

  void rename(Long id, String name);

  void move(TaskMoveDto dto);

  void replaceType(Long id, String type);

  void replaceBugLevel(Long id, String bugLevel);

  void replaceMissingBugFlag(Long id, Boolean missingBugFlag);

  void replaceAssignee(Long id, TaskAssigneeReplaceDto dto);

  void replaceConfirmor(Long id, TaskConfirmorReplaceDto dto);

  void replaceTag(Long id, TaskTagReplaceDto dto);

  void replaceDeadline(Long id, LocalDateTime deadlineDate);

  void replacePriority(Long id, Priority priority);

  void replaceSoftwareVersion(Long id, SoftwareVersionRefReplaceDto version);

  void replaceEvalWorkload(Long id, TaskWorkloadReplaceDto dto);

  void replaceActualWorkload(Long id, TaskWorkloadReplaceDto dto);

  void replaceDescription(Long id, TaskDescriptionReplaceDto dto);

  void replaceAttachment(Long id, TaskAttachmentReplaceDto dto);

  void start(Long id);

  void cancel(Long id);

  void processed(Long id);

  void confirm(Long id, Result result, BigDecimal evalWorkload, BigDecimal actualWorkload);

  void complete(Long id, BigDecimal evalWorkload, BigDecimal actualWorkload);

  void subtaskSet(Long id, HashSet<Long> subTaskIds);

  void subtaskCancel(Long id, HashSet<Long> subTaskIds);

  List<TaskInfoVo> notAssociatedSubtask(Long id, Long moduleId);

  void taskAssocAdd(Long id, HashSet<Long> assocTaskIds);

  void taskAssocCancel(Long id, HashSet<Long> assocTaskIds);

  List<TaskInfoVo> notAssociatedTask(Long id, Long moduleId, TaskType taskType);

  void caseAssocAdd(Long id, HashSet<Long> assocCaseIds);

  void caseAssocCancel(Long id, HashSet<Long> assocCaseIds);

  List<FuncCaseListVo> notAssociatedCase(Long id, Long moduleId);

  void restart(Long id);

  void reopen(Long id);

  List<IdKey<Long, Object>> imports(TaskImportDto dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  TaskDetailVo detail(Long id);

  PageResult<TaskListVo> list(TaskFindDto dto);

  PageResult<TaskListVo> search(boolean exportFlag, TaskSearchDto dto);

  ResponseEntity<Resource> export(TaskSearchDto dto, HttpServletResponse response);

}
