package cloud.xcan.angus.core.tester.application.cmd.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Set;

public interface MockServiceAuthCmd {

  IdKey<Long, Object> add(MockServiceAuth serviceAuth);

  void replace(MockServiceAuth serviceAuth);

  void delete(Long id);

  void enabled(Long serviceId, boolean enabled);

  void addCreatorAuth(Set<Long> serviceIds);

}
