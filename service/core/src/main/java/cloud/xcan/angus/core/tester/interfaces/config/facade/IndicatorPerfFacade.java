package cloud.xcan.angus.core.tester.interfaces.config.facade;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.PerfReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.PerfListVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.PerfVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface IndicatorPerfFacade {

  IdKey<Long, Object> add(PerfAddDto dto);

  void replace(PerfReplaceDto dto);

  void delete(Collection<Long> ids);

  void deleteByTarget(CombinedTargetType targetType, Long targetId);

  PerfVo detail(CombinedTargetType targetType, Long targetId);

  PerfVo detailOrDefault(CombinedTargetType targetType, Long targetId);

  PageResult<PerfListVo> list(PerfFindDto dto);

}
