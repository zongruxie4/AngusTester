package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Objects;
import java.util.Set;

public class ApisAssembler {

  public static Apis archiveDtoToDomain(ApisArchiveDto dto) {
    Apis apis = new Apis()
        .setUnarchivedId(dto.getUnarchivedId())
        .setSource(ApiSource.CREATED)
        .setImportSource(null)
        .setServiceId(dto.getServiceId())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        // Fix:: Uri cannot be a null value, must be safe to ""
        .setEndpoint(nullSafe(dto.getEndpoint(), ""))
        /////// OpenAPI document start
        //.setTags(dto.getTags())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        .setOperationId(stringSafe(dto.getOperationId()))
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setResponses(nonNull(dto.getResponses()) ? dto.getResponses() : null)
        .setDeprecated(false)
        //.setSecurity(dto.getSecurity())
        .setCurrentServer(dto.getCurrentServer())
        .setCurrentServerId(
            nonNull(dto.getCurrentServer()) && isNotEmpty(dto.getCurrentServer().getExtensions())
                && nonNull(dto.getCurrentServer().getExtensions().get(ID_KEY))
                ? Long.parseLong(dto.getCurrentServer().getExtensions().get(ID_KEY).toString())
                : null)
        .setExtensions(dto.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setOwnerId(nonNull(dto.getOwnerId()) && dto.getOwnerId() > 0
            ? dto.getOwnerId() : getUserId())
        .setStatus(dto.getStatus())
        .setAuth(nullSafe(dto.getAuth(), false))
        .setSecured(nonNull(dto.getAuthentication()))
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode())
        .setTestFunc(nullSafe(dto.getTestFunc(), true))
        .setTestPerf(nullSafe(dto.getTestPerf(), true))
        .setTestStability(nullSafe(dto.getTestStability(), true))
        .setServiceDeleted(false)
        .setDeleted(false);
    apis.setEndpoint(apis.getEndpoint());
    return apis;
  }

  public static Apis updateDtoToDomain(ApisUpdateDto dto) {
    Apis apis = new Apis()
        .setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        //.setSource(dto.getSource())
        //.setImportSource(dto.getImportSource())
        //.setServiceId(dto.getServiceId())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        /////// OpenAPI document start
        .setTags(dto.getTags())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        .setOperationId(dto.getOperationId())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setResponses(dto.getResponses())
        .setDeprecated(dto.getDeprecated())
        .setSecurity(dto.getSecurity())
        .setCurrentServer(dto.getCurrentServer())
        .setCurrentServerId(
            nonNull(dto.getCurrentServer()) && isNotEmpty(dto.getCurrentServer().getExtensions())
                && nonNull(dto.getCurrentServer().getExtensions().get(ID_KEY))
                ? Long.parseLong(dto.getCurrentServer().getExtensions().get(ID_KEY).toString())
                : null)
        .setExtensions(dto.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setOwnerId(dto.getOwnerId())
        .setStatus(dto.getStatus())
        //.setAuth(true) // -> Fix:: Security Bug
        .setSecured(nonNull(dto.getAuthentication()) ? true : null)
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode())
        .setTestFunc(dto.getTestFunc())
        .setTestPerf(dto.getTestPerf())
        .setTestStability(dto.getTestStability());
    apis.setEndpoint(apis.getEndpoint());
    return apis;
  }

