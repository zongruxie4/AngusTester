package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface FuncPlanAuthCmd {

  IdKey<Long, Object> add(FuncPlanAuth auth);

  void replace(FuncPlanAuth auth);

  void enabled(Long planId, Boolean enabledFlag);

  void delete(Long id);

  void addCreatorAuth(Long id, Set<Long> authCreatorIds);

  void addOwnerAndTesterAuth(Long planId, Long ownerId, Set<Long> testerIds);

  void deleteAuthByPlanId(Long planId,  Collection<Long> testerIds);
}
