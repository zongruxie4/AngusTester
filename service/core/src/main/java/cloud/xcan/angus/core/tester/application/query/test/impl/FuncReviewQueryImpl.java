package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_REVIEW_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.REVIEW_NOT_STARTED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.REVIEW_NOT_STARTED_CODE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewRepo;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewSearchRepo;
import cloud.xcan.angus.core.tester.domain.test.review.ReviewCaseNum;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncReviewQuery for managing functional test review queries and operations.
 * <p>
 * This class provides comprehensive functionality for querying, validating, and managing
 * functional test reviews. It handles review retrieval, validation, progress tracking,
 * participant management, and various statistical operations.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Review detail retrieval with enrichment (case counts, progress, participants)</li>
 *   <li>Paginated review listing with search capabilities</li>
 *   <li>Review validation and status checking</li>
 *   <li>Progress calculation and participant information management</li>
 *   <li>Name conflict prevention and safe cloning</li>
 *   <li>Statistical summaries and filtering</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and data enrichment.
 * <p>
 * Supports both individual review operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncReviewQueryImpl implements FuncReviewQuery {

  @Resource
  private FuncReviewRepo funcReviewRepo;
  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private FuncReviewSearchRepo funcReviewSearchRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed information for a specific functional test review.
   * <p>
   * Fetches the review by ID and enriches it with case counts, progress information,
   * participant details, and user information.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution with
   * parameter validation and comprehensive data enrichment.
   *
   * @param id the review ID to retrieve details for
   * @return FuncReview object with complete enriched details
   * @throws ResourceNotFound if the review is not found
   */
  @Override
  public FuncReview detail(Long id) {
    return new BizTemplate<FuncReview>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        reviewDb = checkAndFind(id);
      }

      @Override
      protected FuncReview process() {
        // Prepare single review for enrichment operations
        List<FuncReview> reviews = List.of(reviewDb);
        Set<Long> ids = Set.of(id);

        // Enrich review with comprehensive information
        setCaseNum(reviews, ids);
        setProgress(reviews, ids);
        setParticipants(reviews, ids);

        // Enrich user information for the review owner
        userManager.setUserNameAndAvatar(List.of(reviewDb), "ownerId", "ownerName", "ownerAvatar");
        return reviewDb;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of functional test reviews with search capabilities.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering.
   * Enriches results with case counts, progress, participants, and user information.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match full-text search match parameters
   * @return Page of FuncReview objects with enriched information
   */
  @Override
  public Page<FuncReview> find(GenericSpecification<FuncReview> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncReview>>() {
      @Override
      protected void checkParams() {
        // Validate project membership permissions before querying
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<FuncReview> process() {
        // Execute search based on search type
        Page<FuncReview> page = fullTextSearch
            ? funcReviewSearchRepo.find(spec.getCriteria(), pageable, FuncReview.class, match)
            : funcReviewRepo.findAll(spec, pageable);

        // Enrich reviews with comprehensive information if results exist
        if (page.hasContent()) {
          Set<Long> reviewIds = page.getContent().stream().map(FuncReview::getId)
              .collect(Collectors.toSet());

          // Enrich reviews with case counts, progress, and participant information
          setCaseNum(page.getContent(), reviewIds);
          setProgress(page.getContent(), reviewIds);
          setParticipants(page.getContent(), reviewIds);

          // Enrich user information for review owners
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Finds reviews by plan IDs for bulk operations.
   * <p>
   * Retrieves all reviews associated with the specified plan IDs.
   *
   * @param planIds set of plan IDs to find reviews for
   * @return List of FuncReview objects associated with the plans
   */
  @Override
  public List<FuncReview> findByPlanIds(HashSet<Long> planIds) {
    return funcReviewRepo.findByPlanIdIn(planIds);
  }

  /**
   * Finds a review by ID with validation.
   * <p>
   * Retrieves a review and throws ResourceNotFound if not found.
   *
   * @param id the review ID
   * @return FuncReview object
   * @throws ResourceNotFound if review is not found
   */
  @Override
  public FuncReview checkAndFind(Long id) {
    return funcReviewRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncReview"));
  }

  /**
   * Finds multiple reviews by IDs with validation.
   * <p>
   * Validates that all requested reviews exist and returns them.
   * <p>
   * Optimized validation to reduce duplicate checks and improve performance.
   *
   * @param ids collection of review IDs
   * @return List of FuncReview objects
   * @throws ResourceNotFound if any review is not found
   */
  @Override
  public List<FuncReview> checkAndFind(Collection<Long> ids) {
    List<FuncReview> reviews = funcReviewRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(reviews), ids.iterator().next(), "FuncReview");

    // Validate that all requested reviews were found
    if (ids.size() != reviews.size()) {
      for (FuncReview review : reviews) {
        assertResourceNotFound(ids.contains(review.getId()), review.getId(), "FuncReview");
      }
    }
    return reviews;
  }

  /**
   * Validates that a review has started and is in progress.
   * <p>
   * Checks the review status to ensure it is in a state that allows operations.
   *
   * @param reviewDb the review to validate
   * @throws BizException if the review has not started or is not in progress
   */
  @Override
  public void checkHasStarted(FuncReview reviewDb) {
    if (reviewDb.getStatus().isNotInProcess()) {
      throw BizException.of(REVIEW_NOT_STARTED_CODE, REVIEW_NOT_STARTED);
    }
  }

  /**
   * Validates that a review name does not already exist in the project.
   * <p>
   * Prevents duplicate review names within the same project.
   *
   * @param projectId the project ID to check for name conflicts
   * @param name the review name to validate
   * @throws ResourceExisted if a review with the same name already exists
   */
  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = funcReviewRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(FUNC_REVIEW_NAME_REPEATED_T, new Object[]{name});
    }
  }

  /**
   * Checks if a review has pending cases.
   * <p>
   * Determines whether there are cases in the review that are still pending review.
   *
   * @param reviewId the review ID to check
   * @return true if the review has pending cases, false otherwise
   */
  @Override
  public boolean hasPendingCaseInReview(Long reviewId) {
    return !funcReviewCaseRepo.findPendingCaseIdByReviewId(reviewId).isEmpty();
  }

  /**
   * Sets case count information for multiple reviews.
   * <p>
   * Calculates and sets the total number of cases for each review.
   * <p>
   * Optimized for bulk operations with efficient database queries.
   *
   * @param reviews list of reviews to update with case counts
   * @param reviewIds set of review IDs for efficient database querying
   */
  @Override
  public void setCaseNum(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      // Retrieve case counts for all reviews in a single query
      Map<Long, Long> caseNumsMap = funcReviewCaseRepo.findReviewCaseNumsGroupByReviewId(reviewIds)
          .stream().collect(toMap(ReviewCaseNum::getReviewId, ReviewCaseNum::getCaseNum));

      // Set case counts for each review
      for (FuncReview review : reviews) {
        review.setCaseNum(caseNumsMap.containsKey(review.getId())
            ? caseNumsMap.get(review.getId()) : 0);
      }
    }
  }

  /**
   * Sets progress information for multiple reviews.
   * <p>
   * Calculates and sets progress metrics including completion rates and passed case counts.
   * <p>
   * Note: Must be executed after setCaseNum() to ensure valid case counts are available.
   *
   * @param reviews list of reviews to update with progress information
   * @param reviewIds set of review IDs for efficient database querying
   */
  @Override
  public void setProgress(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      // Retrieve passed case counts for all reviews
      Map<Long, Long> reviewPassedNumsMap =
          funcReviewCaseRepo.findReviewPassedCaseNumsGroupByReviewId(reviewIds)
              .stream().collect(toMap(ReviewCaseNum::getReviewId, ReviewCaseNum::getCaseNum));

      for (FuncReview review : reviews) {
        if (reviewPassedNumsMap.containsKey(review.getId())) {
          // Calculate progress with completion rate
          long passedCount = reviewPassedNumsMap.get(review.getId());
          review.setProgress(new Progress().setTotal(review.getCaseNum())
              .setCompleted(passedCount)
              .setCompletedRate(review.getCaseNum() > 0 ?
                  BigDecimal.valueOf(passedCount)
                      .divide(BigDecimal.valueOf(review.getCaseNum()), 4,
                          RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
                      .setScale(2, RoundingMode.HALF_UP)
                  : BigDecimal.ZERO));
        } else {
          // Set zero progress for reviews with no passed cases
          review.setProgress(new Progress().setTotal(review.getCaseNum())
              .setCompleted(0)
              .setCompletedRate(BigDecimal.ZERO));
        }
      }
    }
  }

  /**
   * Sets participant information for multiple reviews.
   * <p>
   * Retrieves and associates user information with review participants for complete data context.
   * Optimized for bulk operations with efficient database queries.
   *
   * @param reviews list of reviews to update with participant information
   * @param reviewIds set of review IDs for efficient database querying
   */
  @Override
  public void setParticipants(List<FuncReview> reviews, Set<Long> reviewIds) {
    if (isNotEmpty(reviews)) {
      // Collect all participant IDs across all reviews
      Set<Long> participantIds = new HashSet<>();
      for (FuncReview review : reviews) {
        if (isNotEmpty(review.getParticipantIds())) {
          participantIds.addAll(review.getParticipantIds());
        }
      }

      // Return early if no participants found
      if (isEmpty(participantIds)) {
        return;
      }

      // Retrieve user information for all participants in a single query
      Map<Long, UserInfo> userMap = userManager.getUserBaseMap(participantIds).entrySet().stream()
          .collect(toMap(Entry::getKey, x -> new UserInfo().setId(x.getValue().getId())
              .setFullName(x.getValue().getFullName()).setAvatar(x.getValue().getAvatar())));

      // Associate participant information with each review
      for (FuncReview review : reviews) {
        if (isNotEmpty(review.getParticipantIds())) {
          List<UserInfo> participants = new ArrayList<>();
          for (Long userId : review.getParticipantIds()) {
            // Create user info object with available data or fallback to basic info
            participants.add(userMap.getOrDefault(userId, new UserInfo().setId(userId)));
          }
          review.setParticipants(participants);
        }
      }
    }
  }

  /**
   * Sets a safe clone name for a review to prevent conflicts.
   * <p>
   * Generates a unique name for cloned reviews by appending "-Copy" and handling
   * name conflicts with random suffixes. Ensures name length compliance.
   *
   * @param review the review to set a safe clone name for
   */
  @Override
  public void setSafeCloneName(FuncReview review) {
    String saltName = randomAlphanumeric(3);
    String clonedName = funcReviewRepo.existsByProjectIdAndName(
        review.getProjectId(), review.getName() + "-Copy")
        ? review.getName() + "-Copy." + saltName : review.getName() + "-Copy";

    // Ensure name length compliance with business rules
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    review.setName(clonedName);
  }

  /**
   * Retrieves review creation summaries with comprehensive filtering.
   * <p>
   * Provides statistical overview of review creation patterns with support for
   * project, plan, date range, and creator organization filtering.
   * <p>
   * Optimized for bulk operations and statistical analysis.
   *
   * @param projectId optional project ID for filtering
   * @param planId optional plan ID for filtering
   * @param createdDateStart optional start date for filtering
   * @param createdDateEnd optional end date for filtering
   * @param creatorOrgType optional creator organization type for filtering
   * @param creatorOrgId optional creator organization ID for filtering
   * @return List of FuncReview objects matching the filter criteria
   */
  @Override
  public List<FuncReview> getReviewCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Determine creator IDs based on organization type and ID
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);

    // Build comprehensive filter criteria
    Set<SearchCriteria> allFilters = getCommonCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);

    return funcReviewRepo.findAllByFilters(allFilters);
  }

}
