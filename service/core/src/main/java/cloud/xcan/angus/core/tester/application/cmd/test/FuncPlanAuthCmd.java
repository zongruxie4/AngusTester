package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface FuncPlanAuthCmd {

  IdKey<Long, Object> add(FuncPlanAuth auth);

  void replace(FuncPlanAuth auth);

  void enabled(Long planId, Boolean enabled);

  void delete(Long id);

  void addCreatorAuth(Long id, Set<Long> authCreatorIds);

  void addOwnerAndTesterAuth(Long planId, Long ownerId, Set<Long> testerIds);

  void deleteAuthByPlanId(Long planId, Collection<Long> testerIds);
}
