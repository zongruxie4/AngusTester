package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MOCK_EXCEPTION_SALF_LENGTH;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.lengthSafe;

import cloud.xcan.sdf.api.pojo.ApisRequestLog;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log.MockApisLogSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log.MockApisLogListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.time.LocalDateTime;
import java.util.Set;

public class MockApisLogAssembler {

  public static MockApisLog toMockApisLog(ApisRequestLog dto) {
    return new MockApisLog()
        .setRequestId(dto.getRequestId())
        .setRemote(dto.getRemote())
        .setExceptionMessage(lengthSafe(dto.getExceptionMessage(), MOCK_EXCEPTION_SALF_LENGTH))
        .setMockServiceId(dto.getMockServiceId())
        .setMockApisId(dto.getMockApisId())
        .setSummary(dto.getMockApisName())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getUri())
        .setPushbackFlag(dto.getPushbackFlag())
        .setPushbackRequestId(dto.getPushbackRequestId())
        .setRequestHeaders(dto.getRequestHeaders())
        .setRequestContentEncoding(dto.getRequestContentEncoding())
        .setRequestBody(dto.getRequestBody())
        .setRequestDate(dto.getRequestDate())
        .setResponseStatus(dto.getResponseStatus())
        .setResponseHeaders(dto.getResponseHeaders())
        .setResponseBody(dto.getResponseBody())
        .setResponseDate(dto.getResponseDate())
        .setCreatedDate(LocalDateTime.now());
  }

  public static MockApisLogDetailVo toApiLogDetailVo(MockApisLog apisLog) {
    return new MockApisLogDetailVo().setId(apisLog.getId())
        .setRequestId(apisLog.getRequestId())
        .setRemote(apisLog.getRemote())
        .setMockServiceId(apisLog.getMockServiceId())
        .setMockApisId(apisLog.getMockApisId())
        .setSummary(apisLog.getSummary())
        .setMethod(apisLog.getMethod())
        .setEndpoint(apisLog.getEndpoint())
        .setPushbackFlag(apisLog.getPushbackFlag())
        .setPushbackRequestId(apisLog.getPushbackRequestId())
        .setQueryParameters(apisLog.getQueryParameters())
        .setRequestHeaders(apisLog.getRequestHeaders())
        .setRequestContentEncoding(apisLog.getRequestContentEncoding())
        .setRequestBody(apisLog.getRequestBody())
        .setRequestDate(apisLog.getRequestDate())
        .setResponseStatus(apisLog.getResponseStatus())
        .setResponseHeaders(apisLog.getResponseHeaders())
        .setResponseBody(apisLog.getResponseBody())
        .setResponseDate(apisLog.getResponseDate())
        .setCreatedDate(apisLog.getCreatedDate());
  }

  public static MockApisLogListVo toApisLogListVo(MockApisLogInfo apisLog) {
    return new MockApisLogListVo().setId(apisLog.getId())
        .setRequestId(apisLog.getRequestId())
        .setRemote(apisLog.getRemote())
        .setMockServiceId(apisLog.getMockServiceId())
        .setMockApisId(apisLog.getMockApisId())
        .setSummary(apisLog.getSummary())
        .setMethod(apisLog.getMethod())
        .setEndpoint(apisLog.getEndpoint())
        .setPushbackFlag(apisLog.getPushbackFlag())
        .setRequestDate(apisLog.getRequestDate())
        .setResponseStatus(apisLog.getResponseStatus())
        .setCreatedDate(apisLog.getCreatedDate());
  }

  public static GenericSpecification<MockApisLogInfo> getSpecification(MockApisLogFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(MockApisLogSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint")
        .build();
  }
}
