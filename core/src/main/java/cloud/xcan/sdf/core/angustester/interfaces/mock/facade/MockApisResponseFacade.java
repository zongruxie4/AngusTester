package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response.MockApisResponseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.response.MockApiResponseVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface MockApisResponseFacade {

  List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponseAddDto> dtos);

  void replace(Long apisId, List<MockApisResponseReplaceDto> dtos);

  void delete(Long apisId, HashSet<Long> responseIds);

  List<MockApiResponseVo> all(Long apisId);
}
