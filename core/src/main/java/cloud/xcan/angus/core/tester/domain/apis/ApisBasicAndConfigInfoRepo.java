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
public interface ApisBasicAndConfigInfoRepo extends BaseRepository<ApisBasicAndConfigInfo, Long> {

  Optional<ApisBasicAndConfigInfo> findAllInfoById(Long id);

  List<ApisBasicAndConfigInfo> findBasicByIdInAndDeleted(Collection<Long> ids,
      Boolean delete);

  List<ApisBasicAndConfigInfo> findAllByServiceId(Long serviceId);

  List<ApisBasicAndConfigInfo> findAllByServiceIdIn(Collection<Long> serviceIds);

}
