package cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ServicesAssembler {

  public static Services addDtoToDomain(ServicesAddDto dto) {
    return new Services()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName().trim())
        .setAuth(nullSafe(dto.getAuth(), false))
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
        .setAuth(services.getAuth())
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
        .setAuth(services.getAuth())
        .setName(services.getName())
        .setStatus(services.getStatus())
        .setApisNum(services.getApisNum())
        .setCreatedBy(services.getCreatedBy())
        .setCreatedDate(services.getCreatedDate())
        .setHasApis(services.getHasApis())
        .setMockServiceId(services.getMockServiceId());
  }

  @NotNull
  public static ApiLocaleResult<PageResult<ServiceVo>> assembleAllowImportSampleStatus(
      PageResult<ServiceVo> result) {
    ApiLocaleResult<PageResult<ServiceVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
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
