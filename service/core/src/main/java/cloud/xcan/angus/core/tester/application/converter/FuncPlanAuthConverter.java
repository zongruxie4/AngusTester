package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class FuncPlanAuthConverter {

  @NotNull
  public static List<FuncPlanAuth> toFuncPlanAuths(Set<Long> creatorIds, Long planId
      , List<FuncPlanPermission> auths, boolean creator) {
    return creatorIds.stream()
        .map(creatorId -> new FuncPlanAuth()
            .setId(getBean(CachedUidGenerator.class).getUID())
            .setPlanId(planId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(auths)
            .setCreator(creator))
        .toList();
  }

}
