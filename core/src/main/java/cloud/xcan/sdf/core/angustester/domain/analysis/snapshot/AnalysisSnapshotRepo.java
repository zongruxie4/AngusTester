package cloud.xcan.sdf.core.angustester.domain.analysis.snapshot;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface AnalysisSnapshotRepo extends BaseRepository<AnalysisSnapshot, Long> {

  Optional<AnalysisSnapshot> findByAnalysisId(Long analysisId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM analysis_snapshot WHERE analysis_id IN ?1", nativeQuery = true)
  void deleteByAnalysisIdIn(Collection<Long> ids);

}
