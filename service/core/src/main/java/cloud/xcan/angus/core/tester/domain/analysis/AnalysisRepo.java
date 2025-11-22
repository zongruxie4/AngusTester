package cloud.xcan.angus.core.tester.domain.analysis;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface AnalysisRepo extends BaseRepository<Analysis, Long>,
    NameJoinRepository<Analysis, Long> {

  long countByProjectIdAndNameAndResource(Long projectId, String name, AnalysisResource resource);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM analysis WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
