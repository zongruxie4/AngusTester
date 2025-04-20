package cloud.xcan.angus.core.tester.domain.data.dataset;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DatasetTargetRepo extends BaseRepository<DatasetTarget, Long> {

  long countByTargetIdAndTargetType(Long targetId, CombinedTargetType targetType);

  @Query(value = "SELECT DISTINCT dataset_id FROM data_dataset_target WHERE target_id = ?1 AND target_type = ?2", nativeQuery = true)
  List<Long> findByDatasetIdByTargetIdAndType(Long targetId, String targetType);

  @Query(value = "SELECT * FROM data_dataset_target WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  List<DatasetTarget> findByTargetIdInAndType(Collection<Long> targetIds, String targetType);

  List<DatasetTarget> findByDatasetId(Long datasetId);

  @Modifying
  @Query(value = "DELETE FROM data_dataset_target WHERE dataset_id IN ?1", nativeQuery = true)
  void deleteByDatasetIdIn(Collection<Long> datasetIds);

  @Modifying
  @Query(value = "DELETE FROM data_dataset_target WHERE target_id = ?1 AND target_type = ?2 AND dataset_id IN ?3", nativeQuery = true)
  void deleteByTargetIdAndTypeAndDatasetIdIn(Long targetId, String targetType,
      Collection<Long> datasetIds);

  @Modifying
  @Query(value = "DELETE FROM data_dataset_target WHERE target_id IN ?1 AND target_type = ?2", nativeQuery = true)
  void deleteByTarget(List<Long> targetIds, String targetType);

}
