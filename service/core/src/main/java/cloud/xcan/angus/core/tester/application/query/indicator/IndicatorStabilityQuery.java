package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorStability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorStabilityQuery {

  IndicatorStability find(Long targetId, CombinedTargetType targetType);

  IndicatorStability detailOrDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorStability> list(GenericSpecification<IndicatorStability> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  IndicatorStability find0(Long targetId, CombinedTargetType targetType);


}




