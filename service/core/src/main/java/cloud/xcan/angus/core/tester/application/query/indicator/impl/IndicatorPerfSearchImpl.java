package cloud.xcan.angus.core.tester.application.query.indicator.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.converter.IndicatorPerfConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfSearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorPerfSearchImpl implements IndicatorPerfSearch {

  @Resource
  private IndicatorPerfSearchRepo indicatorPerfSearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorPerf> search(Set<SearchCriteria> criteria,
      PageRequest pageable, Class<IndicatorPerf> clz) {
    return new BizTemplate<Page<IndicatorPerf>>() {

      @Override
      protected Page<IndicatorPerf> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);

        return indicatorPerfSearchRepo.find(criteria, pageable, clz,
            IndicatorPerfConverter::objectArrToPerf, null);
      }
    }.execute();
  }

}



