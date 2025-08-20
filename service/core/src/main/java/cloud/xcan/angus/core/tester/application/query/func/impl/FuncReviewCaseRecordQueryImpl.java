package cloud.xcan.angus.core.tester.application.query.func.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewCaseRecordQuery;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.domain.func.review.record.FuncReviewCaseRecordRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * Implementation of FuncReviewCaseRecordQuery for managing functional test case review records.
 * <p>
 * This class provides functionality for querying and managing review case records, which track
 * the history and audit trail of case review operations. It handles record retrieval and
 * user information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Review case record retrieval and validation</li>
 *   <li>User information enrichment for audit trails</li>
 *   <li>Case existence validation</li>
 *   <li>Historical review data access</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for user data enrichment.
 * <p>
 * Supports individual case review history operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncReviewCaseRecordQueryImpl implements FuncReviewCaseRecordQuery {

  @Resource
  private FuncReviewCaseRecordRepo funcReviewCaseRecordRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves review case records for a specific functional test case.
   * <p>
   * Provides comprehensive review history for a case including all review operations,
   * timestamps, and user information. Enriches results with user details for audit purposes.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param caseId the case ID to retrieve review records for
   * @return List of FuncReviewCaseRecord objects with enriched user information
   * @throws ResourceNotFound if the case is not found
   */
  @Override
  public List<FuncReviewCaseRecord> caseReview(Long caseId) {
    return new BizTemplate<List<FuncReviewCaseRecord>>() {
      @Override
      protected void checkParams() {
        // Validate that the case exists before retrieving review records
        funcCaseQuery.checkAndFind(caseId);
      }

      @Override
      protected List<FuncReviewCaseRecord> process() {
        // Retrieve all review records for the specified case
        List<FuncReviewCaseRecord> reviewCases = funcReviewCaseRecordRepo.findByCaseId(caseId);

        // Enrich review records with user information for audit trail
        userManager.setUserNameAndAvatar(reviewCases, "createdBy");
        return reviewCases;
      }
    }.execute();
  }

}
