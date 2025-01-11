package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class FuncPlanAuthAssembler {

  public static FuncPlanAuth addDtoToDomain(Long planId, FuncPlanAuthAddDto dto) {
    Set<FuncPlanPermission> permissions = new HashSet<>();
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new FuncPlanAuth().setPlanId(planId)
        .setCreatorFlag(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static FuncPlanAuth replaceDtoToDomain(Long planId, FuncPlanAuthReplaceDto dto) {
    return new FuncPlanAuth().setId(planId).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static FuncPlanAuthVo toDetailVo(FuncPlanAuth dirAuth) {
    FuncPlanAuthVo authVo;
    switch (dirAuth.getAuthObjectType()) {
      case USER:
        authVo = new FuncPlanAuthUserDetailVo();
        break;
      case GROUP:
        authVo = new FuncPlanAuthGroupDetailVo();
        break;
      case DEPT:
        authVo = new FuncPlanAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(dirAuth.getAuthObjectType().getMessage());
    }
    authVo.setId(dirAuth.getId());
    authVo.setPermissions(dirAuth.getAuths());
    authVo.setAuthObjectType(dirAuth.getAuthObjectType());
    authVo.setAuthObjectId(dirAuth.getAuthObjectId());
    authVo.setCreatorFlag(dirAuth.getCreatorFlag());
    authVo.setPlanId(dirAuth.getPlanId());
    return authVo;
  }

  public static FuncPlanAuthCurrentVo toAuthCurrentVo(FuncPlanAuthCurrent authCurrent) {
    return new FuncPlanAuthCurrentVo()
        .setFuncPlanAuthFlag(authCurrent.isFuncPlanAuthFlag())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<FuncPlanAuth> getSpecification(FuncPlanAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "planId")
        .orderByFields("id", "planId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}




