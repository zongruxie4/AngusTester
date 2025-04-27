package cloud.xcan.angus.core.tester.domain.apis.design;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import jakarta.transaction.Transactional;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisDesignRepo extends BaseRepository<ApisDesign, Long> {

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM apis_design WHERE design_source = ?1 AND design_source_id IN ?2", nativeQuery = true)
  void deleteByDesignSourceIdIn(String source, Collection<Long> sourceIds);

}
