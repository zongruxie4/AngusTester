package cloud.xcan.sdf.core.angustester.domain.data.variables;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface VariableTargetRepo extends BaseRepository<VariableTarget, Long> {

  long countByTargetIdAndTargetType(Long targetId, CombinedTargetType targetType);

  @Query(value = "SELECT DISTINCT variable_id FROM data_variable_target WHERE target_id = ?1 AND target_type = ?2", nativeQuery = true)
  List<Long> findByVariableIdByTargetIdAndType(Long targetId, String targetType);

  @Query(value = "SELECT variable_id FROM data_variable_target WHERE target_id IN ?1 ", nativeQuery = true)
  Set<Long> findByVariableIdByTargetId(Collection<Long> targetIds);

  @Query(value = "SELECT * FROM data_variable_target WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  List<VariableTarget> findByTargetIdInAndType(Collection<Long> targetIds, String targetType);

  List<VariableTarget> findByVariableId(Long variableId);

  @Modifying
  @Query(value = "DELETE FROM data_variable_target WHERE variable_id IN ?1", nativeQuery = true)
  void deleteByVariableIdIn(Collection<Long> variableIds);

  @Modifying
  @Query(value = "DELETE FROM data_variable_target WHERE target_id = ?1 AND target_type = ?2 AND variable_id IN ?3", nativeQuery = true)
  void deleteByTargetIdAndTypeAndVariableIdIn(Long targetId, String targetType,
      Collection<Long> variableIds);

  @Modifying
  @Query(value = "DELETE FROM data_variable_target WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  void deleteByTarget(List<Long> targetIds, String targetType);

}
