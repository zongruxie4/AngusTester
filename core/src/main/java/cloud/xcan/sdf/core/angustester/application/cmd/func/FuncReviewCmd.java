package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface FuncReviewCmd {

  IdKey<Long, Object> add(FuncReview review);

  void update(FuncReview review);

  IdKey<Long, Object> replace(FuncReview review);

  void start(Long id);

  void block(Long id);

  void end(Long id);

  IdKey<Long, Object> clone(Long id);

  void reviewReset(Set<Long> ids, boolean reset);

  void delete(Long id);

  void deleteByPlanId(Collection<Long> planIds);

  void deleteByCaseId(Collection<Long> caseIds);
}
