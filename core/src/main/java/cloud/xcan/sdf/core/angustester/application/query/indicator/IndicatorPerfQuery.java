package cloud.xcan.sdf.core.angustester.application.query.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorPerfQuery {

  IndicatorPerf find(Long targetId, CombinedTargetType targetType);

  IndicatorPerf detailAndDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorPerf> list(GenericSpecification<IndicatorPerf> spec, PageRequest pageable,
      Class<IndicatorPerf> clz);

  IndicatorPerf find0(Long targetId, CombinedTargetType targetType);

}




