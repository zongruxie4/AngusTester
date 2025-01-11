package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.StabilityVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface IndicatorStabilityFacade {

  IdKey<Long, Object> add(StabilityAddDto dto);

  void replace(StabilityReplaceDto dto);

  void delete(Collection<Long> ids);

  void deleteByTarget(CombinedTargetType targetType, Long targetId);

  StabilityVo detail(CombinedTargetType targetType, Long targetId);

  StabilityVo detailOrDefault(CombinedTargetType targetType, Long targetId);

  PageResult<StabilityListVo> list(StabilityFindDto dto);

  PageResult<StabilityListVo> search(StabilitySearchDto dto);

}
