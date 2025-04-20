package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCaseRecordCmd;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecordRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

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
