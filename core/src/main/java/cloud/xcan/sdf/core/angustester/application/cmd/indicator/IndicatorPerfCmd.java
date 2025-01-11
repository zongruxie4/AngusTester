package cloud.xcan.sdf.core.angustester.application.cmd.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorPerfCmd {

  IdKey<Long, Object> add(IndicatorPerf indicatorPerf);

  void replace(IndicatorPerf indicatorPerf);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




