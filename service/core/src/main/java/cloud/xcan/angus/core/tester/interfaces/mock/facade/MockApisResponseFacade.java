package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface MockApisResponseFacade {

  List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponseAddDto> dto);

  void replace(Long apisId, List<MockApisResponseReplaceDto> dto);

  void delete(Long apisId, HashSet<Long> responseIds);

  List<MockApiResponseVo> all(Long apisId);
}
