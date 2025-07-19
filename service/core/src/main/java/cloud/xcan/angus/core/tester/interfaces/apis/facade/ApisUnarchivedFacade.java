package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.ApisUnarchivedUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedDetailVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.ApisUnarchivedListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface ApisUnarchivedFacade {

  List<IdKey<Long, Object>> add(List<ApisUnarchivedAddDto> dto);

  void update(List<ApisUnarchivedUpdateDto> dto);

  void delete(Long id);

  void rename(Long id, String name);

  void deleteAll();

  ApisUnarchivedDetailVo detail(Long id);

  Long count(Long projectId);

  PageResult<ApisUnarchivedListVo> list(ApisUnarchivedFindDto dto);

}
