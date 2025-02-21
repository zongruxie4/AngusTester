package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncReviewSearch {

  Page<FuncReview> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncReview> clz, String... matches);

}




