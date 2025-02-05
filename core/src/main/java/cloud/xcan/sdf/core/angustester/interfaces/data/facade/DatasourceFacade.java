package cloud.xcan.sdf.core.angustester.interfaces.data.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceTestVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

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
