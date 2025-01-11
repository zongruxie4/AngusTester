package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface AnalysisRepo extends BaseRepository<Analysis, Long>,
    NameJoinRepository<Analysis, Long>{

  long countByProjectIdAndName(Long projectId, String name);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM analysis WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
