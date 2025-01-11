package cloud.xcan.sdf.core.angustester.domain.apis.share;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ApisShareRepo extends BaseRepository<ApisShare, Long> {

  @Modifying
  @Query(value = "UPDATE apis_share SET view_num = view_num + 1  WHERE id = ?1", nativeQuery = true)
  void incrViewById(Long id);

  @Modifying
  @Query(value = "DELETE FROM apis_share WHERE id in ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
