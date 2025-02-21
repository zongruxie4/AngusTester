package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;
import static io.swagger.v3.oas.models.extension.ExtensionKey.ID_KEY;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Objects;
import java.util.Set;

public class ApisAssembler {

  public static Apis archiveDtoToDomain(ApisArchiveDto dto) {
    Apis apis = new Apis()
        .setUnarchiveId(dto.getUnarchiveId())
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
        .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
        .setSecurityFlag(nonNull(dto.getAuthentication()))
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode())
        .setTestFuncFlag(nullSafe(dto.getTestFuncFlag(), true))
        .setTestPerfFlag(nullSafe(dto.getTestPerfFlag(), true))
        .setTestStabilityFlag(nullSafe(dto.getTestStabilityFlag(), true))
        .setServiceDeletedFlag(false)
        .setDeletedFlag(false);
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
        //.setAuthFlag(true) // -> Fix:: Security Bug
        .setSecurityFlag(nonNull(dto.getAuthentication()) ? true : null)
        .setDatasetActionOnEOF(dto.getDatasetActionOnEOF())
        .setDatasetSharingMode(dto.getDatasetSharingMode())
        .setTestFuncFlag(dto.getTestFuncFlag())
        .setTestPerfFlag(dto.getTestPerfFlag())
        .setTestStabilityFlag(dto.getTestStabilityFlag());
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
      apis.setUnarchiveId(dto.getUnarchiveId())
          .setSource(ApiSource.CREATED)
          .setImportSource(null)
          .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
          .setSecurityFlag(true)
          .setTestFuncFlag(nullSafe(dto.getTestFuncFlag(), true))
          .setTestPerfFlag(nullSafe(dto.getTestPerfFlag(), true))
          .setTestStabilityFlag(nullSafe(dto.getTestStabilityFlag(), true))
          .setServiceDeletedFlag(false)
          .setDeletedFlag(false);
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
        .setAuthFlag(apis.getAuthFlag())
        .setServiceAuthFlag(apis.getServiceAuthFlag())
        .setSecurityFlag(apis.getSecurityFlag())
        .setDatasetActionOnEOF(apis.getDatasetActionOnEOF())
        .setDatasetSharingMode(apis.getDatasetSharingMode())
        .setTestFuncFlag(apis.getTestFuncFlag())
        .setTestFuncPassedFlag(apis.getTestFuncPassedFlag())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerfFlag(apis.getTestPerfFlag())
        .setTestPerfPassedFlag(apis.getTestPerfPassedFlag())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStabilityFlag(apis.getTestStabilityFlag())
        .setTestStabilityPassedFlag(apis.getTestStabilityPassedFlag())
        .setTestStabilityFailureMessage(apis.getTestStabilityFailureMessage())
        .setFavouriteFlag(apis.getFavouriteFlag())
        .setFollowFlag(apis.getFollowFlag())
        .setSyncName(apis.getSyncName())
        .setResolvedRefModels(apis.getResolvedRefModels())
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }

  public static ServicesApisInfoListVo toServiceApisVo(ApisBasicInfo apis) {
    return new ServicesApisInfoListVo().setId(apis.getId())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setProtocol(apis.getProtocol())
        .setMethod(nonNull(apis.getMethod()) ? apis.getMethod().getValue() : null)
        .setEndpoint(apis.getEndpoint())
        .setSummary(apis.getSummary())
        .setOperationId(apis.getOperationId())
        //.setServers(apis.getServers())
        .setDeprecated(apis.getDeprecated())
        .setOwnerId(apis.getOwnerId())
        .setAvatar(apis.getAvatar())
        .setFavouriteFlag(apis.getFavouriteFlag())
        .setFollowFlag(apis.getFollowFlag())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedByName(apis.getCreatedByName())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate())
        .setAuthFlag(apis.getAuthFlag())
        .setServiceAuthFlag(apis.getServiceAuthFlag())
        .setTestFuncFlag(apis.getTestFuncFlag())
        .setTestFuncPassedFlag(apis.getTestFuncPassedFlag())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerfFlag(apis.getTestPerfFlag())
        .setTestPerfPassedFlag(apis.getTestPerfPassedFlag())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStabilityFlag(apis.getTestStabilityFlag())
        .setTestStabilityPassedFlag(apis.getTestStabilityPassedFlag())
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
        .setFavouriteFlag(apis.getFavouriteFlag())
        .setFollowFlag(apis.getFollowFlag())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedByName(apis.getCreatedByName())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedDate(apis.getLastModifiedDate())
        .setAuthFlag(apis.getAuthFlag())
        .setServiceAuthFlag(apis.getServiceAuthFlag())
        .setTestFuncFlag(apis.getTestFuncFlag())
        .setTestFuncPassedFlag(apis.getTestFuncPassedFlag())
        .setTestFuncFailureMessage(apis.getTestFuncFailureMessage())
        .setTestPerfFlag(apis.getTestPerfFlag())
        .setTestPerfPassedFlag(apis.getTestPerfPassedFlag())
        .setTestPerfFailureMessage(apis.getTestPerfFailureMessage())
        .setTestStabilityFlag(apis.getTestStabilityFlag())
        .setTestStabilityPassedFlag(apis.getTestStabilityPassedFlag())
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
        .setInconsistentOperationFlag(mockApis.getInconsistentOperationFlag());
  }

  public static GenericSpecification<ApisBasicInfo> getSpecification(ServiceApisFindDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
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

  public static Set<SearchCriteria> getSearchCriteria(ServicesApisSearchDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
    }
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("protocol", "id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint", "extSearchMerge")
        .orderByFields("endpoint", "createdDate", "createdBy", "id", "summary")
        .build();
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

  public static Set<SearchCriteria> getSearchCriteria(ApisInfoSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("protocol", "id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint", "extSearchMerge")
        .orderByFields("endpoint", "createdDate", "createdBy", "id", "summary")
        .build();
  }

}
