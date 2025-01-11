package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findAdminFlagInCriteria;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.api.commonlink.user.UserRepo;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.schema.ServicesSchema;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServicesAssembler {

  public static Services addDtoToDomain(ServicesAddDto dto) {
    return new Services()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName().trim())
        .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
        .setStatus(ApiStatus.UNKNOWN)
        .setSource(ApiSource.CREATED)
        .setImportSource(null);
  }

  public static ServicesDetailVo toDetailVo(Services services) {
    ServicesDetailVo detailVo = new ServicesDetailVo()
        .setId(services.getId())
        .setProjectId(services.getProjectId())
        .setSource(services.getSource())
        .setImportSource(services.getImportSource())
        .setName(services.getName())
        .setAuthFlag(services.getAuthFlag())
        .setStatus(services.getStatus())
        .setApisNum(services.getApisNum())
        .setApisCaseNum(services.getApisCaseNum())
        .setCreatedBy(services.getCreatedBy())
        .setCreatedDate(services.getCreatedDate())
        .setLastModifiedBy(services.getLastModifiedBy())
        .setLastModifiedDate(services.getLastModifiedDate());
    Object schema = PrincipalContext.getExtension("servicesSchema");
    if (isNotNull(schema)) {
      detailVo.setSchema(ServicesSchemaAssembler.toDetailVo((ServicesSchema) schema));
    }
    return detailVo;
  }

  public static List<ServiceVo> toVo(Collection<Services> allProjects) {
    List<ServiceVo> vos = new ArrayList<>();
    for (Services project : allProjects) {
      vos.add(toServiceVo(project));
    }
    return vos;
  }

  private static ServiceVo toServiceVo(Services services) {
    return new ServiceVo().setId(services.getId())
        .setProjectId(services.getProjectId())
        .setAuthFlag(services.getAuthFlag())
        .setName(services.getName())
        .setStatus(services.getStatus())
        .setApisNum(services.getApisNum())
        .setCreatedBy(services.getCreatedBy())
        .setCreatedDate(services.getCreatedDate())
        .setHasApisFlag(services.getHasApisFlag())
        .setMockServiceId(services.getMockServiceId());
  }

  public static Set<Long> getAdminConditionValue(ServicesSearchDto dto,
      GenericSpecification<Services> spec, CommonQuery commonQuery, UserRepo userRepo) {
    boolean adminFlag = findAdminFlagInCriteria(spec.getCriterias(), true);
    adminFlag = nonNull(dto.getAdminFlag()) ? dto.getAdminFlag() : adminFlag;
    List<Long> authObjectIds;
    if (!adminFlag || !commonQuery.isAdminUser()) {
      authObjectIds = userRepo.findOrgIdsById(getUserId());
      authObjectIds.add(getUserId());
      return new HashSet<>(authObjectIds);
    }
    return Collections.emptySet();
  }

  public static Set<SearchCriteria> getSearchCriteria(ServicesSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id")
        .orderByFields("id", "name", "createdBy", "createdDate")
        .matchSearchFields("name")
        .build();
  }

  public static GenericSpecification<Services> getSpecification(ServicesFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id")
        .orderByFields("id", "name", "createdBy", "createdDate")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }
}
