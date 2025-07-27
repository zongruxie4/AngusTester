package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_REVIEW;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.REVIEW_CANNOT_END;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.REVIEW_STATUS_MISMATCH_T;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCmd;
import cloud.xcan.angus.core.tester.application.converter.FuncReviewConverter;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewRepo;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for functional review session management operations.
 * <p>
 * Provides comprehensive CRUD operations for functional review sessions including creation, 
 * modification, deletion, lifecycle management, and case association.
 * <p>
 * Implements business logic validation, permission checks, activity logging, 
 * and transaction management for all review operations.
 * <p>
 * Supports review lifecycle management (start, end, block), case association management,
 * result reset functionality, and comprehensive activity tracking.
 */
@Biz
public class FuncReviewCmdImpl extends CommCmd<FuncReview, Long> implements FuncReviewCmd {

  @Resource
  private FuncReviewRepo funcReviewRepo;
  @Resource
  private FuncReviewQuery funcReviewQuery;
  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncReviewCaseCmd funcReviewCaseCmd;
  @Resource
  private UserManager userManager;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new functional review session to the system.
   * <p>
   * Performs comprehensive validation including plan existence, review enablement, 
   * user permissions, name uniqueness, and user existence checks.
   * <p>
   * Creates review case associations and logs creation activity 
   * for audit trail purposes.
   * <p>
   * Ensures proper setup for review workflow and case tracking.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncReview review) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Ensure the plan exists and review is enabled
        planDb = funcPlanQuery.checkAndFind(review.getPlanId());
        assertTrue(planDb.getReview(), "Plan review is not enabled");
        
        // Check user permission to create reviews for the plan
        funcPlanAuthQuery.checkReviewAuth(getUserId(), review.getPlanId());
        
        // Validate review name is unique within the project
        funcReviewQuery.checkNameExists(review.getProjectId(), review.getName());
        
        // Ensure review owner exists and is valid
        userManager.checkAndFind(review.getOwnerId());
        
        // Ensure all participants exist and are valid
        userManager.checkAndFind(review.getParticipantIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Set project ID from plan and save review
        review.setProjectId(planDb.getProjectId());
        IdKey<Long, Object> idKey = insert(review);

        // Create review case associations if cases specified
        if (isNotEmpty(review.getCaseIds())) {
          funcReviewCaseCmd.add(review.getId(), review.getCaseIds());
        }

        // Log review creation activity for audit
        activityCmd.add(toActivity(FUNC_REVIEW, review, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Update an existing functional review session.
   * <p>
   * Checks existence, name, permission, owner, and participants before updating.
   * <p>
   * Updates review details and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(FuncReview review) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(review.getId());
        // Check the review name exists
        if (isNotEmpty(review.getName()) && !reviewDb.getName().equals(review.getName())) {
          funcReviewQuery.checkNameExists(reviewDb.getProjectId(), review.getName());
        }

        // Check the plan review permission
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), review.getPlanId());
        }

        // Check the owner exists
        if (nonNull(review.getOwnerId())) {
          userManager.checkAndFind(review.getOwnerId());
        }
        // Check the testers exists
        if (isNotEmpty(review.getParticipants())) {
          // Check the participants exists
          userManager.checkAndFind(review.getParticipantIds());
        }
      }

      @Override
      protected Void process() {
        funcReviewRepo.save(copyPropertiesIgnoreNull(review, reviewDb));

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replace (add or update) a functional review session.
   * <p>
   * Adds a new review or updates an existing one, handling participants and logging activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(FuncReview review) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        if (nonNull(review.getId())) {
          // Check the review exists
          reviewDb = funcReviewQuery.checkAndFind(review.getId());

          // Check the review name exists
          if (!reviewDb.getName().equals(review.getName())) {
            funcReviewQuery.checkNameExists(reviewDb.getProjectId(), review.getName());
          }

          // Check the plan review permission
          if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
            funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
          }

          // Check the owner exists
          userManager.checkAndFind(review.getOwnerId());
          // Check the participants exists
          userManager.checkAndFind(review.getParticipantIds());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(review.getId())) {
          return add(review);
        }

        FuncReviewConverter.setReplaceInfo(review, reviewDb);
        funcReviewRepo.save(review);

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(reviewDb.getId()).setKey(reviewDb.getName());
      }
    }.execute();
  }

  /**
   * Starts a functional review session to begin review activities.
   * <p>
   * Validates review existence, user permissions, and review status before transitioning 
   * the review to IN_PROGRESS state.
   * <p>
   * Only reviews in appropriate status (typically PENDING) can be started.
   * <p>
   * Logs status change activity for audit trail purposes.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(Long id) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Ensure the review exists in database
        reviewDb = funcReviewQuery.checkAndFind(id);

        // Check user permission to manage the review (creator has full access)
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
        }

        // Ensure review status allows starting (typically PENDING status)
        assertTrue(reviewDb.getStatus().allowStart(), REVIEW_STATUS_MISMATCH_T,
            new Object[]{reviewDb.getStatus(), FuncPlanStatus.IN_PROGRESS});
      }

      @Override
      protected Void process() {
        // Transition review to IN_PROGRESS status
        reviewDb.setStatus(FuncPlanStatus.IN_PROGRESS);
        funcReviewRepo.save(reviewDb);

        // Log status change activity for audit
        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, STATUS_UPDATE,
            FuncPlanStatus.IN_PROGRESS));
        return null;
      }
    }.execute();
  }

  /**
   * Ends a functional review session to complete review activities.
   * <p>
   * Validates review existence, user permissions, pending cases, and review status 
   * before transitioning the review to COMPLETED state.
   * <p>
   * Only reviews with all cases reviewed can be ended.
   * <p>
   * Logs status change activity for audit trail purposes.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void end(Long id) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Ensure the review exists in database
        reviewDb = funcReviewQuery.checkAndFind(id);

        // Check user permission to manage the review (creator has full access)
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
        }

        // Ensure no cases are pending review (all must be reviewed)
        assertTrue(!funcReviewQuery.hasPendingCaseInReview(id), REVIEW_CANNOT_END);

        // Ensure review status allows ending (typically IN_PROGRESS status)
        assertTrue(reviewDb.getStatus().allowEnd(), REVIEW_STATUS_MISMATCH_T,
            new Object[]{reviewDb.getStatus(), FuncPlanStatus.COMPLETED});
      }

      @Override
      protected Void process() {
        // Transition review to COMPLETED status
        reviewDb.setStatus(FuncPlanStatus.COMPLETED);
        funcReviewRepo.save(reviewDb);

        // Log status change activity for audit
        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, STATUS_UPDATE, FuncPlanStatus.COMPLETED));
        return null;
      }
    }.execute();
  }

  /**
   * Block a functional review session.
   * <p>
   * Checks existence, permission, and status before blocking the review.
   * <p>
   * Logs status update activity.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void block(Long id) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(id);

        // Check the plan review permission
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
        }

        // Check the status is allowed
        assertTrue(reviewDb.getStatus().allowEnd(), REVIEW_STATUS_MISMATCH_T,
            new Object[]{reviewDb.getStatus(), FuncPlanStatus.BLOCKED});
      }

      @Override
      protected Void process() {
        reviewDb.setStatus(FuncPlanStatus.BLOCKED);
        funcReviewRepo.save(reviewDb);

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, STATUS_UPDATE, FuncPlanStatus.BLOCKED));
        return null;
      }
    }.execute();
  }

  /**
   * Clone a functional review session.
   * <p>
   * Checks existence and permission before cloning.
   * <p>
   * Logs clone activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(id);

        // Check the plan review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        FuncReview newReview = FuncReviewConverter.clone(reviewDb);
        funcReviewQuery.setSafeCloneName(newReview);
        IdKey<Long, Object> idKey = insert(newReview, "name");

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, ActivityType.CLONE, reviewDb.getName()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Resets review results for a batch of functional review sessions.
   * <p>
   * Validates review existence and user permissions before resetting review results.
   * <p>
   * Resets case review status, review case status, and review session status.
   * <p>
   * Logs reset activities and restarts reviews if needed.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(Set<Long> ids, boolean reset) {
    new BizTemplate<Void>() {
      List<FuncReview> reviewsDb;

      @Override
      protected void checkParams() {
        // Ensure all reviews exist in database
        reviewsDb = funcReviewQuery.checkAndFind(ids);

        // Check user permission to reset review results
        funcPlanAuthQuery.batchCheckPermission(reviewsDb.stream().map(FuncReview::getPlanId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_REVIEW_RESULT);
      }

      @Override
      protected Void process() {
        for (Long id : ids) {
          // Get all case IDs associated with this review
          Set<Long> caseIds = funcReviewCaseRepo.findCaseIdByReviewId(id);

          if (isNotEmpty(caseIds)) {
            // Reset case review status based on reset flag
            if (reset) {
              // Reset to initial state
              funcCaseRepo.updateReviewResultToInitByIds(caseIds);
            } else {
              // Reset to restart state
              funcCaseRepo.updateReviewResultToRestartByIds(caseIds);
            }

            // Reset review case status to initial state
            funcReviewCaseRepo.updateReviewResultToInitByReviewIdAndCaseIds(id, caseIds);

            // Reset review session status to PENDING
            reviewsDb.forEach(x -> x.setStatus(FuncPlanStatus.PENDING));
            funcReviewRepo.saveAll(reviewsDb);

            // Log review reset activities for audit
            activityCmd.addAll(toActivities(FUNC_REVIEW, reviewsDb, ActivityType.REVIEW_RESET));
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Deletes a functional review session from the system.
   * <p>
   * Validates review existence and user permissions before permanently removing 
   * the review from the database.
   * <p>
   * Logs deletion activity for audit trail purposes.
   * <p>
   * Note: Review case records are not automatically deleted.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check if review exists in database
        reviewDb = funcReviewRepo.findById(id).orElse(null);
        if (isNull(reviewDb)) {
          return;
        }
        
        // Check user permission to delete the review (creator has full access)
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
        }
      }

      @Override
      protected Void process() {
        if (isNull(reviewDb)) {
          return null;
        }

        // Permanently delete the review from database
        funcReviewRepo.deleteById(reviewDb.getId());

        // Note: Review case records are not automatically deleted

        // Log review deletion activity for audit
        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes all review sessions associated with the specified plan IDs.
   * <p>
   * Removes all reviews and their associated review case records 
   * for the given plan IDs.
   * <p>
   * Used for cleanup when plans are deleted.
   */
  @Override
  public void deleteByPlanId(Collection<Long> planIds) {
    // Delete all reviews for the specified plans
    funcReviewRepo.deleteByPlanIdIn(planIds);
    
    // Delete all review case associations for the specified plans
    funcReviewCaseRepo.deleteByPlanIdIn(planIds);
  }

  /**
   * Deletes all review case associations for the specified case IDs.
   * <p>
   * Removes review case records without affecting the review sessions themselves.
   * <p>
   * Used for cleanup when cases are deleted.
   */
  @Override
  public void deleteByCaseId(Collection<Long> caseIds) {
    // Delete all review case associations for the specified cases
    funcReviewCaseRepo.deleteByCaseIdIn(caseIds);
  }

  /**
   * Get the repository for functional review sessions.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncReview, Long> getRepository() {
    return this.funcReviewRepo;
  }
}
