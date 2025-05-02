package cloud.xcan.angus.core.tester.application.query.func.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewCaseRecordQuery;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecordRepo;
import jakarta.annotation.Resource;
import java.util.List;

@Biz
public class FuncReviewCaseRecordQueryImpl implements FuncReviewCaseRecordQuery {

  @Resource
  private FuncReviewCaseRecordRepo funcReviewCaseRecordRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private UserManager userManager;

  @Override
  public List<FuncReviewCaseRecord> caseReview(Long caseId) {
    return new BizTemplate<List<FuncReviewCaseRecord>>() {
      @Override
      protected void checkParams() {
        funcCaseQuery.checkAndFind(caseId);
      }

      @Override
      protected List<FuncReviewCaseRecord> process() {
        List<FuncReviewCaseRecord> reviewCases = funcReviewCaseRecordRepo.findByCaseId(caseId);

        // Set user name and avatar
        userManager.setUserNameAndAvatar(reviewCases, "createdBy");
        return reviewCases;
      }
    }.execute();
  }

}
