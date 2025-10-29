package cloud.xcan.angus.core.tester.domain.issue.trash;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskTrashRepo extends BaseRepository<TaskTrash, Long> {

  List<TaskTrash> findByCreatedBy(Long userId);

  List<TaskTrash> findByProjectId(Long projectId);

  List<TaskTrash> findByProjectIdAndCreatedBy(Long projectId, Long userId);

  Long countByCreatedBy(Long userId);

  Long countByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM task_trash WHERE target_id in ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);

}
