package cloud.xcan.sdf.core.angustester.domain.task.sprint;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
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

  @Query(value = "SELECT DISTINCT id FROM task_sprint WHERE id IN (?1) AND auth_flag = ?2 ", nativeQuery = true)
  Collection<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, Boolean authFlag);

  List<TaskSprint> findByProjectId(Long projectId);

  boolean existsByProjectIdAndName(Long projectId, String name);

  long countByProjectIdAndName(Long projectId, String name);

  int countByProjectId(Long projectId);

  @Modifying
  @Query("UPDATE TaskSprint a SET a.deletedFlag = 0, a.deletedBy = null, a.deletedDate = null WHERE a.id in ?1")
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query("UPDATE TaskSprint s SET s.authFlag=?2 WHERE s.id=?1")
  void updateAuthFlagById(Long id, Boolean enabledFlag);

  @Modifying
  @Query(value = "UPDATE task_sprint s SET s.project_id =?2 WHERE s.id = ?1", nativeQuery = true)
  void updateProjectById(Long id, Long projectId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM task_sprint WHERE id IN ?1", nativeQuery = true)
  void deleteAllByIdIn(Collection<Long> ids);

}
