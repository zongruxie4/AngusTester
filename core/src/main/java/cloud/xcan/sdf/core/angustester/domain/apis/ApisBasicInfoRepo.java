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
public interface ApisBasicInfoRepo extends BaseRepository<ApisBasicInfo, Long> {

  @Override
  Optional<ApisBasicInfo> findById(Long id);

  List<ApisBasicInfo> findAllByIdIn(Collection<Long> ids);

  List<ApisBasicInfo> findAllByServiceId(Long serviceId);

  List<ApisBasicInfo> findAllByServiceIdIn(Collection<Long> serviceIds);

  List<ApisBasicInfo> findAllByIdInAndDeletedFlag(Collection<Long> ids, Boolean deleteFlag);

  List<ApisBasicInfo> findByServiceIdOrderByIdDesc(Long serviceId);

}
