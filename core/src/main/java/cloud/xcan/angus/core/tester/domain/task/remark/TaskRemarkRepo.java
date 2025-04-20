package cloud.xcan.angus.core.tester.domain.task.remark;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskRemarkRepo extends BaseRepository<TaskRemark, Long> {

  @Query(value = "SELECT task_id FROM task_remark WHERE id = ?1 limit 1", nativeQuery = true)
  Long findTaskIdById(Long id);

  List<TaskRemark> findByTaskId(Long taskId);

  int countAllByTaskId(Long taskId);

  @Modifying
  @Query(value = "DELETE FROM task_remark WHERE task_id IN ?1", nativeQuery = true)
  void deleteByTaskIdIn(Collection<Long> taskIds);

}
