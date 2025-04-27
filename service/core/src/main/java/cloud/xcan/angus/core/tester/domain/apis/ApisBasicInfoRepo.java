package cloud.xcan.angus.core.tester.domain.apis;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
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

  List<ApisBasicInfo> findAllByIdInAndDeleted(Collection<Long> ids, Boolean delete);

  List<ApisBasicInfo> findByServiceIdOrderByIdDesc(Long serviceId);

}
