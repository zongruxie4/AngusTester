package cloud.xcan.angus.core.tester.interfaces.indicator.facade;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface IndicatorStabilityFacade {

  IdKey<Long, Object> add(StabilityAddDto dto);

  void replace(StabilityReplaceDto dto);

  void delete(Collection<Long> ids);

  void deleteByTarget(CombinedTargetType targetType, Long targetId);

  StabilityVo detail(CombinedTargetType targetType, Long targetId);

  StabilityVo detailOrDefault(CombinedTargetType targetType, Long targetId);

  PageResult<StabilityListVo> list(StabilityFindDto dto);

}
