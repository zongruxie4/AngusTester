package cloud.xcan.sdf.core.angustester.application.query.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorStabilityQuery {

  IndicatorStability find(Long targetId, CombinedTargetType targetType);

  IndicatorStability detailAndDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorStability> list(GenericSpecification<IndicatorStability> spec,
      PageRequest pageable, Class<IndicatorStability> clz);

  IndicatorStability find0(Long targetId, CombinedTargetType targetType);

}




