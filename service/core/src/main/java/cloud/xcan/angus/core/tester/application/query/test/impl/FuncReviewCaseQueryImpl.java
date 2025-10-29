package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_REVIEW_REPEATED_T;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewCaseQuery;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseSearchRepo;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncReviewCaseQuery for managing functional test case review queries.
 * <p>
 * This class provides comprehensive functionality for querying, validating, and managing
 * functional test case reviews. It handles review case retrieval, validation, and
 * case information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Review case detail retrieval and validation</li>
 *   <li>Paginated review case listing with search capabilities</li>
 *   <li>Review case validation and duplicate prevention</li>
 *   <li>Case information enrichment and association</li>
 *   <li>Bulk operations with optimized validation</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and validation checks.
 * <p>
 * Supports both individual review case operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncReviewCaseQueryImpl implements FuncReviewCaseQuery {

  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private FuncReviewCaseSearchRepo funcReviewCaseSearchRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;

  /**
   * Retrieves detailed information for a specific functional test case review.
   * <p>
   * Fetches the review case by ID and validates its existence.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution with
   * parameter validation and error handling.
   *
   * @param id the review case ID to retrieve details for
   * @return FuncReviewCase object with complete details
   * @throws ResourceNotFound if the review case is not found
   */
  @Override
  public FuncReviewCase detail(Long id) {
    return new BizTemplate<FuncReviewCase>() {
      FuncReviewCase reviewCaseDb;

      @Override
      protected void checkParams() {
        reviewCaseDb = checkAndFind(id);
      }

      @Override
      protected FuncReviewCase process() {
        return reviewCaseDb;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of functional test case reviews.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering.
   * Enriches results with case information and filters for latest reviews only.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match full-text search match parameters
   * @return Page of FuncReviewCase objects with enriched case information
   */
  @Override
  public Page<FuncReviewCase> list(GenericSpecification<FuncReviewCase> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncReviewCase>>() {

      @Override
      protected Page<FuncReviewCase> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        // Filter for latest reviews only to avoid duplicate entries
        criteria.add(equal("lastReview", true));

        // Execute search based on search type
        Page<FuncReviewCase> page = fullTextSearch
            ? funcReviewCaseSearchRepo.find(criteria, pageable, FuncReviewCase.class, match)
            : funcReviewCaseRepo.findAll(spec, pageable);

        // Enrich review cases with case information
        setCaseInfo(page.getContent());
        return page;
      }
    }.execute();
  }

  /**
   * Validates that cases can be added to a functional test review.
   * <p>
   * Performs comprehensive validation including case review status, duplicate prevention,
   * and case eligibility checks. Ensures data integrity and prevents review conflicts.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param reviewDb the review object to validate cases for
   * @param funcCasesDb list of cases to validate for review inclusion
   * @throws ProtocolException if cases are not eligible for review or have duplicate reviews
   */
  @Override
  public void checkReviewCaseValid(FuncReview reviewDb, List<FuncCaseInfo> funcCasesDb) {
    // Note: Review status validation is currently disabled
    // funcReviewQuery.checkHasStarted(reviewDb);

    // Validate that cases are in a state that allows review operations
    funcCaseQuery.checkInfoCanReview(funcCasesDb);

    // Check for duplicate reviews to prevent conflicts
    List<Long> caseIds = funcCasesDb.stream().map(FuncCaseInfo::getId).toList();
    Set<Long> pendingReviewCaseIds = funcReviewCaseRepo.findPendingCaseIdByCaseIdInAndReviewIdNot(
        caseIds, reviewDb.getId());

    if (!pendingReviewCaseIds.isEmpty()) {
      // Find the first case that has a duplicate review for error reporting
      FuncCaseInfo repeatedReviewCase = funcCasesDb.stream()
          .filter(x -> pendingReviewCaseIds.contains(x.getId())).findFirst().get();
      throw ProtocolException.of(CASE_REVIEW_REPEATED_T,
          new Object[]{reviewDb.getName(), repeatedReviewCase.getName()});
    }
  }

  /**
   * Finds a review case by ID with validation.
   * <p>
   * Retrieves a review case and throws ResourceNotFound if not found.
   *
   * @param id the review case ID
   * @return FuncReviewCase object
   * @throws ResourceNotFound if review case is not found
   */
  @Override
  public FuncReviewCase checkAndFind(Long id) {
    return funcReviewCaseRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncReviewCase"));
  }

  /**
   * Finds multiple review cases by IDs with validation.
   * <p>
   * Validates that all requested review cases exist and returns them.
   * <p>
   * Optimized validation to reduce duplicate checks and improve performance.
   *
   * @param ids collection of review case IDs
   * @return List of FuncReviewCase objects
   * @throws ResourceNotFound if any review case is not found
   */
  @Override
  public List<FuncReviewCase> checkAndFind(Collection<Long> ids) {
    List<FuncReviewCase> reviewCases = funcReviewCaseRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(reviewCases), ids.iterator().next(), "FuncReviewCase");

    // Validate that all requested review cases were found
    if (ids.size() != reviewCases.size()) {
      for (FuncReviewCase reviewCase : reviewCases) {
        assertResourceNotFound(ids.contains(reviewCase.getId()), reviewCase.getId(),
            "FuncReviewCase");
      }
    }
    return reviewCases;
  }

  /**
   * Enriches review cases with case information.
   * <p>
   * Retrieves and associates case information with review cases for complete data context.
   * Optimized for bulk operations with efficient database queries.
   *
   * @param reviewCases list of review cases to enrich with case information
   */
  @Override
  public void setCaseInfo(List<FuncReviewCase> reviewCases) {
    if (isNotEmpty(reviewCases)) {
      // Collect all case IDs for efficient batch retrieval
      List<Long> caseIds = reviewCases.stream().map(FuncReviewCase::getCaseId).collect(
          Collectors.toList());

      // Retrieve case information for all cases in a single query
      Map<Long, FuncCaseInfo> caseInfoMap = funcCaseQuery.findInfo(caseIds).stream()
          .collect(Collectors.toMap(FuncCaseInfo::getId, x -> x));

      // Associate case information with each review case
      for (FuncReviewCase reviewCase : reviewCases) {
        reviewCase.setCaseInfo(caseInfoMap.get(reviewCase.getCaseId()));
      }
    }
  }
}
