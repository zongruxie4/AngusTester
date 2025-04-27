package cloud.xcan.angus.core.tester.interfaces.data.facade;

import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceSearchDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceTestVo;
import cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource.DatasourceVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface DatasourceFacade {

  IdKey<Long, Object> add(DatasourceAddDto dto);

  IdKey<Long, Object> replace(DatasourceReplaceDto dto);

  DatasourceTestVo testById(Long id);

  DatasourceTestVo testByConfig(DatasourceTestDto dto);

  void delete(Long id);

  DatasourceDetailVo detail(Long id);

  PageResult<DatasourceVo> list(DatasourceFindDto dto);

  PageResult<DatasourceVo> search(DatasourceSearchDto dto);

}
