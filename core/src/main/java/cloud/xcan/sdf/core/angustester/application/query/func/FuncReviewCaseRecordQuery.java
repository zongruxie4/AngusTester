package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import java.util.List;

public interface FuncReviewCaseRecordQuery {

  List<FuncReviewCaseRecord> caseReview(Long caseId);
}
