package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApisAuthCmd {

  IdKey<Long, Object> add(ApisAuth apisAuth);

  void replace(ApisAuth apisAuth);

  void delete(Long id);

  void enabled(Long projectId, Boolean enabled);

  void addCreatorAuth(Set<Long> apisIds, Set<Long> creatorIds);

  void addCreatorAuth(Map<Long, Set<Long>> apisIdAndCreatorIds);

  void moveCreatorAuth(Services targetProjectDb, List<Long> apiIds, List<Apis> apis);

}




