package cloud.xcan.angus.core.tester.application.query.test;

import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecord;
import java.util.List;

public interface FuncReviewCaseRecordQuery {

  List<FuncReviewCaseRecord> caseReview(Long caseId);
}
