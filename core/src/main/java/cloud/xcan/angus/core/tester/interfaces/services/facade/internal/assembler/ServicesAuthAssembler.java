package cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthCurrent;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthDeptDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthGroupDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthUserDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
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
        .setCreator(false)
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
    vo.setCreator(projectAuth.getCreator());
    return vo;
  }

  public static ServiceAuthCurrentVo toAuthCurrentVo(ServicesAuthCurrent authCurrent) {
    return new ServiceAuthCurrentVo().setServiceAuth(authCurrent.isServiceAuth())
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
