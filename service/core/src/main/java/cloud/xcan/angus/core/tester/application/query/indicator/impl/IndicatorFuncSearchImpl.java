package cloud.xcan.angus.core.tester.application.query.indicator.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.converter.IndicatorFuncConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncSearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorFuncSearchImpl implements IndicatorFuncSearch {

  @Resource
  private IndicatorFuncSearchRepo indicatorPerfSearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorFunc> search(Set<SearchCriteria> criteria,
      PageRequest pageable, Class<IndicatorFunc> clz) {
    return new BizTemplate<Page<IndicatorFunc>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorFunc> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);

        return indicatorPerfSearchRepo.find(criteria, pageable, clz,
            IndicatorFuncConverter::objectArrToFunc, null);
      }
    }.execute();
  }

}



