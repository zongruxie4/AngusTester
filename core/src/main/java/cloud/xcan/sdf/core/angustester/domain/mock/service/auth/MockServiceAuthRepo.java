package cloud.xcan.sdf.core.angustester.domain.mock.service.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockServiceAuthRepo extends BaseRepository<MockServiceAuth, Long> {

  List<MockServiceAuth> findAllByMockServiceIdAndAuthObjectIdIn(Long serviceId,
      Collection<Long> authObjectIds);

  List<MockServiceAuth> findAllByMockServiceIdInAndAuthObjectIdIn(Collection<Long> serviceIds,
      Collection<Long> authObjectIds);

  Long countByMockServiceIdAndAuthObjectIdAndAuthObjectType(Long serviceId,
      Long authObjectId, AuthObjectType authObjectType);

  List<MockServiceAuth> findAllByAuthObjectIdIn(Collection<Long> authObjectIds);

}
