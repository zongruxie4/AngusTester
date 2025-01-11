package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuth;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class ServicesAuthAssembler {

  public static ServicesAuth addDtoToDomain(Long projectId, ServicesAddAuthDto dto) {
    Set<ServicesPermission> permissions = new HashSet<>();
    permissions.add(ServicesPermission.VIEW);
    permissions.addAll(dto.getPermissions());
    return new ServicesAuth().setServiceId(projectId)
        .setCreatorFlag(false)
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId())
        .setAuths(new ArrayList<>(permissions));
  }

  public static ServicesAuth replaceDtoToDomain(Long id, ServicesAuthReplaceDto dto) {
    dto.getPermissions().add(ServicesPermission.VIEW);
    return new ServicesAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ServicesAuthVo toDetailVo(ServicesAuth projectAuth) {
    ServicesAuthVo vo;
    switch (projectAuth.getAuthObjectType()) {
      case USER:
        vo = new ServicesAuthUserDetailVo();
        break;
      case GROUP:
        vo = new ServicesAuthGroupDetailVo();
        break;
      case DEPT:
        vo = new ServicesAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(projectAuth.getAuthObjectType().getMessage());
    }
    vo.setId(projectAuth.getId());
    vo.setServiceId(projectAuth.getServiceId());
    vo.setPermissions(projectAuth.getAuths());
    vo.setAuthObjectType(projectAuth.getAuthObjectType());
    vo.setAuthObjectId(projectAuth.getAuthObjectId());
    vo.setCreatorFlag(projectAuth.getCreatorFlag());
    return vo;
  }

  public static ServiceAuthCurrentVo toAuthCurrentVo(ServicesAuthCurrent authCurrent) {
    return new ServiceAuthCurrentVo().setServiceAuthFlag(authCurrent.isServiceAuthFlag())
        .setPermissions(authCurrent.getPermissions());
  }

  public static Specification<ServicesAuth> getSpecification(ServicesAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "serviceId")
        .orderByFields("id", "serviceId", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}