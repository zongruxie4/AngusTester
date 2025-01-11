package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import java.util.Collection;
import java.util.List;

public interface FuncReviewCaseRecordCmd {

  void add0(List<FuncReviewCaseRecord> records);

  void deleteByReviewCaseIdIn(Collection<Long> reviewCaseIds);
}
