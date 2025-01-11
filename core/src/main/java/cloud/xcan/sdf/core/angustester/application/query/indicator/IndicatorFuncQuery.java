package cloud.xcan.sdf.core.angustester.application.query.indicator;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorFuncQuery {

  IndicatorFunc find(Long targetId, CombinedTargetType targetType);

  IndicatorFunc detailAndDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec, PageRequest pageable,
      Class<IndicatorFunc> clz);

  IndicatorFunc find0(Long targetId, CombinedTargetType targetType);

}




