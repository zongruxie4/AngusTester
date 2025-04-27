package cloud.xcan.angus.core.tester.application.cmd.services;

import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface ServicesAuthCmd {

  IdKey<Long, Object> add(ServicesAuth projectAuth);

  void replace(ServicesAuth projectAuth);

  void delete(Long id);

  void enabled(Long projectId, Boolean enabled);

  void apisEnabled(Long projectId, Boolean enabled);

  void addCreatorAuth(Long projectId, Set<Long> creatorIds);

  void moveCreatorAuth(Long projectId, Long creatorId);

  void deleteAllByProject(Collection<Long> projectIds);

}




