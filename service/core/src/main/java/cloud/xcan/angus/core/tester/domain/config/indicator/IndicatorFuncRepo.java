package cloud.xcan.angus.core.tester.domain.config.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorFuncRepo extends BaseRepository<IndicatorFunc, Long> {

  List<IndicatorFunc> findByTargetIdAndTargetTypeIn(Long targetId,
      Collection<CombinedTargetType> targetTypes);

  List<IndicatorFunc> findByTargetIdInAndTargetType(Collection<Long> targetIds,
      CombinedTargetType combinedTargetType);

  IndicatorFunc findByTargetIdAndTargetType(Long targetId, CombinedTargetType targetType);

  @Modifying
  @Query(value = "DELETE FROM indicator_func WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  void deleteAllByTargetIdIn(Collection<Long> targetIds, String targetType);


}
