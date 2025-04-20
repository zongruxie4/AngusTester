package cloud.xcan.angus.core.tester.domain.task.sprint.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface TaskSprintAuthRepo extends BaseRepository<TaskSprintAuth, Long> {

  List<TaskSprintAuth> findAllBySprintIdAndAuthObjectIdIn(Long sprintId, List<Long> orgIds);

  List<TaskSprintAuth> findAllByAuthObjectIdIn(Collection<Long> orgIds);

  List<TaskSprintAuth> findAllBySprintIdInAndAuthObjectIdIn(Collection<Long> sprintIds,
      Collection<Long> orgIds);

  long countBySprintIdAndAuthObjectIdAndAuthObjectType(Long sprintId, Long authObjectId,
      AuthObjectType authObjectType);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM task_sprint_auth WHERE sprint_id in ?1", nativeQuery = true)
  void deleteBySprintIdIn(Collection<Long> sprintIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM task_sprint_auth WHERE sprint_id = ?1 AND auth_object_id IN ?2 AND creator = ?3", nativeQuery = true)
  void deleteBySprintIdAndAuthObjectIdInAndCreator(Long sprintId, Set<Long> creatorIds,
      boolean creator);

}
