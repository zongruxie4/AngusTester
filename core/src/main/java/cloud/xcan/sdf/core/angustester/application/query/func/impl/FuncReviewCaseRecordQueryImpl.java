package cloud.xcan.sdf.core.angustester.application.query.func.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseRecordQuery;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecordRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.List;
import javax.annotation.Resource;

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
