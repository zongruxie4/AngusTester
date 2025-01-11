package cloud.xcan.sdf.core.angustester.application.cmd.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Set;

public interface MockServiceAuthCmd {

  IdKey<Long, Object> add(MockServiceAuth serviceAuth);

  void replace(MockServiceAuth serviceAuth);

  void delete(Long id);

  void enabled(Long serviceId, boolean enabledFlag);

  void addCreatorAuth(Set<Long> serviceIds);

}
