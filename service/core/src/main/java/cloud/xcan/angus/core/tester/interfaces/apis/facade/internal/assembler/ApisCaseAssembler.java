package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases.ApisCaseUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.cases.ApisCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.cases.ApisCaseListVo;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ApisCaseAssembler {

  public static ApisCase addDtoToDomain(ApisCaseAddDto dto) {
    return new ApisCase()
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setApisId(dto.getApisId())
        .setEnabled(nullSafe(dto.getEnabled(), true))
        .setType(nullSafe(dto.getType(), ApisCaseType.USER_DEFINED))
        .setTestMethod(dto.getTestMethod())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        .setCurrentServer(dto.getCurrentServer())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode())
        .setApisDeleted(false)
        .setExecTestNum(0)
        .setExecTestFailureNum(0);
  }

  public static ApisCase updateDtoToDomain(ApisCaseUpdateDto dto) {
    return new ApisCase()
        .setId(dto.getId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        // No modification allowed, only movement allowed
        //.setApisId(dto.getApisId())
        //.setEnabled(dto.getEnabled())
        //.setType(dto.getType())
        .setTestMethod(dto.getTestMethod())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        .setCurrentServer(dto.getCurrentServer())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode());
  }

  public static ApisCase replaceDtoToDomain(ApisCaseReplaceDto dto) {
    ApisCase apisCase = new ApisCase().setId(dto.getId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setApisId(dto.getApisId())
        .setEnabled(isNull(dto.getId()) ? nullSafe(dto.getEnabled(), true) : null)
        .setType(isNull(dto.getId()) ? nullSafe(dto.getType(), ApisCaseType.USER_DEFINED) : null)
        .setTestMethod(dto.getTestMethod())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        .setCurrentServer(dto.getCurrentServer())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode());
    if (isNull(dto.getId())) { // add
      apisCase.setApisDeleted(false)
          .setExecTestNum(0)
          .setExecTestFailureNum(0);
    }
    return apisCase;
  }

  public static ApisCaseDetailVo toDetailVo(ApisCase case0) {
    return new ApisCaseDetailVo().setId(case0.getId())
        .setName(case0.getName())
        .setDescription(case0.getDescription())
        .setApisId(case0.getApisId())
        .setApisSummary(case0.getApisSummary())
        .setApisServiceId(case0.getApisServiceId())
        .setEnabled(case0.getEnabled())
        .setType(case0.getType())
        .setTestMethod(case0.getTestMethod())
        .setProtocol(case0.getProtocol())
        .setMethod(case0.getMethod())
        .setEndpoint(case0.getEndpoint())
        .setCurrentServer(case0.getCurrentServer())
        .setParameters(case0.getParameters())
        .setRequestBody(case0.getRequestBody())
        .setAuthentication(case0.getAuthentication())
        .setAssertions(case0.getAssertions())
        .setDatasetActionOnEOF(case0.getDatasetActionOnEOF())
        .setDatasetSharingMode(case0.getDatasetSharingMode())
        .setApisDeleted(case0.getApisDeleted())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedByName(case0.getCreatedByName())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static ApisCaseListVo toListVo(ApisCaseInfo case0) {
    return new ApisCaseListVo().setId(case0.getId())
        .setName(case0.getName())
        .setDescription(case0.getDescription())
        .setApisId(case0.getApisId())
        .setApisSummary(case0.getApisSummary())
        .setEnabled(case0.getEnabled())
        .setType(case0.getType())
        .setTestMethod(case0.getTestMethod())
        .setProtocol(case0.getProtocol())
        .setMethod(case0.getMethod())
        .setEndpoint(case0.getEndpoint())
        .setApisDeleted(case0.getApisDeleted())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedByName(case0.getCreatedByName())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static GenericSpecification<ApisCaseInfo> getSpecification(ApisCaseFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("createdDate", "lastModifiedDate")
        .inAndNotFields("id")
        .orderByFields("id", "createdDate", "lastModifiedDate")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

}