  public static Apis replaceDtoToDomain(ApisReplaceDto dto) {
    Apis apis = new Apis().setId(dto.getId())
        //.setProjectId(dto.getProjectId())
        //.setSource(dto.getSource())
        //.setImportSource(dto.getImportSource())
        .setServiceId(isNull(dto.getId()) ? dto.getServiceId() : null)
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        // Fix:: Uri cannot be a null value, must be safe to ""
        .setEndpoint(nullSafe(dto.getEndpoint(), ""))
        /////// OpenAPI document start
        .setTags(dto.getTags())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        .setOperationId(dto.getOperationId())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setResponses(dto.getResponses())
        .setDeprecated(dto.getDeprecated())
        .setSecurity(dto.getSecurity())
        .setCurrentServer(dto.getCurrentServer())
        .setCurrentServerId(
            nonNull(dto.getCurrentServer()) && isNotEmpty(dto.getCurrentServer().getExtensions())
                && nonNull(dto.getCurrentServer().getExtensions().get(ID_KEY))
                ? Long.parseLong(dto.getCurrentServer().getExtensions().get(ID_KEY).toString())
                : null)
        .setExtensions(dto.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setOwnerId(dto.getOwnerId())
        .setStatus(dto.getStatus())
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode());
    if (isNull(dto.getId())) {
      apis.setUnarchivedId(dto.getUnarchivedId())
          .setSource(ApiSource.CREATED)
          .setImportSource(null)
          .setAuth(nullSafe(dto.getAuth(), false))
          .setSecured(true)
          .setTestFunc(nullSafe(dto.getTestFunc(), true))
          .setTestPerf(nullSafe(dto.getTestPerf(), true))
          .setTestStability(nullSafe(dto.getTestStability(), true))
          .setServiceDeleted(false)
          .setDeleted(false);
    }
    apis.setEndpoint(apis.getEndpoint());
    return apis;
  }

