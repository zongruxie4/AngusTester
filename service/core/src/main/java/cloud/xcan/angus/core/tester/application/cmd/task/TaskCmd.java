package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface TaskCmd {

  IdKey<Long, Object> add(Task task);

  Object generate(@Nullable Long sprintId, TaskType taskType, Long targetId, List<Task> tasks,
      boolean ignoreApisOrScenarioPermission);

  void update(Task toTask);

  void replace(Task task);

  void rename(Long taskId, String name);

  void move(List<Long> taskIds, Long targetSprintId);

  void replaceType(Long id, String type);

  void replaceBugLevel(Long id, String bugLevel);

  void replaceMissingBug(Long id, Boolean missingBug);

  void replaceAssignees(Long id, Long assigneeId);

  void replaceConfirmers(Long id, Long confirmerId);

  void replaceDeadline(Long taskId, LocalDateTime endDate);

  void replacePriority(Long taskId, Priority priority);

  void replaceSoftwareVersion(Long id, String version);

  void replaceEvalWorkload(Long taskId, BigDecimal evalWorkload);

  void replaceActualWorkload(Long taskId, BigDecimal actualWorkload);

  void replaceDescription(Long taskId, String description);

  void replaceAttachment(Long taskId, List<Attachment> attachments);

  void start(Long taskId);

  void cancel(Long taskId);

  void processed(Long taskId);

  void confirm(Long taskId, Result result, BigDecimal evalWorkload, BigDecimal actualWorkload);

  void restart(List<Long> taskIds);

  void reopen(List<Long> taskIds);

  void retest0ByTarget(Boolean restart, List<Task> taskDbs);

  void subtaskSet(Long id, HashSet<Long> subTaskIds);

  void subtaskCancel(Long id, HashSet<Long> subTaskIds);

  void taskAssocAdd(Long id, HashSet<Long> assocTaskIds);

  void taskAssocCancel(Long id, HashSet<Long> assocTaskIds);

  void caseAssocAdd(Long id, HashSet<Long> assocCaseIds);

  void caseAssocCancel(Long id, HashSet<Long> assocCaseIds);

  List<IdKey<Long, Object>> imports(Long projectId, @Nullable Long sprintId,
      StrategyWhenDuplicated strategyWhenDuplicated, MultipartFile file);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(List<Long> taskIds);

  void delete0ByTarget(List<Long> taskIds);

  void delete0(List<Long> taskIds);

}
