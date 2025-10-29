package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecord;
import java.util.Collection;
import java.util.List;

public interface FuncReviewCaseRecordCmd {

  void add0(List<FuncReviewCaseRecord> records);

  void deleteByReviewCaseIdIn(Collection<Long> reviewCaseIds);
}
