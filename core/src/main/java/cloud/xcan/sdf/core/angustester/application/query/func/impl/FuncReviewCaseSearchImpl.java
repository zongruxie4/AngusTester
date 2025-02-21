package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import static cloud.xcan.sdf.api.search.SearchCriteria.equal;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseSearch;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCaseSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncReviewCaseSearchImpl implements FuncReviewCaseSearch {

  @Resource
  private FuncReviewCaseSearchRepo funcReviewCaseSearchRepo;

  @Resource
  private FuncReviewCaseQuery funcReviewCaseQuery;

  @Override
  public Page<FuncReviewCase> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<FuncReviewCase> clz, String... matches) {
    return new BizTemplate<Page<FuncReviewCase>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<FuncReviewCase> process() {
        criteria.add(equal("lastReview", true));

        Page<FuncReviewCase> page = funcReviewCaseSearchRepo.find(criteria, pageable, clz, matches);
        funcReviewCaseQuery.setCaseInfo(page.getContent());
        return page;
      }
    }.execute();
  }
}
