package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisResponseAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisResponseAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisResponseCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisResponseQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockApisResponseFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockApisResponseAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
 */
@Component
public class MockApisResponseFacadeImpl implements MockApisResponseFacade {

  @Resource
  private MockApisResponseCmd mockApisResponseCmd;

  @Resource
  private MockApisResponseQuery mockApisResponseQuery;

  @Override
  public List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponseAddDto> dto) {
    return mockApisResponseCmd.add(apisId, dto.stream()
        .map(x -> addDtoToDomain(apisId, x)).toList());
  }

  @Override
  public void replace(Long apisId, List<MockApisResponseReplaceDto> dto) {
    mockApisResponseCmd.replace(apisId, dto.stream()
        .map(x -> replaceDtoToDomain(apisId, x)).toList());
  }

  @Override
  public void delete(Long apisId, HashSet<Long> responseIds) {
    mockApisResponseCmd.delete(apisId, responseIds);
  }

  @Override
  public List<MockApiResponseVo> all(Long apisId) {
    List<MockApisResponse> apisResponses = mockApisResponseQuery.findAllByApisId(apisId);
    return isEmpty(apisResponses) ? null : apisResponses.stream()
        .map(MockApisResponseAssembler::toMockApisResponseVo).toList();
  }

}
