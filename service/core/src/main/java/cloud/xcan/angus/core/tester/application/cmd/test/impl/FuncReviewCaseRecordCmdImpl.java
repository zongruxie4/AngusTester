package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCaseRecordCmd;
import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecordRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Command implementation for managing functional review case records.
 * </p>
 * <p>
 * Provides methods for adding and deleting review case records.
 * Handles record management for review case tracking and audit purposes.
 * </p>
 * <p>
 * Key features include review record management, batch operations,
 * and record cleanup for review case lifecycle.
 * </p>
 */
@Biz
public class FuncReviewCaseRecordCmdImpl extends CommCmd<FuncReviewCaseRecord, Long>
    implements FuncReviewCaseRecordCmd {

  @Resource
  private FuncReviewCaseRecordRepo funcReviewCaseRecordRepo;

  /**
   * Add a batch of review case records.
   * <p>
   * Batch inserts review case records for persistence.
   */
  @Override
  public void add0(List<FuncReviewCaseRecord> records) {
    batchInsert0(records);
  }

  /**
   * Delete review case records by review case IDs.
   * <p>
   * Removes all records associated with the given review case IDs.
   */
  @Override
  public void deleteByReviewCaseIdIn(Collection<Long> reviewCaseIds) {
    funcReviewCaseRecordRepo.deleteByReviewCaseIdIn(reviewCaseIds);
  }

  /**
   * Get the repository for functional review case records.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncReviewCaseRecord, Long> getRepository() {
    return funcReviewCaseRecordRepo;
  }
}
