package cloud.xcan.sdf.core.angustester.domain.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorStabilityRepo extends BaseRepository<IndicatorStability, Long> {

  List<IndicatorStability> findByTargetIdAndTargetTypeIn(Long targetId,
      List<CombinedTargetType> targetTypes);

  List<IndicatorStability> findByTargetIdInAndTargetType(Collection<Long> targetIds,
      CombinedTargetType targetType);

  IndicatorStability findByTargetIdAndTargetType(Long targetId, CombinedTargetType targetType);

  @Modifying
  @Query(value = "DELETE FROM indicator_stability WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  void deleteAllByTargetIdIn(Collection<Long> targetIds, String targetType);
}
