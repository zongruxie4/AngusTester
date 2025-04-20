package cloud.xcan.angus.core.tester.domain.data.variables;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface VariableRepo extends BaseRepository<Variable, Long> {


  @Query(value = "SELECT id FROM data_variable WHERE project_id = ?1", nativeQuery = true)
  List<Long> findAllIdsByProjectId(Long projectId);

  @Query(value = "SELECT DISTINCT project_id FROM data_variable WHERE id IN ?1", nativeQuery = true)
  List<Long> findProjectIdByIdIn(Collection<Long> ids);

  List<Variable> findByProjectId(Long projectId);

  List<Variable> findByProjectIdAndIdIn(Long projectId, Collection<Long> ids);

  List<Variable> findByProjectIdAndNameIn(Long projectId, Collection<String> names);

  @Query(value = "SELECT name FROM data_variable WHERE project_id = ?1 AND name IN ?2", nativeQuery = true)
  Set<String> findNamesByProjectIdAndNameIn(Long projectId, Collection<String> names);

  @Query(value = "SELECT * FROM data_variable WHERE id IN ?1 AND extracted = ?2", nativeQuery = true)
  List<Variable> findByIdAndExtracted(Set<Long> ids, boolean extracted);

  boolean existsByProjectIdAndName(Long projectId, String name);

  boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long id);

  boolean existsByName(String name);

  long countByTenantId(Long tenantId);

  @Modifying
  @Query(value = "DELETE FROM data_variable WHERE id in ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM data_variable WHERE project_id = ?1 AND name IN ?2", nativeQuery = true)
  void deleteByProjectIdAndNameIn(Long projectId, Collection<String> existedNames);

}
