package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorFuncQuery {

  IndicatorFunc find(Long targetId, CombinedTargetType targetType);

  IndicatorFunc detailAndDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec, PageRequest pageable,
      Class<IndicatorFunc> clz);

  IndicatorFunc find0(Long targetId, CombinedTargetType targetType);

}




