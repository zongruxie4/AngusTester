package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import java.util.List;

public interface FuncReviewCaseRecordQuery {

  List<FuncReviewCaseRecord> caseReview(Long caseId);
}
