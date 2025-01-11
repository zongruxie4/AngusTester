package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler;

import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;

public class MockApisResponseAssembler {

  public static MockApisResponse addDtoToDomain(Long apiId, MockApisResponseAddDto dto) {
    return new MockApisResponse()
        .setName(dto.getName())
        .setMockApisId(apiId)
        .setMatch(dto.getMatch())
        .setContent(dto.getContent())
        .setPushbackFlag(nonNull(dto.getPushback()))
        .setPushback(dto.getPushback());
  }

  public static MockApisResponse replaceDtoToDomain(Long apiId, MockApisResponseReplaceDto dto) {
    return new MockApisResponse()
        .setName(dto.getName())
        .setMockApisId(apiId)
        .setMatch(dto.getMatch())
        .setContent(dto.getContent())
        .setPushbackFlag(nonNull(dto.getPushback()))
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
