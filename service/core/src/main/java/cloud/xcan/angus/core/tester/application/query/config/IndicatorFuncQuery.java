package cloud.xcan.angus.core.tester.application.query.config;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFunc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorFuncQuery {

  IndicatorFunc find(Long targetId, CombinedTargetType targetType);

  IndicatorFunc detailOrDefault(CombinedTargetType targetType, Long targetId);

  Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  IndicatorFunc find0(Long targetId, CombinedTargetType targetType);

}




