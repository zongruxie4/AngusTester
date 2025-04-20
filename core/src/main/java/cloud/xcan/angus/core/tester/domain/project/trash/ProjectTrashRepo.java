package cloud.xcan.angus.core.tester.domain.project.trash;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectTrashRepo extends BaseRepository<ProjectTrash, Long> {

  List<ProjectTrash> findByDeletedBy(Long userId);

  Long countByCreatedBy(Long userId);

  @Modifying
  @Query(value = "DELETE FROM project_trash WHERE target_id in ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);

}
