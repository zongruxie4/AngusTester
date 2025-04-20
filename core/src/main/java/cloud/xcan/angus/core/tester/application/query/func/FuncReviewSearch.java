package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncReviewSearch {

  Page<FuncReview> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncReview> clz, String... matches);

}




