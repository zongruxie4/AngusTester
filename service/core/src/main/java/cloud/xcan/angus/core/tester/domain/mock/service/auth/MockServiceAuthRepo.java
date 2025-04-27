package cloud.xcan.angus.core.tester.domain.mock.service.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
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
