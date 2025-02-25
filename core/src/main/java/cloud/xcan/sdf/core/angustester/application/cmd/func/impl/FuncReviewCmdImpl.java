package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_REVIEW;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.REVIEW_CANNOT_END;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginMessage.REVIEW_STATUS_MISMATCH_T;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCmd;
import cloud.xcan.sdf.core.angustester.application.converter.FuncReviewConverter;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewRepo;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncReview review) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(review.getPlanId());
        // Check the plan review enabled
        assertTrue(planDb.getReviewFlag(), "Plan review is not enabled");
        // Check the plan review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), review.getPlanId());
        // Check the review name exists
        funcReviewQuery.checkNameExists(review.getProjectId(), review.getName());
        // Check the owner exists
        userManager.checkAndFind(review.getOwnerId());
        // Check the participants exists
        userManager.checkAndFind(review.getParticipantIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save case review
        review.setProjectId(planDb.getProjectId());
        IdKey<Long, Object> idKey = insert(review);

        // Save review cases
        if (isNotEmpty(review.getCaseIds())) {
          funcReviewCaseCmd.add(review.getId(), review.getCaseIds());
        }

        // Save activity
        activityCmd.add(toActivity(FUNC_REVIEW, review, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

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

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(Long id) {
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
        assertTrue(reviewDb.getStatus().allowStart(), REVIEW_STATUS_MISMATCH_T,
            new Object[]{reviewDb.getStatus(), FuncPlanStatus.IN_PROGRESS});
      }

      @Override
      protected Void process() {
        reviewDb.setStatus(FuncPlanStatus.IN_PROGRESS);
        funcReviewRepo.save(reviewDb);

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, STATUS_UPDATE,
            FuncPlanStatus.IN_PROGRESS));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void end(Long id) {
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

        // Check cases without pending review status
        assertTrue(!funcReviewQuery.hasPendingCaseInReview(id), REVIEW_CANNOT_END);

        // Check the status is allowed
        assertTrue(reviewDb.getStatus().allowEnd(), REVIEW_STATUS_MISMATCH_T,
            new Object[]{reviewDb.getStatus(), FuncPlanStatus.COMPLETED});
      }

      @Override
      protected Void process() {
        reviewDb.setStatus(FuncPlanStatus.COMPLETED);
        funcReviewRepo.save(reviewDb);

        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, STATUS_UPDATE, FuncPlanStatus.COMPLETED));
        return null;
      }
    }.execute();
  }

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reviewReset(Set<Long> ids, boolean reset) {
    new BizTemplate<Void>() {
      List<FuncReview> reviewsDb;

      @Override
      protected void checkParams() {
        // Check the review exists
        reviewsDb = funcReviewQuery.checkAndFind(ids);

        // Check the review permission
        funcPlanAuthQuery.batchCheckPermission(reviewsDb.stream().map(FuncReview::getPlanId)
            .collect(Collectors.toSet()), FuncPlanPermission.RESET_REVIEW_RESULT);
      }

      @Override
      protected Void process() {
        for (Long id : ids) {
          // Query reset review case ids
          Set<Long> caseIds = funcReviewCaseRepo.findCaseIdByReviewId(id);

          if (isNotEmpty(caseIds)) {
            // Reset cases status
            if (reset) {
              funcCaseRepo.updateReviewResultToInitByIds(caseIds);
            } else {
              funcCaseRepo.updateReviewResultToRestartByIds(caseIds);
            }

            // Reset review cases status
            funcReviewCaseRepo.updateReviewResultToInitByReviewIdAndCaseIds(id, caseIds);

            // Reset review status
            reviewsDb.forEach(x -> x.setStatus(FuncPlanStatus.PENDING));
            funcReviewRepo.saveAll(reviewsDb);

            activityCmd.addAll(toActivities(FUNC_REVIEW, reviewsDb, ActivityType.REVIEW_RESET));
          }
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review existed
        reviewDb = funcReviewRepo.findById(id).orElse(null);
        if (isNull(reviewDb)) {
          return;
        }
        // Check the plan review permission
        if (Objects.equals(getUserId(), reviewDb.getCreatedBy())) {
          funcPlanAuthQuery.checkReviewAuth(getUserId(), reviewDb.getPlanId());
        }
      }

      @Override
      protected Void process() {
        if (isNull(reviewDb)) {
          return null;
        }

        // Delete review
        funcReviewRepo.deleteById(reviewDb.getId());

        // NOOP -> Delete review case records ?

        // Add delete review activity
        activityCmd.add(toActivity(FUNC_REVIEW, reviewDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void deleteByPlanId(Collection<Long> planIds) {
    funcReviewRepo.deleteByPlanIdIn(planIds);
    funcReviewCaseRepo.deleteByPlanIdIn(planIds);
  }

  @Override
  public void deleteByCaseId(Collection<Long> caseIds) {
    //funcReviewRepo.deleteByCaseIdIn(caseIds);
    funcReviewCaseRepo.deleteByCaseIdIn(caseIds);
  }

  @Override
  protected BaseRepository<FuncReview, Long> getRepository() {
    return this.funcReviewRepo;
  }
}
