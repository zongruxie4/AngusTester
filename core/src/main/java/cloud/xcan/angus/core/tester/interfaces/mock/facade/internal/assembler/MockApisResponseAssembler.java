package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;

public class MockApisResponseAssembler {

  public static MockApisResponse addDtoToDomain(Long apiId, MockApisResponseAddDto dto) {
    return new MockApisResponse()
        .setName(dto.getName())
        .setMockApisId(apiId)
        .setMatch(dto.getMatch())
        .setContent(dto.getContent())
        .setEnablePushback(nonNull(dto.getPushback()))
        .setPushback(dto.getPushback());
  }

  public static MockApisResponse replaceDtoToDomain(Long apiId, MockApisResponseReplaceDto dto) {
    return new MockApisResponse()
        .setName(dto.getName())
        .setMockApisId(apiId)
        .setMatch(dto.getMatch())
        .setContent(dto.getContent())
        .setEnablePushback(nonNull(dto.getPushback()))
        .setPushback(dto.getPushback());
  }

  public static MockApiResponseVo toMockApisResponseVo(MockApisResponse apisResponse) {
    return new MockApiResponseVo()
        .setId(apisResponse.getId())
        .setApisId(apisResponse.getMockApisId())
        .setMockServiceId(apisResponse.getMockServiceId())
        .setName(apisResponse.getName())
        .setMatch(apisResponse.getMatch())
        .setContent(apisResponse.getContent())
        .setPushback(apisResponse.getPushback());
  }

}
