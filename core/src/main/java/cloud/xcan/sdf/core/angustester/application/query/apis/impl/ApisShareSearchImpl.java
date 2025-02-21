package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisShareSearch;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisShareSearchImpl implements ApisShareSearch {

  @Resource
  private ApisShareSearchRepo apisShareSearchRepo;

  @Override
  public Page<ApisShare> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisShare> clz, String... matches) {
    return new BizTemplate<Page<ApisShare>>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ApisShare> process() {
        return apisShareSearchRepo.find(criteria, pageable, ApisShare.class, matches);
      }
    }.execute();
  }

}
