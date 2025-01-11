package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.FuncVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface IndicatorFuncFacade {

  IdKey<Long, Object> add(FuncAddDto dto);

  void replace(FuncReplaceDto dto);

  void delete(Collection<Long> ids);

  void deleteByTarget(CombinedTargetType targetType, Long targetId);

  FuncVo detail(CombinedTargetType targetType, Long targetId);

  FuncVo detailOrDefault(CombinedTargetType targetType, Long targetId);

  PageResult<FuncListVo> search(FuncSearchDto dto);

  PageResult<FuncListVo> list(FuncFindDto dto);

}
