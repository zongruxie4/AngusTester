package cloud.xcan.sdf.core.angustester.domain.apis;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Make the query include logic delete data
 */
@NoRepositoryBean
public interface ApisBasicAndConfigInfoRepo extends BaseRepository<ApisBasicAndConfigInfo, Long> {

  Optional<ApisBasicAndConfigInfo> findAllInfoById(Long id);

  List<ApisBasicAndConfigInfo> findBasicByIdInAndDeletedFlag(Collection<Long> ids,
      Boolean deleteFlag);

  List<ApisBasicAndConfigInfo> findAllByServiceId(Long serviceId);

  List<ApisBasicAndConfigInfo> findAllByServiceIdIn(Collection<Long> serviceIds);

}
