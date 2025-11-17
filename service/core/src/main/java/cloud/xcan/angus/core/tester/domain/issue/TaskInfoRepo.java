package cloud.xcan.angus.core.tester.domain.issue;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import cloud.xcan.angus.core.tester.domain.issue.count.SprintTaskNum;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskInfoRepo extends BaseRepository<TaskInfo, Long>,
    NameJoinRepository<TaskInfo, Long> {

  @Query(value = "SELECT * FROM task WHERE id = ?1", nativeQuery = true)
  Optional<TaskInfo> find0ById(Long id);

  @Query(value = "SELECT DISTINCT sprint_id FROM task WHERE id IN ?1", nativeQuery = true)
  List<Long> findAll0SprintIdsByIdIn(Collection<Long> ids);

  @Query(value = "SELECT DISTINCT id FROM task WHERE project_id IN ?1", nativeQuery = true)
  List<Long> findAll0IdByProjectIdIn(Collection<Long> projectIds);

  @Query(value = "SELECT COUNT(id) FROM task WHERE project_id = ?1", nativeQuery = true)
  Long countAll0ByProjectId(Long projectId);

  @Query(value = "SELECT COUNT(id) FROM task WHERE project_id = ?1 AND name = ?2", nativeQuery = true)
  int countByProjectIdAndName(Long projectId, String name);

  @Query(value = "SELECT COUNT(id) FROM task WHERE sprint_id = ?1 AND name = ?2", nativeQuery = true)
  int countBySprintIdAndName(Long sprintId, String name);

  @Query(value = "SELECT COUNT(id) FROM task WHERE sprint_id = ?1 AND name = ?2 AND id <> ?3", nativeQuery = true)
  int countBySprintIdAndNameAndIdNot(Long sprintId, String name, Long id);

  @Query(value = "SELECT COUNT(id) FROM task WHERE project_id = ?1 AND name = ?2 AND id <> ?3", nativeQuery = true)
  int countByProjectIdAndNameAndIdNot(Long projectId, String name, Long id);

  @Query(value = "SELECT count(ud.id) as taskNum, ud.sprintId as sprintId FROM Task ud WHERE ud.sprintId IN (?1) GROUP BY ud.sprintId")
  List<SprintTaskNum> findSprintTaskNumsGroupBySprintId(Collection<Long> sprintIds);

  @Query(value = "SELECT count(ud.id) as taskNum, ud.sprintId as sprintId FROM Task ud WHERE ud.sprintId IN (?1) AND ud.status <> 'CANCELED' GROUP BY ud.sprintId")
  List<SprintTaskNum> findValidSprintTaskNumsGroupBySprintId(Collection<Long> sprintIds);

  @Query(value = "SELECT count(ud.id) as taskNum, ud.sprintId as sprintId FROM Task ud WHERE ud.sprintId IN (?1) AND ud.status = 'COMPLETED' GROUP BY ud.sprintId")
  List<SprintTaskNum> findSprintPassedTaskNumsGroupBySprintId(Collection<Long> sprintIds);

  @Query(value = "SELECT DISTINCT ud.createdBy FROM Task ud WHERE ud.sprintId = ?1")
  List<Long> findCreatorIdBySprintId(Long sprintId);

  @Query(value = "SELECT DISTINCT ud.assigneeId FROM Task ud WHERE ud.sprintId = ?1")
  List<Long> findAssigneeIdBySprintId(Long sprintId);

  @Query(value = "SELECT DISTINCT ud.confirmerId FROM Task ud WHERE ud.sprintId = ?1")
  List<Long> findConfirmerIdBySprintId(Long sprintId);

  @Query(value = "SELECT DISTINCT ud.assigneeId FROM Task ud WHERE ud.id IN ?1")
  List<Long> findAssigneeIdByIdIn(Collection<Long> ids);

  @Query(value = "SELECT DISTINCT ud.confirmerId FROM Task ud WHERE ud.id IN ?1")
  List<Long> findConfirmerIdByByIdIn(Collection<Long> ids);

  @Query(value = "SELECT ud.id FROM Task ud WHERE ud.parentTaskId = ?1")
  Set<Long> findSubTaskIdsById(Long taskId);

  List<TaskInfo> findByProjectIdAndSoftwareVersion(Long projectId, String version);

  List<TaskInfo> findByProjectIdAndSoftwareVersionIn(Long projectId, Collection<String> versions);

  List<TaskInfo> findAllBySprintId(Long sprintId);

  List<TaskInfo> findByProjectIdAndParentTaskIdIn(Long projectId, Collection<Long> taskIds);

  List<TaskInfo> findByParentTaskId(Long taskId);

  @Query(value = "SELECT * FROM task WHERE sprint_id = ?1 AND assignee_id = ?2 AND deleted = 0 AND sprint_deleted =0", nativeQuery = true)
  List<TaskInfo> findAllBySprintIdAndAssigneeId(Long sprintId, Long userId);

  @Query(value = "SELECT * FROM task WHERE project_id = ?1 AND assignee_id = ?2 AND deleted = 0 AND sprint_deleted =0", nativeQuery = true)
  List<TaskInfo> findAllByProjectIdAndAssigneeId(Long projectId, Long userId);

  List<TaskInfo> findByProjectIdAndNameIn(Long projectId, Collection<String> names);

  List<TaskInfo> findBySprintIdAndNameIn(Long sprintId, Collection<String> names);

  @Query(value = "SELECT * FROM task WHERE project_id = ?1 AND deleted = 0 AND sprint_deleted = 0 ORDER BY created_date ASC LIMIT 1", nativeQuery = true)
  TaskInfo findEarliestByProjectId(Long projectId);

  @Query(value = "SELECT * FROM task WHERE sprint_id = ?1 AND deleted = 0 AND sprint_deleted = 0 ORDER BY created_date ASC LIMIT 1", nativeQuery = true)
  TaskInfo findEarliestBySprintId(Long sprintId);

  @Query(value = "SELECT * FROM task WHERE sprint_id = ?1 AND deleted = 0 AND sprint_deleted = 0 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  TaskInfo findLeastBySprintId(Long sprintId);

  @Query(value = "SELECT * FROM task WHERE project_id = ?1 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  TaskInfo findLeastByProjectId(Long projectId);

  @Query(value = "SELECT * FROM task WHERE deadline_date < ?1 AND deadline_date >= ?2 AND status <> 'COMPLETED' AND status <> 'CANCELED' AND assignee_id <> null AND deleted = 0 AND sprint_deleted = 0 ORDER BY created_date ASC LIMIT ?3", nativeQuery = true)
  List<TaskInfo> findWillOverdue(LocalDateTime now, LocalDateTime deadline, Long count);

  @Query(value =
      "SELECT * FROM task WHERE overdue = 0 AND ((deadline_date < now() AND STATUS <> 'COMPLETED' AND STATUS <> 'CANCELED') "
          + "OR (completed_date > deadline_date AND STATUS = 'COMPLETED' )) LIMIT ?1", nativeQuery = true)
  List<TaskInfo> findIdByOverdue(Long count);

  @Transactional
  @Modifying
  @Query(value = "UPDATE task SET overdue=1 WHERE id IN ?1", nativeQuery = true)
  void updateOverdue(Collection<Long> ids);

}
