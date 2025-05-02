package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskSprintQuery {

  TaskSprint detail(Long id);

  Page<TaskSprint> find(GenericSpecification<TaskSprint> spec, Pageable pageable);

  TaskSprint findLeastByProjectId(Long projectId);

  TaskSprint checkAndFind(Long id);

  List<TaskSprint> checkAndFind(Collection<Long> ids);

  boolean isAuthCtrl(Long sprintId);

  void checkNameExists(Long projectId, String name);

  void checkSprintDateRange(LocalDateTime startDate, LocalDateTime deadlineDate);

  void checkHasStarted(TaskSprint sprintDb);

  void checkSprintTasksCompleted(Long id);

  int checkQuota();

  void setTaskNum(List<TaskSprint> sprints, Set<Long> sprintIds);

  void setProgress(List<TaskSprint> sprints, Set<Long> sprintIds);

  void setMembers(List<TaskSprint> sprints, Set<Long> sprintIds);

  void setSafeCloneName(TaskSprint sprint);

  boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria);

  List<TaskSprint> getSprintCreatedSummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId);

}
