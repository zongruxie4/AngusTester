package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuth;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApisAuthCmd {

  IdKey<Long, Object> add(ApisAuth apisAuth);

  void replace(ApisAuth apisAuth);

  void delete(Long id);

  void enabled(Long projectId, Boolean enabledFlag);

  void addCreatorAuth(Set<Long> apisIds, Set<Long> creatorIds);

  void addCreatorAuth(Map<Long, Set<Long>> apisIdAndCreatorIds);

  void moveCreatorAuth(Services targetProjectDb, List<Long> apiIds, List<Apis> apis);

}




