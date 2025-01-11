package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthDeptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthGroupDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthUserDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MockServiceAuthAssembler {

  public static MockServiceAuth addDtoToDomain(Long serviceId, ServiceAddAuthDto dto) {
    Set<MockServicePermission> permissions = new HashSet<>();
    permissions.add(MockServicePermission.VIEW);
    if (ObjectUtils.isNotEmpty(dto.getPermissions())) {
      permissions.addAll(dto.getPermissions());
    }
    return new MockServiceAuth()
        .setMockServiceId(serviceId)
        .setCreatorFlag(false)
        .setAuths(new ArrayList<>(permissions))
        .setAuthObjectType(dto.getAuthObjectType())
        .setAuthObjectId(dto.getAuthObjectId());
  }

  public static MockServiceAuth replaceDtoToDomain(Long id, ServiceAuthReplaceDto dto) {
    dto.getPermissions().add(MockServicePermission.VIEW);
    return new MockServiceAuth().setId(id).setAuths(new ArrayList<>(dto.getPermissions()));
  }

  public static ServiceAuthVo toDetailVo(MockServiceAuth serviceAuth) {
    ServiceAuthVo vo;
    switch (serviceAuth.getAuthObjectType()) {
      case USER:
        vo = new ServiceAuthUserDetailVo();
        break;
      case GROUP:
        vo = new ServiceAuthGroupDetailVo();
        break;
      case DEPT:
        vo = new ServiceAuthDeptDetailVo();
        break;
      default:
        throw ResourceNotFound.of(serviceAuth.getAuthObjectType().getMessage());
    }
    vo.setId(serviceAuth.getId());
    vo.setPermissions(serviceAuth.getAuths());
    vo.setAuthObjectType(serviceAuth.getAuthObjectType());
    vo.setAuthObjectId(serviceAuth.getAuthObjectId());
    vo.setCreatorFlag(serviceAuth.getCreatorFlag());
    return vo;
  }

  public static GenericSpecification<MockServiceAuth> getSpecification(Long serviceId,
      ServiceAuthFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
