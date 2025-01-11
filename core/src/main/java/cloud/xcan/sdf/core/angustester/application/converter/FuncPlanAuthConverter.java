package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class FuncPlanAuthConverter {

  @NotNull
  public static List<FuncPlanAuth> toFuncPlanAuths(Set<Long> creatorIds, Long planId
      , List<FuncPlanPermission> auths, boolean creatorFlag) {
    return creatorIds.stream()
        .map(creatorId -> new FuncPlanAuth()
            .setId(getBean(CachedUidGenerator.class).getUID())
            .setPlanId(planId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(auths)
            .setCreatorFlag(creatorFlag))
        .collect(Collectors.toList());
  }

}
