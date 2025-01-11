package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisResponseCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockApisResponseQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockApisResponseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockApisResponseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class MockApisResponseFacadeImpl implements MockApisResponseFacade {

  @Resource
  private MockApisResponseCmd mockApisResponseCmd;

  @Resource
  private MockApisResponseQuery mockApisResponseQuery;

  @Override
  public List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponseAddDto> dtos) {
    return mockApisResponseCmd.add(apisId, dtos.stream()
        .map(dto -> MockApisResponseAssembler.addDtoToDomain(apisId, dto))
        .collect(Collectors.toList()));
  }

  @Override
  public void replace(Long apisId, List<MockApisResponseReplaceDto> dtos) {
    mockApisResponseCmd.replace(apisId, dtos.stream()
        .map(dto -> MockApisResponseAssembler.replaceDtoToDomain(apisId, dto))
        .collect(Collectors.toList()));
  }

  @Override
  public void delete(Long apisId, HashSet<Long> responseIds) {
    mockApisResponseCmd.delete(apisId, responseIds);
  }

  @Override
  public List<MockApiResponseVo> all(Long apisId) {
    List<MockApisResponse> apisResponses = mockApisResponseQuery.findAllByApisId(apisId);
    return isEmpty(apisResponses) ? null : apisResponses.stream()
        .map(MockApisResponseAssembler::toMockApisResponseVo).collect(Collectors.toList());
  }

}
