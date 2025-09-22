package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NAME_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_CASE_NOT_COMPLETED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_NOT_STARTED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_NOT_STARTED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_REVIEW_NOT_ENABLED;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.PLAN_REVIEW_NOT_ENABLED_CODE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.count.PlanCaseNum;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanSearchRepo;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import com.google.common.base.Joiner;
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
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncPlanQuery for managing functional test plan queries and operations.
 * <p>
 * This class provides comprehensive functionality for querying, validating, and managing
 * functional test plans. It handles plan retrieval, validation, progress tracking, and
 * various statistical operations including case counting and member management.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Plan detail retrieval with enrichment (case counts, progress, members)</li>
 *   <li>Comprehensive plan validation and consistency checking</li>
 *   <li>Progress calculation and member information management</li>
 *   <li>Authorization and permission integration</li>
 *   <li>Quota management and validation</li>
 *   <li>Review and baseline integration</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and validation checks.
 * <p>
 * Supports both individual plan operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncPlanQueryImpl implements FuncPlanQuery {

  @Resource
  private FuncPlanRepo funcPlanRepo;
  @Resource
  private FuncPlanSearchRepo funcPlanSearchRepo;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private FuncBaselineQuery funcBaselineQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;
  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  /**
   * Retrieves detailed information for a specific functional test plan.
   * <p>
   * Fetches the plan by ID and enriches it with additional information including
   * case counts, progress metrics, member information, and user details.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution with
   * parameter validation and error handling.
   *
   * @param id the plan ID to retrieve details for
   * @return FuncPlan object with complete details and enriched information
   * @throws ResourceNotFound if the plan is not found
   */
  @Override
  public FuncPlan detail(Long id) {
    return new BizTemplate<FuncPlan>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        planDb = checkAndFind(id);
      }

      @Override
      protected FuncPlan process() {
        List<FuncPlan> plans = List.of(planDb);
        Set<Long> ids = Set.of(id);

        // Enrich plan with case counts, progress, and member information
        setCaseNum(plans, ids);
        setProgress(plans, ids);
        setMembers(plans, ids);

        // Set user name and avatar for plan owner
        userManager.setUserNameAndAvatar(List.of(planDb), "ownerId", "ownerName", "ownerAvatar");
        return planDb;
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of functional test plans.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering.
   * Enriches results with case counts, progress, member information, and user details.
   * <p>
   * Includes permission checking and authorization filtering for security.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match full-text search match parameters
   * @return Page of FuncPlan objects with enriched information
   * @throws BizException if permission validation fails
   */
  @Override
  public Page<FuncPlan> list(GenericSpecification<FuncPlan> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncPlan>>() {
      @Override
      protected void checkParams() {
        // Validate project member permissions
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<FuncPlan> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("deleted", false));

        // Note: All project members are visible, no additional authorization filtering needed
        // checkAndSetAuthObjectIdCriteria(criteria); -> All project members are visible

        // Execute search based on search type
        Page<FuncPlan> page = fullTextSearch
            ? funcPlanSearchRepo.find(criteria, pageable, FuncPlan.class, match)
            : funcPlanRepo.findAll(spec, pageable);

        if (page.hasContent()) {
          Set<Long> planIds = page.getContent().stream().map(FuncPlan::getId)
              .collect(Collectors.toSet());

          // Enrich plans with case counts, progress, and member information
          setCaseNum(page.getContent(), planIds);
          setProgress(page.getContent(), planIds);
          setMembers(page.getContent(), planIds);

          // Set user name and avatar for plan owners
          userManager.setUserNameAndAvatar(page.getContent(), "ownerId", "ownerName",
              "ownerAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Retrieves cases that have not been reviewed in a functional test plan.
   * <p>
   * Provides filtered list of cases that are eligible for review, excluding cases
   * that are already in review or have been reviewed. Supports module and review filtering.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to find unreviewed cases for
   * @param moduleId optional module ID to filter cases by module
   * @param reviewId optional review ID to exclude cases from other reviews
   * @return List of FuncCaseInfo objects that are eligible for review
   * @throws BizException if user lacks review permission
   */
  @Override
  public List<FuncCaseInfo> notReviewed(Long planId, @Nullable Long moduleId,
      @Nullable Long reviewId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      @Override
      protected void checkParams() {
        // Validate review permission for the current user
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<Long> excludeCaseIds = new HashSet<>();

        // Exclude cases that are already in review or have been reviewed
        if (nonNull(reviewId)) {
          // Exclude cases pending in other reviews
          Set<Long> otherReviewPendingCaseIds =
              funcReviewCaseRepo.findPendingCaseIdByPlanIdAndReviewIdNot(planId, reviewId);
          excludeCaseIds.addAll(otherReviewPendingCaseIds);

          // Exclude cases already in current review
          Set<Long> currentReviewCaseIds =
              funcReviewCaseRepo.findCaseIdByPlanIdAndReviewId(planId, reviewId);
          excludeCaseIds.addAll(currentReviewCaseIds);
        } else {
          // Exclude all cases currently in review
          Set<Long> pendingCaseIdsInReview = funcReviewCaseRepo.findPendingCaseIdByPlanId(planId);
          excludeCaseIds.addAll(pendingCaseIdsInReview);
        }

        // Build search criteria for unreviewed cases
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("planId", planId));
        filters.add(SearchCriteria.notEqual("reviewStatus", ReviewStatus.PASSED.getValue()));

        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }
        if (isNotEmpty(excludeCaseIds)) {
          filters.add(SearchCriteria.notIn("id", excludeCaseIds));
        }

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(filters);
        // Note: Tester name and avatar not set as they are not used in this context
        // userManager.setUserNameAndAvatar(caseInfos, "testerId", "testerName", "testerAvatar");
        return caseInfos;
      }
    }.execute();
  }

  /**
   * Retrieves cases that have not been established in a baseline.
   * <p>
   * Provides filtered list of cases that are eligible for baseline establishment,
   * excluding cases that are already included in the specified baseline.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to find cases for
   * @param moduleId optional module ID to filter cases by module
   * @param baselineId optional baseline ID to exclude cases already in baseline
   * @return List of FuncCaseInfo objects eligible for baseline establishment
   * @throws BizException if user lacks establish baseline permission
   */
  @Override
  public List<FuncCaseInfo> notEstablishedBaseline(Long planId, @Nullable Long moduleId,
      @Nullable Long baselineId) {
    return new BizTemplate<List<FuncCaseInfo>>() {
      FuncBaseline funcBaselineDb;

      @Override
      protected void checkParams() {
        // Validate baseline exists when creating a new baseline
        if (nonNull(baselineId)) {
          funcBaselineDb = funcBaselineQuery.checkAndFind(baselineId);
        }
        // Validate establish baseline permission for the current user
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), planId);
      }

      @Override
      protected List<FuncCaseInfo> process() {
        Set<SearchCriteria> filters = new HashSet<>();
        filters.add(SearchCriteria.equal("planId", planId));

        if (nonNull(moduleId)) {
          filters.add(SearchCriteria.equal("moduleId", moduleId));
        }

        // Exclude cases already in the specified baseline
        if (nonNull(funcBaselineDb) && isNotEmpty(funcBaselineDb.getCaseIds())) {
          filters.add(SearchCriteria.notIn("id", funcBaselineDb.getCaseIds()));
        }

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(filters);
        // Note: Tester name and avatar not set as they are not used in this context
        // userManager.setUserNameAndAvatar(caseInfos, "testerId", "testerName", "testerAvatar");
        return caseInfos;
      }
    }.execute();
  }

  /**
   * Finds the least recent plan by project ID.
   * <p>
   * Retrieves the plan with the earliest creation date within the specified project.
   *
   * @param projectId the project ID to search within
   * @return FuncPlan object, or null if no plans exist
   */
  @Override
  public FuncPlan findLeastByProjectId(Long projectId) {
    return funcPlanRepo.findLeastByProjectId(projectId);
  }

  /**
   * Checks if authorization control is enabled for a functional test plan.
   * <p>
   * Validates whether the plan has authorization control enabled.
   *
   * @param id the plan ID to check
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public boolean isAuthCtrl(Long id) {
    FuncPlan plan = funcPlanRepo.findById(id).orElse(null);
    return nonNull(plan) && plan.getAuth();
  }

  /**
   * Finds a plan by ID with validation.
   * <p>
   * Retrieves a plan and throws ResourceNotFound if not found.
   *
   * @param id the plan ID
   * @return FuncPlan object
   * @throws ResourceNotFound if plan is not found
   */
  @Override
  public FuncPlan checkAndFind(Long id) {
    return funcPlanRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncPlan"));
  }

  /**
   * Finds multiple plans by IDs with validation.
   * <p>
   * Validates that all requested plans exist and returns them.
   * <p>
   * Optimized validation to reduce duplicate checks and improve performance.
   *
   * @param ids collection of plan IDs
   * @return List of FuncPlan objects
   * @throws ResourceNotFound if any plan is not found
   */
  @Override
  public List<FuncPlan> checkAndFind(Collection<Long> ids) {
    List<FuncPlan> plans = funcPlanRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(plans), ids.iterator().next(), "FuncPlan");

    // Validate that all requested plans were found
    if (ids.size() != plans.size()) {
      for (FuncPlan plan : plans) {
        assertResourceNotFound(ids.contains(plan.getId()), plan.getId(), "FuncPlan");
      }
    }
    return plans;
  }

  /**
   * Validates that a plan name is unique within a project.
   * <p>
   * Ensures the plan name does not conflict with existing plans in the same project.
   *
   * @param projectId the project ID
   * @param name the plan name to validate
   * @throws ResourceExisted if the name already exists
   */
  @Override
  public void checkNameExists(Long projectId, String name) {
    long count = funcPlanRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(FUNC_PLAN_NAME_REPEATED_T, new Object[]{name});
    }
  }

  /**
   * Validates that a plan has started.
   * <p>
   * Ensures the plan is in an active state that allows operations.
   *
   * @param plan the plan to validate
   * @throws BizException if plan has not started
   */
  @Override
  public void checkHasStarted(FuncPlan plan) {
    if (plan.getStatus().isNotInProcess()) {
      throw BizException.of(PLAN_NOT_STARTED_CODE, PLAN_NOT_STARTED);
    }
  }

  /**
   * Validates that review functionality is enabled for a plan.
   * <p>
   * Ensures the plan has review functionality enabled before allowing review operations.
   *
   * @param plan the plan to validate
   * @throws BizException if review is not enabled
   */
  @Override
  public void checkReviewEnabled(FuncPlan plan) {
    if (isNull(plan.getReview()) || !plan.getReview()) {
      throw BizException.of(PLAN_REVIEW_NOT_ENABLED_CODE, PLAN_REVIEW_NOT_ENABLED);
    }
  }

  /**
   * Validates that all cases in a plan have been completed.
   * <p>
   * Ensures all cases in the plan have passed before allowing plan completion.
   *
   * @param id the plan ID to validate
   * @throws ProtocolException if any cases are not completed
   */
  @Override
  public void checkPlanCaseCompleted(Long id) {
    long num = funcCaseRepo.countNotPassedByPlanId(id);
    if (num > 0) {
      throw ProtocolException.of(PLAN_CASE_NOT_COMPLETED);
    }
  }

  /**
   * Validates that case IDs belong to the specified plan.
   * <p>
   * Ensures consistency between case IDs and plan ownership.
   *
   * @param planId the plan ID
   * @param caseIds set of case IDs to validate
   * @throws ProtocolException if any case does not belong to the plan
   */
  @Override
  public void checkConsistent(Long planId, HashSet<Long> caseIds) {
    if (isNotEmpty(caseIds)) {
      Set<Long> planCaseIds = funcCaseRepo.findAll0IdByPlanIdAndIdIn(planId, caseIds);
      if (caseIds.size() != planCaseIds.size()) {
        Set<Long> noExistCaseIds = new HashSet<>(caseIds);
        noExistCaseIds.removeAll(planCaseIds);
        ProtocolAssert.assertTrue(noExistCaseIds.isEmpty(),
            String.format("The use case [%s] does not belong to the plan [%s]",
                Joiner.on(", ").join(noExistCaseIds), planId));
      }
    }
  }

  /**
   * Validates plan quota limits.
   * <p>
   * Checks against tenant quota limits to ensure resource constraints are respected.
   *
   * @throws BizException if quota limit would be exceeded
   */
  @Override
  public void checkQuota() {
    long count = funcPlanRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterFuncPlan,
        null, count + 1);
  }

  /**
   * Sets case count information for multiple plans.
   * <p>
   * Enriches plans with total case counts and valid case counts for statistical analysis.
   * <p>
   * Optimized for bulk operations with efficient database queries.
   *
   * @param plans list of plans to update with case counts
   * @param planIds set of plan IDs for efficient database querying
   */
  @Override
  public void setCaseNum(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      // Set total case counts
      Map<Long, Long> caseNumsMap = funcCaseRepo.findPlanCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));
      for (FuncPlan plan : plans) {
        plan.setCaseNum(caseNumsMap.containsKey(plan.getId())
            ? caseNumsMap.get(plan.getId()) : 0);
      }

      // Set valid case counts (excluding canceled cases)
      Map<Long, Long> validCaseNumsMap = funcCaseRepo.findValidPlanCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));
      for (FuncPlan plan : plans) {
        plan.setValidCaseNum(validCaseNumsMap.containsKey(plan.getId())
            ? validCaseNumsMap.get(plan.getId()) : 0);
      }
    }
  }

  /**
   * Sets progress information for multiple plans.
   * <p>
   * Calculates and sets progress metrics including completion rates and passed case counts.
   * <p>
   * Note: Must be executed after setCaseNum() to ensure valid case counts are available.
   *
   * @param plans list of plans to update with progress information
   * @param planIds set of plan IDs for efficient database querying
   */
  @Override
  public void setProgress(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      // Retrieve passed case counts for all plans
      Map<Long, Long> planPassedNumsMap = funcCaseRepo.findPlanPassedCaseNumsGroupByPlanId(planIds)
          .stream().collect(toMap(PlanCaseNum::getPlanId, PlanCaseNum::getCaseNum));

      for (FuncPlan plan : plans) {
        if (planPassedNumsMap.containsKey(plan.getId())) {
          // Calculate progress with completion rate
          long passedCount = planPassedNumsMap.get(plan.getId());
          plan.setProgress(new Progress().setTotal(plan.getValidCaseNum())
              .setCompleted(passedCount)
              .setCompletedRate(plan.getValidCaseNum() > 0 ?
                  BigDecimal.valueOf(passedCount)
                      .divide(BigDecimal.valueOf(plan.getValidCaseNum()), 4,
                          RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) // X 100%
                      .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO));
        } else {
          // Set zero progress for plans with no passed cases
          plan.setProgress(new Progress().setTotal(plan.getValidCaseNum())
              .setCompleted(0).setCompletedRate(BigDecimal.ZERO));
        }
      }
    }
  }

  /**
   * Sets member information for multiple plans.
   * <p>
   * Enriches plans with user information for assigned testers and team members.
   * <p>
   * Optimized for bulk operations with efficient user data retrieval.
   *
   * @param plans list of plans to update with member information
   * @param planIds set of plan IDs for efficient processing
   */
  @Override
  public void setMembers(List<FuncPlan> plans, Set<Long> planIds) {
    if (isNotEmpty(plans)) {
      // Collect all tester IDs from all plans
      Set<Long> testerIds = new HashSet<>();
      for (FuncPlan plan : plans) {
        if (isNotEmpty(plan.getTesterResponsibilities())) {
          testerIds.addAll(plan.getTesterResponsibilities().keySet());
        }
      }

      if (isEmpty(testerIds)) {
        return;
      }

      // Retrieve user information for all testers
      Map<Long, UserInfo> userMap = userManager.getUserBaseMap(testerIds).entrySet().stream()
          .collect(toMap(Entry::getKey, x -> new UserInfo().setId(x.getValue().getId())
              .setFullName(x.getValue().getFullName()).setAvatar(x.getValue().getAvatar())));

      // Set member information for each plan
      for (FuncPlan plan : plans) {
        if (isNotEmpty(plan.getTesterResponsibilities())) {
          List<UserInfo> members = new ArrayList<>();
          for (Long userId : plan.getTesterResponsibilities().keySet()) {
            members.add(userMap.getOrDefault(userId, new UserInfo().setId(userId)));
          }
          plan.setMembers(members);
        }
      }
    }
  }

  /**
   * Sets a safe clone name for a plan.
   * <p>
   * Generates a unique name for plan cloning operations with suffix and length validation.
   * <p>
   * Ensures the generated name is unique within the project and respects length constraints.
   * Optimized to minimize database calls for name validation.
   *
   * @param plan the plan to set clone name for
   */
  @Override
  public void setSafeCloneName(FuncPlan plan) {
    String saltName = randomAlphanumeric(3);
    String clonedName = funcPlanRepo.existsByProjectIdAndName(
        plan.getProjectId(), plan.getName() + "-Copy")
        ? plan.getName() + "-Copy." + saltName : plan.getName() + "-Copy";

    // Handle length constraints efficiently
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH_X2 - 3) + saltName : clonedName;
    plan.setName(clonedName);
  }

  /**
   * Sets authorization conditions for query filtering.
   * <p>
   * Configures search criteria to include authorization object ID filtering when
   * the user is not an administrator or when admin override is not enabled.
   * <p>
   * This method ensures proper authorization filtering for plan queries.
   *
   * @param criteria the search criteria to modify
   * @return false (legacy return value, not used)
   */
  @Override
  public boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria) {
    // Extract admin parameter from criteria
    SearchCriteria adminCriteria = findFirstAndRemove(criteria, "admin");
    boolean admin = false;
    if (Objects.nonNull(adminCriteria)) {
      admin = Boolean.parseBoolean(adminCriteria.getValue().toString().replaceAll("\"", ""));
    }

    // Add authorization filtering unless admin override is enabled and user is admin
    if (!admin || !funcPlanAuthQuery.isAdminUser()) {
      criteria.add(SearchCriteria.in("authObjectId", userManager.getValidOrgAndUserIds()));
    }
    return false;
  }

  /**
   * Gets plan creation summaries for resource creation analysis.
   * <p>
   * Retrieves plan creation statistics filtered by creator organization and date range.
   * <p>
   * Used for analyzing resource creation patterns and trends.
   *
   * @param projectId the project ID
   * @param planId the plan ID
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param creatorOrgType the creator organization type
   * @param creatorOrgId the creator organization ID
   * @return List of FuncPlan objects
   */
  @Override
  public List<FuncPlan> getPlanCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getCommonCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);
    return funcPlanRepo.findAllByFilters(allFilters);
  }
}
