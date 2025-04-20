package cloud.xcan.angus.core.tester.application.cmd.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorFuncCmd {

  IdKey<Long, Object> add(IndicatorFunc indicatorFunc);

  void replace(IndicatorFunc indicatorFunc);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




