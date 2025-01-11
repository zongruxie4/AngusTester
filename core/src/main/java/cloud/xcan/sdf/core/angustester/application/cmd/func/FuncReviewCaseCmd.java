package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FuncReviewCaseCmd {

  List<IdKey<Long, Object>> add(Long reviewId, Set<Long> caseIds);

  void review(List<FuncReviewCase> reviewCases);

  void reviewReset(Set<Long> ids, boolean reset);

  void delete(Collection<Long> ids);


}
