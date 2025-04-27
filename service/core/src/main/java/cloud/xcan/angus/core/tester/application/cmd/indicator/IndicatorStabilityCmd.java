package cloud.xcan.angus.core.tester.application.cmd.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorStabilityCmd {

  IdKey<Long, Object> add(IndicatorStability stability);

  void replace(IndicatorStability stability);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




