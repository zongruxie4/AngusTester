package cloud.xcan.sdf.core.angustester.domain.version;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
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
  @Query(value = "DELETE FROM task_release_version WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
