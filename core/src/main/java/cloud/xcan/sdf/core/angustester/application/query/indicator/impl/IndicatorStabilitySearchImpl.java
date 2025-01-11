package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorStabilityConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorStabilitySearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilitySearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class IndicatorStabilitySearchImpl implements IndicatorStabilitySearch {

  @Resource
  private IndicatorStabilitySearchRepo indicatorStabilitySearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorStability> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<IndicatorStability> clz) {
    return new BizTemplate<Page<IndicatorStability>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorStability> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);

        return indicatorStabilitySearchRepo.find(criterias, pageable,
            clz, IndicatorStabilityConverter::objectArrToStability, null);
      }
    }.execute();
  }

}




