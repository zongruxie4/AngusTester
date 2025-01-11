package cloud.xcan.sdf.core.angustester.domain.task.follow;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskFollowRepo extends BaseRepository<TaskFollow, Long> {

  List<TaskFollow> findByTaskIdInAndCreatedBy(Set<Long> taskIds, Long userId);

  Long countByTaskIdAndCreatedBy(Long taskId, Long userId);

  @Query(value =
      "select sf.id id, s.id taskId, s.name taskName, s.project_id projectId, s.sprint_id sprintId, s.task_type taskType, s.code taskCode "
          + "FROM task_follow sf, task s WHERE s.id = sf.task_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.sprint_deleted_flag = 0 AND s.deleted_flag = 0",
      countQuery =
          "select count(sf.id) FROM task_follow sf, task s WHERE s.id = sf.task_id "
              + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.sprint_deleted_flag = 0 AND s.deleted_flag = 0",
      nativeQuery = true)
  Page<TaskFollowP> search(Long projectId, Long userId, Pageable pageable);

  /**
   * Note: Overwritten in implementation.
   */
  Page<TaskFollowP> searchByMatch(Long projectId, Long userId, String taskName, Pageable pageable);

  @Query(value = "SELECT count(sf.id) FROM task_follow sf, task s WHERE s.id = sf.task_id "
      + "AND sf.created_by = ?1 AND s.sprint_deleted_flag = 0 AND s.deleted_flag = 0", nativeQuery = true)
  Long countByCreatedBy(Long userId);

  @Query(value = "SELECT count(sf.id) FROM task_follow sf, task s WHERE s.id = sf.task_id "
      + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND s.sprint_deleted_flag = 0 AND s.deleted_flag = 0", nativeQuery = true)
  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Query(value = "SELECT created_by FROM task_follow WHERE task_id = ?1", nativeQuery = true)
  List<Long> findUserIdsByTaskId(Long id);

  @Modifying
  @Query(value = "DELETE FROM task_follow WHERE task_id = ?1 AND created_by = ?2", nativeQuery = true)
  int deleteByTaskIdAndCreatedBy(Long taskId, Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM task_follow WHERE created_by = ?1", nativeQuery = true)
  int deleteByCreatedBy(Long userId);

  @Modifying
  @Query(value = "DELETE FROM task_follow WHERE project_id = ?1 AND created_by = ?2", nativeQuery = true)
  void deleteByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM task_follow WHERE task_id in ?1", nativeQuery = true)
  void deleteByTaskIdIn(List<Long> taskIds);

}
