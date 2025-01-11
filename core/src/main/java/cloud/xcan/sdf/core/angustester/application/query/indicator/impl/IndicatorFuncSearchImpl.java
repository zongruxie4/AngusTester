package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorFuncConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorFuncSearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFuncSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorFuncSearchImpl implements IndicatorFuncSearch {

  @Resource
  private IndicatorFuncSearchRepo indicatorPerfSearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorFunc> search(Set<SearchCriteria> criterias,
      PageRequest pageable, Class<IndicatorFunc> clz) {
    return new BizTemplate<Page<IndicatorFunc>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorFunc> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);

        return indicatorPerfSearchRepo.find(criterias, pageable, clz,
            IndicatorFuncConverter::objectArrToFunc, null);
      }
    }.execute();
  }

}



