package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorPerfConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorPerfSearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorPerfSearchImpl implements IndicatorPerfSearch {

  @Resource
  private IndicatorPerfSearchRepo indicatorPerfSearchRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<IndicatorPerf> search(Set<SearchCriteria> criterias,
      PageRequest pageable, Class<IndicatorPerf> clz) {
    return new BizTemplate<Page<IndicatorPerf>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorPerf> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);

        return indicatorPerfSearchRepo.find(criterias, pageable, clz,
            IndicatorPerfConverter::objectArrToPerf, null);
      }
    }.execute();
  }

}