  public static ApisDetailVo toDetailVo(Apis apis) {
    return new ApisDetailVo().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setProjectId(apis.getServiceId())
        //.setProjectName(apis.getServiceName())
        .setServiceId(apis.getServiceId())
        .setProtocol(apis.getProtocol())
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        /////// OpenAPI document start
        .setTags(apis.getTags())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setOperationId(apis.getOperationId())
        .setParameters(apis.getParameters())
        .setRequestBody(apis.getRequestBody())
        .setResponses(apis.getResponses())
        .setDeprecated(apis.getDeprecated())
        .setSecurity(apis.getSecurity())
        .setAvailableServers(apis.getAvailableServers())
        .setExtensions(apis.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(apis.getAuthentication())
        .setAssertions(apis.getAssertions())
        .setOwnerId(apis.getOwnerId())
        .setStatus(apis.getStatus())
        .setTagSchemas(apis.getTagSchemas())
        .setAuth(apis.getAuth())
        .setServiceAuth(apis.getServiceAuth())
        .setSecurity(apis.getSecurity())
        .setDatasetActionOnEOF(apis.getDatasetActionOnEOF())
        .setDatasetSharingMode(apis.getDatasetSharingMode())
        .setTestFunc(apis.getTestFunc())
        .setTestFuncPassed(apis.getTestFuncPassed())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerf(apis.getTestPerf())
        .setTestPerfPassed(apis.getTestPerfPassed())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStability(apis.getTestStability())
        .setTestStabilityPassed(apis.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        .setFavourite(apis.getFavourite())
        .setFollow(apis.getFollow())
        .setSyncName(apis.getSyncName())
        .setResolvedRefModels(apis.getResolvedRefModels())
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedBy(apis.getLastModifiedBy())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }

  public static ServicesApisInfoListVo toServiceApisVo(ApisBasicInfo apis) {
    return new ServicesApisInfoListVo().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setServiceId(apis.getServiceId())
        .setProtocol(apis.getProtocol())
        .setMethod(nonNull(apis.getMethod()) ? apis.getMethod().getValue() : null)
        .setEndpoint(apis.getEndpoint())
        .setSummary(apis.getSummary())
        .setOperationId(apis.getOperationId())
        //.setServers(apis.getServers())
        .setDeprecated(apis.getDeprecated())
        .setOwnerId(apis.getOwnerId())
        .setAvatar(apis.getAvatar())
        .setFavourite(apis.getFavourite())
        .setFollow(apis.getFollow())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedByName(apis.getCreatedByName())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate())
        .setAuth(apis.getAuth())
        .setServiceAuth(apis.getServiceAuth())
        .setTestFunc(apis.getTestFunc())
        .setTestFuncPassed(apis.getTestFuncPassed())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerf(apis.getTestPerf())
        .setTestPerfPassed(apis.getTestPerfPassed())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStability(apis.getTestStability())
        .setTestStabilityPassed(apis.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        .setTags(apis.getTags())
        .setStatus(apis.getStatus())
        .setMockServiceId(apis.getMockServiceId())
        .setMockApisId(apis.getMockApisId());
  }

  public static ApisInfoListVo toApisVo(ApisBasicInfo apis) {
    return new ApisInfoListVo().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setServiceId(apis.getServiceId())
        .setProtocol(apis.getProtocol())
        .setMethod(nonNull(apis.getMethod()) ? apis.getMethod().getValue() : null)
        .setEndpoint(apis.getEndpoint())
        .setSummary(apis.getSummary())
        .setOperationId(apis.getOperationId())
        //.setServers(apis.getServers())
        .setDeprecated(apis.getDeprecated())
        .setStatus(apis.getStatus())
        .setOwnerId(apis.getOwnerId())
        .setAvatar(apis.getAvatar())
        .setFavourite(apis.getFavourite())
        .setFollow(apis.getFollow())
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedByName(apis.getCreatedByName())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate())
        .setAuth(apis.getAuth())
        .setServiceAuth(apis.getServiceAuth())
        .setTestFunc(apis.getTestFunc())
        .setTestFuncPassed(apis.getTestFuncPassed())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerf(apis.getTestPerf())
        .setTestPerfPassed(apis.getTestPerfPassed())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStability(apis.getTestStability())
        .setTestStabilityPassed(apis.getTestStabilityPassed())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        .setStatus(apis.getStatus())
        .setMockServiceId(apis.getMockServiceId())
        .setMockApisId(apis.getMockApisId());
  }

  public static ApisAssocMockApiVo toAssocMockApis(MockApis mockApis) {
    return new ApisAssocMockApiVo()
        .setId(mockApis.getId())
        .setName(mockApis.getName())
        .setMockServiceId(mockApis.getMockServiceId())
        .setMockServiceName(mockApis.getMockServiceName())
        .setParentMockServiceId(mockApis.getParentMockServiceId())
        .setParentMockServiceName(mockApis.getParentMockServiceName())
        .setMockServiceHostUrl(mockApis.getMockServiceHostUrl())
        .setMockServiceDomainUrl(mockApis.getMockServiceDomainUrl())
        .setMethod(mockApis.getMethod())
        .setEndpoint(mockApis.getEndpoint())
        .setSource(mockApis.getSource())
        .setImportSource(mockApis.getImportSource())
        .setRequestNum(mockApis.getRequestNum())
        .setPushbackNum(mockApis.getPushbackNum())
        .setSimulateErrorNum(mockApis.getSimulateErrorNum())
        .setSuccessNum(mockApis.getSuccessNum())
        .setExceptionNum(mockApis.getExceptionNum())
        .setCreatedBy(mockApis.getCreatedBy())
        .setCreatedDate(mockApis.getCreatedDate())
        .setInconsistentOperation(mockApis.getInconsistentOperation());
  }

  public static GenericSpecification<ApisBasicInfo> getSpecification(ServiceApisFindDto dto) {
    if (Objects.nonNull(dto.getAdmin()) && dto.getAdmin()) {
      dto.getFilters().add(SearchCriteria.equal("admin", "true"));
    }
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("protocol", "id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint", "extSearchMerge")
        .orderByFields("endpoint", "createdDate", "createdBy", "id", "summary")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static GenericSpecification<ApisBasicInfo> getSpecification(ApisInfoFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("protocol", "id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint", "extSearchMerge")
        .orderByFields("endpoint", "createdDate", "createdBy", "id", "summary")
        .build();
    return new GenericSpecification<>(filters);
  }

}
