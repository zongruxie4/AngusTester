package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.remote.search.SearchCriteria.equal;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewCaseSearch;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
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
