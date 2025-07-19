package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorPerfQuery {

  IndicatorPerf find(Long targetId, CombinedTargetType targetType);

  IndicatorPerf detailOrDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorPerf> list(GenericSpecification<IndicatorPerf> spec,
      PageRequest pageRequest, boolean fullTextSearch, String[] match);

  IndicatorPerf find0(Long targetId, CombinedTargetType targetType);

}




