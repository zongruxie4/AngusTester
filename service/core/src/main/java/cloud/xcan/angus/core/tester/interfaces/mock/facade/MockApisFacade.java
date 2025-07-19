package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisAddDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.MockApisUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisDetailVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.MockApisListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public interface MockApisFacade {

  List<IdKey<Long, Object>> add(List<MockApisAddDto> dto);

  void update(List<MockApisUpdateDto> dto);

  List<IdKey<Long, Object>> replace(List<MockApisReplaceDto> dto);

  IdKey<Long, Object> clone(Long id);

  void move(HashSet<Long> ids, Long targetServiceId);

  void instanceSync(Long id);

  IdKey<Long, Object> copyApisAdd(Long mockServiceId, Long apisId);

  IdKey<Long, Object> assocApisAdd(Long mockServiceId, Long apisId);

  void assocApisUpdate(Long mockApisId, Long apisId);

  void assocDelete(HashSet<Long> ids);

  void delete(Collection<Long> ids);

  MockApisDetailVo detail(Long id);

  PageResult<MockApisListVo> list(MockApisFindDto dto);

}
