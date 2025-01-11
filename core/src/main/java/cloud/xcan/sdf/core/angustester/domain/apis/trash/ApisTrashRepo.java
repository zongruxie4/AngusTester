package cloud.xcan.sdf.core.angustester.domain.apis.trash;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisTrashRepo extends BaseRepository<ApisTrash, Long> {

  List<ApisTrash> findByCreatedBy(Long userId);

  List<ApisTrash> findByProjectId(Long projectId);

  List<ApisTrash> findByProjectIdAndCreatedBy(Long projectId, Long userId);

  Long countByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM apis_trash WHERE target_id in ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);

}
