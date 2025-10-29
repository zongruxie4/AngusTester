package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCase;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FuncReviewCaseCmd {

  List<IdKey<Long, Object>> add(Long reviewId, Set<Long> caseIds);

  void review(List<FuncReviewCase> reviewCases);

  void reviewReset(Set<Long> ids, boolean reset);

  void delete(Collection<Long> ids);


}
