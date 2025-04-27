package cloud.xcan.angus.core.tester.interfaces.indicator.facade;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.tester.indicator.vo.FuncVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
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
