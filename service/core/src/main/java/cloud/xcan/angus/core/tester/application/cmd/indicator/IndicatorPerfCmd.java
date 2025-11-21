package cloud.xcan.angus.core.tester.application.cmd.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorPerf;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface IndicatorPerfCmd {

  IdKey<Long, Object> add(IndicatorPerf indicatorPerf);

  void replace(IndicatorPerf indicatorPerf);

  void delete(Collection<Long> ids);

  void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType);
}




