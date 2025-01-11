package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import java.time.LocalDateTime;

public class FuncReviewCaseConverter {

  public static FuncReviewCase toReviewCase(FuncReview funcReviewDb, FuncCaseInfo caseInfo) {
    return new FuncReviewCase().setPlanId(funcReviewDb.getPlanId())
        .setReviewId(funcReviewDb.getId()).setReviewStatus(ReviewStatus.PENDING)
        .setCaseId(caseInfo.getId()).setCaseName(caseInfo.getName()).setCaseCode(caseInfo.getCode())
        .setCreatedBy(getUserId()).setCreatedDate(LocalDateTime.now());
  }

  public static void setReviewInfoAndStatus(FuncCase caseDb, FuncReviewCase reviewCase) {
    if (reviewCase.getReviewStatus().isFailed() || reviewCase.getReviewStatus().isPassed()) {
      caseDb.setReviewDate(LocalDateTime.now());
    }
    caseDb.setReviewerId(getUserId())
        .setReviewStatus(reviewCase.getReviewStatus())
        .setReviewDate(LocalDateTime.now()) // Last review time.
        .setReviewRemark(reviewCase.getReviewRemark())
        .setReviewNum(caseDb.getReviewNum() + 1)
        .setReviewFailNum(
            caseDb.getReviewFailNum() + (reviewCase.getReviewStatus().isFailed() ? 1 : 0));
  }

}
