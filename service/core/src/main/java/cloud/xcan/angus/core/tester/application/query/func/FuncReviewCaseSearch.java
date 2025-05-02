package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncReviewCaseSearch {

  Page<FuncReviewCase> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<FuncReviewCase> clz, String... matches);
}
