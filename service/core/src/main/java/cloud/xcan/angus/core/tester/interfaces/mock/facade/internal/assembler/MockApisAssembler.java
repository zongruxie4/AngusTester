package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisListVo;
import cloud.xcan.angus.model.remoting.vo.MockApiResponseInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisInfoVo;
import cloud.xcan.angus.model.remoting.vo.MockApisServiceInfoVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class MockApisAssembler {

  public static MockApis addDtoToDomain(MockApisAddDto dto) {
    return new MockApis()
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        .setSource(MockApisSource.CREATED)
        .setImportSource(null)
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        .setMockServiceId(dto.getMockServiceId())
        .setAssocServiceId(null)
        .setAssocApisId(null)
        .setRequestNum(0L)
        .setPushbackNum(0L)
        .setSimulateErrorNum(0L)
        .setExceptionNum(0L)
        .setSuccessNum(0L);
  }

  public static MockApis updateDtoToDomain(MockApisUpdateDto dto) {
    return new MockApis()
        .setId(dto.getId())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        //.setSource(MockServiceSource.CREATED)
        //.setImportSource(null)
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint());
  }

  public static MockApis replaceDtoToDomain(MockApisReplaceDto dto) {
    MockApis mockApis = new MockApis()
        .setId(dto.getId())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        //.setImportSource(null)
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint());
    if (isNull(dto.getId())) {
      mockApis.setSource(MockApisSource.CREATED) // Modifications are not allowed
          .setMockServiceId(dto.getMockServiceId()) // Modifications are not allowed
          .setRequestNum(0L)
          .setPushbackNum(0L)
          .setSimulateErrorNum(0L)
          .setExceptionNum(0L)
          .setSuccessNum(0L);
    }
    return mockApis;
  }

  public static cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisDetailVo toDetailVo(
      MockApis apis) {
    return new cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisDetailVo()
        .setId(apis.getId())
        .setProjectId(apis.getProjectId())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        .setMockServiceId(apis.getMockServiceId())
        .setMockServiceName(apis.getMockServiceName())
        .setMockServiceDomainUrl(apis.getMockServiceDomainUrl())
        .setMockServiceHostUrl(apis.getMockServiceHostUrl())
        .setAssocProjectId(apis.getAssocServiceId())
        .setAssocApisId(apis.getAssocApisId())
        .setAssocApisName(apis.getAssocApisName())
        .setRequestNum(apis.getRequestNum())
        .setPushbackNum(apis.getPushbackNum())
        .setSimulateErrorNum(apis.getSimulateErrorNum())
        .setExceptionNum(apis.getExceptionNum())
        .setSuccessNum(apis.getSuccessNum())
        .setInconsistentOperation(apis.getInconsistentOperation())
        .setAssocApisDeleted(apis.getAssocApisDeleted())
        .setApisMethod(apis.getApisMethod())
        .setApisEndpoint(apis.getApisEndpoint())
        .setResponses(nonNull(apis.getResponses()) ? apis.getResponses().stream()
            .map(MockApisAssembler::toMockApiResponseInfoVo).toList() : null)
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedBy(apis.getLastModifiedBy())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }

  private static MockApiResponseInfoVo toMockApiResponseInfoVo(MockApisResponse response) {
    return new MockApiResponseInfoVo()
        .setName(response.getName())
        .setMatch(response.getMatch())
        .setContent(response.getContent())
        .setPushback(response.getPushback());
  }

  public static MockApisListVo toApisListVo(MockApis apis) {
    return new MockApisListVo()
        .setId(apis.getId())
        .setProjectId(apis.getProjectId())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setSource(apis.getSource())
        .setImportSource(apis.getImportSource())
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        .setMockServiceId(apis.getMockServiceId())
        .setMockServiceName(apis.getMockServiceName())
        .setMockServiceDomainUrl(apis.getMockServiceDomainUrl())
        .setMockServiceHostUrl(apis.getMockServiceHostUrl())
        .setAssocProjectId(apis.getAssocServiceId())
        .setAssocApisId(apis.getAssocApisId())
        .setRequestNum(apis.getRequestNum())
        .setPushbackNum(apis.getPushbackNum())
        .setSimulateErrorNum(apis.getSimulateErrorNum())
        .setExceptionNum(apis.getExceptionNum())
        .setSuccessNum(apis.getSuccessNum())
        .setTenantId(apis.getTenantId())
        .setCreatedBy(apis.getCreatedBy())
        .setCreatedDate(apis.getCreatedDate())
        .setLastModifiedBy(apis.getLastModifiedBy())
        .setLastModifiedDate(apis.getLastModifiedDate());
  }

  public static MockApisServiceInfoVo toMockApisServiceInfoVo(MockService service) {
    return new MockApisServiceInfoVo()
        .setId(service.getId())
        .setProjectId(service.getProjectId())
        .setName(service.getName())
        .setNodeId(service.getNodeId())
        .setServiceDomainUrl(service.getServiceDomainUrl())
        .setServiceHostUrl(service.getServiceHostUrl())
        .setAuth(service.getAuth())
        .setApisSecurity(service.getApisSecurity())
        .setApisCors(service.getApisCors())
        .setSetting(service.getSetting());
  }

  public static MockApisInfoVo toMockApisDetailVo(MockApis apis) {
    return new MockApisInfoVo()
        .setId(apis.getId())
        .setProjectId(apis.getProjectId())
        .setName(apis.getName())
        .setMethod(apis.getMethod().getValue())
        .setEndpoint(apis.getEndpoint())
        .setAssocProjectId(apis.getAssocServiceId())
        .setAssocApisId(apis.getAssocApisId())
        .setResponses(nonNull(apis.getResponses()) ? apis.getResponses().stream()
            .map(MockApisAssembler::toMockApiResponseInfoVo).toList() : null);
  }

  @NotNull
  public static ApiLocaleResult<PageResult<MockApisListVo>> assembleAllowImportSampleStatus(
      PageResult<MockApisListVo> result) {
    ApiLocaleResult<PageResult<MockApisListVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }

  public static GenericSpecification<MockApis> getSpecification(MockApisFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "requestNum", "pushbackNum", "simulateErrorNum",
            "successNum", "exceptionNum")
        .orderByFields("id", "createdDate", "requestNum", "pushbackNum", "simulateErrorNum",
            "successNum", "exceptionNum")
        .matchSearchFields("summary", "endpoint")
        .build();
    return new GenericSpecification<>(filters);
  }

}
