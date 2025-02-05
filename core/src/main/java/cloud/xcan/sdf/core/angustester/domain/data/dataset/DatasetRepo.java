package cloud.xcan.sdf.core.angustester.domain.data.dataset;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasetRepo extends BaseRepository<Dataset, Long> {

  @Query(value = "SELECT id FROM data_dataset WHERE project_id = ?1", nativeQuery = true)
  List<Long> findAllIdsByProjectId(Long projectId);

  @Query(value = "SELECT DISTINCT project_id FROM data_dataset WHERE id IN ?1", nativeQuery = true)
  List<Long> findProjectIdByIdIn(Collection<Long> ids);

  @Query(value = "SELECT name FROM data_dataset WHERE project_id = ?1 AND name IN ?2", nativeQuery = true)
  Set<String> findNamesByProjectIdAndNameIn(Long projectId, Collection<String> names);

  List<Dataset> findByProjectId(Long projectId);

  List<Dataset> findByProjectIdAndIdIn(Long projectId, Collection<Long> ids);

  List<Dataset> findByProjectIdAndNameIn(Long projectId, Collection<String> names);

  boolean existsByProjectIdAndName(Long projectId, String name);

  boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long id);

  boolean existsByName(String name);

  long countByTenantId(Long tenantId);

  @Modifying
  @Query(value = "DELETE FROM data_dataset WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM data_dataset WHERE project_id = ?1 AND name IN ?2", nativeQuery = true)
  void deleteByProjectIdAndNameIn(Long projectId, Collection<String> existedNames);

}
