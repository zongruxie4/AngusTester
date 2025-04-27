package cloud.xcan.angus.core.tester.application.query.indicator.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.converter.IndicatorStabilityConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilitySearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilitySearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class IndicatorStabilitySearchImpl implements IndicatorStabilitySearch {

  @Resource
  private IndicatorStabilitySearchRepo indicatorStabilitySearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorStability> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<IndicatorStability> clz) {
    return new BizTemplate<Page<IndicatorStability>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorStability> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);

        return indicatorStabilitySearchRepo.find(criteria, pageable,
            clz, IndicatorStabilityConverter::objectArrToStability, null);
      }
    }.execute();
  }

}




