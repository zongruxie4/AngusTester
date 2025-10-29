package cloud.xcan.angus.core.tester.domain.issue.sprint;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskSprintRepo extends BaseRepository<TaskSprint, Long>,
    NameJoinRepository<TaskSprint, Long> {

  @Query(value = "SELECT * FROM task_sprint WHERE id = ?1", nativeQuery = true)
  Optional<TaskSprint> find0ById(Long id);

  @Query(value = "SELECT id FROM task_sprint WHERE sprint_id in ?1", nativeQuery = true)
  List<Long> findAll0IdBySprintIdIn(List<Long> sprintIds);

  @Query(value = "SELECT DISTINCT project_id FROM task_sprint WHERE id IN ?1", nativeQuery = true)
  List<Long> findAll0ProjectIdsByIdIn(Collection<Long> ids);

  @Query(value = "SELECT * FROM task_sprint WHERE id in (?1)", nativeQuery = true)
  List<TaskSprint> findAll0ByIdIn(List<Long> ids);

  @Query(value = "SELECT DISTINCT id FROM task_sprint WHERE id IN (?1) AND auth = ?2 ", nativeQuery = true)
  Collection<Long> findIds0ByIdInAndAuth(Collection<Long> ids, Boolean auth);

  List<TaskSprint> findByProjectId(Long projectId);

  @Query(value = "SELECT * FROM task_sprint WHERE project_id = ?1 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  TaskSprint findLeastByProjectId(Long projectId);

  boolean existsByProjectIdAndName(Long projectId, String name);

  long countByProjectIdAndName(Long projectId, String name);

  int countByProjectId(Long projectId);

  @Modifying
  @Query("UPDATE TaskSprint a SET a.deleted = false, a.deletedBy = null, a.deletedDate = null WHERE a.id in ?1")
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query("UPDATE TaskSprint s SET s.auth=?2 WHERE s.id=?1")
  void updateAuthById(Long id, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE task_sprint s SET s.project_id =?2 WHERE s.id = ?1", nativeQuery = true)
  void updateProjectById(Long id, Long projectId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM task_sprint WHERE id IN ?1", nativeQuery = true)
  void deleteAllByIdIn(Collection<Long> ids);

}
