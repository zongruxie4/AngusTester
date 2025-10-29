package cloud.xcan.angus.core.tester.domain.test.trash;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncTrashRepo extends BaseRepository<FuncTrash, Long> {

  List<FuncTrash> findByCreatedBy(Long userId);

  List<FuncTrash> findByProjectId(Long projectId);

  List<FuncTrash> findByProjectIdAndCreatedBy(Long projectId, Long userId);

  Long countByCreatedBy(Long userId);

  Long countByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM func_trash WHERE target_id in ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);


}
