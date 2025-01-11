package cloud.xcan.sdf.core.angustester.application.cmd.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorFuncCmd {

  IdKey<Long, Object> add(IndicatorFunc indicatorFunc);

  void replace(IndicatorFunc indicatorFunc);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




