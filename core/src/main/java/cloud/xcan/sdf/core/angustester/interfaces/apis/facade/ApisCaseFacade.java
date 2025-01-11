package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases.ApisCaseUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases.ApisCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.cases.ApisCaseListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ApisCaseFacade {

  List<IdKey<Long, Object>> add(List<ApisCaseAddDto> dto);

  void update(List<ApisCaseUpdateDto> dto);

  void replace(List<ApisCaseReplaceDto> dto);

  void rename(Long id, String name);

  void enabled(Set<Long> ids, Boolean enabled);

  void syncToScript(Long apisId);

  List<IdKey<Long, Object>> clone(Set<Long> ids);

  void delete(Collection<Long> ids);

  ApisCaseDetailVo detail(Long id);

  PageResult<ApisCaseListVo> list(ApisCaseFindDto dto);

  PageResult<ApisCaseListVo> search(ApisCaseSearchDto dto);

}
