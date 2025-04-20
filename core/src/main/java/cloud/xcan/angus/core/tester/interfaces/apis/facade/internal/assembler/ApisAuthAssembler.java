package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthCurrent;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthDeptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthGroupDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthUserDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class ApisAuthAssembler {

  public static ApisAuth addDtoToDomain(Long apiId, ApisAuthAddDto dto) {
    Set<ApiPermission> permissions = new HashSet<>();
    permissions.add(ApiPermission.VIEW);
    permissions.addAll(dto.getPermissions());
    return new ApisAuth().setApisId(apiId).setCreator(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static ApisAuth replaceDtoToDomain(Long id, ApisAuthReplaceDto dto) {
    dto.getPermissions().add(ApiPermission.VIEW);
    return new ApisAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ApiAuthVo toDetailVo(ApisAuth apisAuth) {
    ApiAuthVo vo;
    switch (apisAuth.getAuthObjectType()) {
      case USER:
        vo = new ApiAuthUserDetailVo();
        break;
      case GROUP:
        vo = new ApiAuthGroupDetailVo();
        break;
      case DEPT:
        vo = new ApiAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(apisAuth.getAuthObjectType().getMessage());
    }
    vo.setId(apisAuth.getId());
    vo.setApisId(apisAuth.getApisId());
    vo.setPermissions(apisAuth.getAuths());
    vo.setAuthObjectType(apisAuth.getAuthObjectType());
    vo.setAuthObjectId(apisAuth.getAuthObjectId());
    vo.setCreator(apisAuth.getCreator());
    return vo;
  }

  public static ApisAuthCurrentVo toAuthCurrentVo(ApisAuthCurrent authCurrent) {
    return new ApisAuthCurrentVo().setProjectAuth(authCurrent.isProjectAuth())
        .setApisAuth(authCurrent.isApisAuth())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<ApisAuth> getSpecification(ApisAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "apisId", "authObjectId")
        .orderByFields("id", "apisId", "authObjectId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}

