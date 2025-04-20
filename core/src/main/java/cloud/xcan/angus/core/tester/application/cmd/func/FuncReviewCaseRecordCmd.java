package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import java.util.Collection;
import java.util.List;

public interface FuncReviewCaseRecordCmd {

  void add0(List<FuncReviewCaseRecord> records);

  void deleteByReviewCaseIdIn(Collection<Long> reviewCaseIds);
}
