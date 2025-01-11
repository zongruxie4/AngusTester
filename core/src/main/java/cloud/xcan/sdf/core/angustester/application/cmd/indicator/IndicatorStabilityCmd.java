package cloud.xcan.sdf.core.angustester.application.cmd.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorStabilityCmd {

  IdKey<Long, Object> add(IndicatorStability stability);

  void replace(IndicatorStability stability);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




