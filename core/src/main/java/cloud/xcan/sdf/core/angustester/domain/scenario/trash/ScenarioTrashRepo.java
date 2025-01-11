package cloud.xcan.sdf.core.angustester.domain.scenario.trash;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioTrashRepo extends BaseRepository<ScenarioTrash, Long> {

  List<ScenarioTrash> findByCreatedBy(Long createdBy);

  List<ScenarioTrash> findByProjectId(Long projectId);

  List<ScenarioTrash> findByProjectIdAndCreatedBy(Long projectId, Long createdBy);

  Long countByCreatedBy(Long userId);

  Long countByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM scenario_trash WHERE target_id in ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);

}
