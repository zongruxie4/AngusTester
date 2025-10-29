package cloud.xcan.angus.core.tester.domain.test.baseline;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineRepo extends BaseRepository<FuncBaseline, Long> {

  @Modifying
  @Query(value = "DELETE FROM func_baseline WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
