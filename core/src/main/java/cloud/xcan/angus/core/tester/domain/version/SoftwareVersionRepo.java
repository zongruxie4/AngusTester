package cloud.xcan.angus.core.tester.domain.version;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface SoftwareVersionRepo extends BaseRepository<SoftwareVersion, Long>,
    NameJoinRepository<SoftwareVersion, Long> {

  long countByProjectIdAndName(Long projectId, String name);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM software_version WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
