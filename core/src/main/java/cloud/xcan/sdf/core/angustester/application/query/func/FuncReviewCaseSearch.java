package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncReviewCaseSearch {

  Page<FuncReviewCase> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<FuncReviewCase> clz, String... matches);
}
