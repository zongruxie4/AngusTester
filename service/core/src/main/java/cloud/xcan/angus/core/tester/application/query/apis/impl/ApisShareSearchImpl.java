package cloud.xcan.angus.core.tester.application.query.apis.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisShareSearch;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
