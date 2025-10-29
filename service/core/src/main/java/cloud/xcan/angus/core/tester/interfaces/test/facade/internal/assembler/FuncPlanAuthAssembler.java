package cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthDeptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthGroupDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthUserDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthVo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class FuncPlanAuthAssembler {

  public static FuncPlanAuth addDtoToDomain(Long planId, FuncPlanAuthAddDto dto) {
    Set<FuncPlanPermission> permissions = new HashSet<>();
    if (isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new FuncPlanAuth().setPlanId(planId)
        .setCreator(false)
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
    authVo.setCreator(dirAuth.getCreator());
    authVo.setPlanId(dirAuth.getPlanId());
    return authVo;
  }

  public static FuncPlanAuthCurrentVo toAuthCurrentVo(FuncPlanAuthCurrent authCurrent) {
    return new FuncPlanAuthCurrentVo()
        .setFuncPlanAuth(authCurrent.isFuncPlanAuth())
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




