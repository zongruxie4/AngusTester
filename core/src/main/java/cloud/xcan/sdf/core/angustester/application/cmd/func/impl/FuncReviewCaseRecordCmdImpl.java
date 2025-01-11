package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCaseRecordCmd;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecordRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;

@Biz
public class FuncReviewCaseRecordCmdImpl extends CommCmd<FuncReviewCaseRecord, Long>
    implements FuncReviewCaseRecordCmd {

  @Resource
  private FuncReviewCaseRecordRepo funcReviewCaseRecordRepo;

  @Override
  public void add0(List<FuncReviewCaseRecord> records) {
    batchInsert0(records);
  }

  @Override
  public void deleteByReviewCaseIdIn(Collection<Long> reviewCaseIds) {
    funcReviewCaseRecordRepo.deleteByReviewCaseIdIn(reviewCaseIds);
  }

  @Override
  protected BaseRepository<FuncReviewCaseRecord, Long> getRepository() {
    return funcReviewCaseRecordRepo;
  }
}
