package cloud.xcan.angus.core.tester.application.cmd.test.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.FuncReviewCaseConverter.setReviewInfoAndStatus;
import static cloud.xcan.angus.core.tester.application.converter.FuncReviewCaseConverter.toReviewCase;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getCaseDetailSummary;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCaseRecordCmd;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewCaseQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewRepo;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.review.record.FuncReviewCaseRecord;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Command implementation for managing functional review cases.
 * </p>
 * <p>
 * Provides methods for adding, reviewing, resetting, and deleting review cases.
 * Handles permission checks, review workflow management, and activity logging.
 * </p>
 * <p>
 * Key features include review case management, review workflow processing,
 * record tracking, and comprehensive activity logging for audit purposes.
 * </p>
 */
@Biz
public class FuncReviewCaseCmdImpl extends CommCmd<FuncReviewCase, Long>
    implements FuncReviewCaseCmd {

  @Resource
  private FuncReviewCaseRepo funcReviewCaseRepo;
  @Resource
  private FuncReviewCaseQuery funcReviewCaseQuery;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private FuncReviewQuery funcReviewQuery;
  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncReviewRepo funcReviewRepo;
  @Resource
  private FuncCaseCmd funcCaseCmd;
  @Resource
  private FuncReviewCaseRecordCmd funcReviewCaseRecordCmd;
  @Resource
  private ActivityCmd activityCmd;
  @Resource
  private JoinSupplier joinSupplier;

  /**
   * Add review cases to a review session.
   * <p>
   * Checks review and case existence, validity, and logs add activities.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long reviewId, Set<Long> caseIds) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      FuncReview funcReviewDb;
      List<FuncCaseInfo> funcCasesDb;

      @Override
      protected void checkParams() {
        // Check the review exists
        funcReviewDb = funcReviewQuery.checkAndFind(reviewId);
        // Check the cases exists
        funcCasesDb = funcCaseQuery.checkAndFindInfo(caseIds);
        // Check the cases is valid
        funcReviewCaseQuery.checkReviewCaseValid(funcReviewDb, funcCasesDb);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Save review cases
        Map<Long, FuncCaseInfo> caseInfoMap = funcCasesDb.stream()
            .collect(Collectors.toMap(FuncCaseInfo::getId, x -> x));
        List<FuncReviewCase> reviewCases = caseIds.stream()
            .map(x -> toReviewCase(funcReviewDb, caseInfoMap.get(x)))
            .toList();
        List<IdKey<Long, Object>> idKeys = batchInsert(reviewCases);

        // Save activities
        activityCmd.addAll(toActivities(FUNC_CASE, funcCasesDb, ActivityType.FUNC_REVIEW_ADD,
            funcReviewDb.getName()));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Review a batch of review cases.
   * <p>
   * Checks existence, consistency, permission, and updates review status and records.
   * <p>
   * Logs review activities.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void review(List<FuncReviewCase> reviewCases) {
    new BizTemplate<Void>() {
      List<Long> reviewCaseIds;
      List<FuncReviewCase> reviewCasesDb;
      FuncReview reviewDb;
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the review cases exists
        reviewCaseIds = reviewCases.stream().map(FuncReviewCase::getId).toList();
        reviewCasesDb = funcReviewCaseQuery.checkAndFind(reviewCaseIds);
        // Check the review and cases is consistent
        Set<Long> planIds = reviewCasesDb.stream().map(FuncReviewCase::getPlanId)
            .collect(Collectors.toSet());
        assertTrue(planIds.size() == 1,"Only supports review one plan result");
        Set<Long> reviewIds = reviewCasesDb.stream().map(FuncReviewCase::getReviewId)
            .collect(Collectors.toSet());
        assertTrue(reviewIds.size() == 1,"Only supports modification one case review result");
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(reviewIds.iterator().next());
        // Check the review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planIds.iterator().next());
        // Check the case exists and can review
        casesDb = funcCaseQuery.checkAndFind(reviewCasesDb.stream().map(FuncReviewCase::getCaseId)
            .toList());
        // Check the review has started
        funcReviewQuery.checkHasStarted(reviewDb);
        // Check the case can review
        funcCaseQuery.checkCanReview(casesDb);
      }

      @Override
      protected Void process() {
        // Update review case
        Map<Long, FuncReviewCase> reviewCaseMap = reviewCases.stream()
            .collect(Collectors.toMap(FuncReviewCase::getId, x -> x));
        Map<Long, FuncCase> casesDbMap = casesDb.stream()
            .collect(Collectors.toMap(FuncCase::getId, x -> x));
        assembleUpdateReviewCases(reviewCasesDb, casesDbMap, reviewCaseMap);
        funcReviewCaseRepo.saveAll(reviewCasesDb);

        // Add review case records
        List<FuncReviewCaseRecord> reviewCaseRecords = assembleAddReviewCaseRecord(reviewCasesDb);
        funcReviewCaseRecordCmd.add0(reviewCaseRecords);

        // Update case review status
        Map<Long, FuncReviewCase> reviewCaseDbMap = reviewCasesDb.stream()
            .collect(Collectors.toMap(FuncReviewCase::getCaseId, x -> x));
        for (FuncCase caseDb : casesDb) {
          FuncReviewCase reviewCase = reviewCaseDbMap.get(caseDb.getId());
          setReviewInfoAndStatus(caseDb, reviewCase);
        }
        funcCaseRepo.saveAll(casesDb);

        // Add review activities
        funcCaseCmd.addReviewActivities(casesDb);
        return null;
      }
    }.execute();
  }

  /**
   * Reset review results for a batch of review cases.
   * <p>
   * Checks existence, consistency, permission, and resets review and case status.
   * <p>
   * Logs reset activities.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void reviewReset(Set<Long> ids, boolean reset) {
    new BizTemplate<Void>() {
      List<FuncReviewCase> reviewCasesDb;
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review cases exists
        reviewCasesDb = funcReviewCaseQuery.checkAndFind(ids);
        // Check the review and cases is consistent
        Set<Long> planIds = reviewCasesDb.stream().map(FuncReviewCase::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only supports resetting one case plan result");
        Set<Long> reviewIds = reviewCasesDb.stream().map(FuncReviewCase::getReviewId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(reviewIds.size() == 1,
            "Only supports resetting one case review result");
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(reviewIds.iterator().next());
        // Check the reset review permission
        funcPlanAuthQuery.checkResetReviewResultAuth(getUserId(), planIds.iterator().next());
      }

      @Override
      protected Void process() {
        List<Long> caseIds = reviewCasesDb.stream().map(FuncReviewCase::getCaseId)
            .toList();

        // Reset cases status
        if (reset) {
          funcCaseRepo.updateReviewResultToInitByIds(caseIds);
        } else {
          funcCaseRepo.updateReviewResultToRestartByIds(caseIds);
        }

        // Reset review cases status
        funcReviewCaseRepo.updateReviewResultToInitByReviewIdAndCaseIds(reviewDb.getId(), caseIds);

        // Reset review status
        if (reviewDb.getStatus().isComplete()) {
          reviewDb.setStatus(FuncPlanStatus.PENDING);
          funcReviewRepo.save(reviewDb);
        }

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAll0ByIdIn(caseIds);
        activityCmd.addAll(toActivities(FUNC_CASE, caseInfos, ActivityType.REVIEW_RESET));
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of review cases.
   * <p>
   * Checks existence, consistency, permission, and deletes review cases and records.
   * <p>
   * Logs delete activities.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncReviewCase> reviewCasesDb;
      FuncReview reviewDb;

      @Override
      protected void checkParams() {
        // Check the review cases exists
        reviewCasesDb = funcReviewCaseQuery.checkAndFind(ids);
        // Check the review and cases is consistent
        Set<Long> planIds = reviewCasesDb.stream().map(FuncReviewCase::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only supports deletion one case plan result");
        Set<Long> reviewIds = reviewCasesDb.stream().map(FuncReviewCase::getReviewId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(reviewIds.size() == 1,
            "Only supports deletion one case review result");
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(reviewIds.iterator().next());
        // Check the reset review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planIds.iterator().next());
      }

      @Override
      protected Void process() {
        funcReviewCaseRepo.deleteByIdIn(ids);

        funcReviewCaseRecordCmd.deleteByReviewCaseIdIn(ids);

        List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAll0ByIdIn(
            reviewCasesDb.stream().map(FuncReviewCase::getCaseId).toList());
        activityCmd.addAll(toActivities(FUNC_CASE, caseInfos, ActivityType.FUNC_REVIEW_DELETE,
            reviewDb.getName()));
        return null;
      }
    }.execute();
  }

  /**
   * Assemble review case records for a batch of review cases.
   * <p>
   * Copies properties from review cases to records for persistence.
   */
  public static List<FuncReviewCaseRecord> assembleAddReviewCaseRecord(
      List<FuncReviewCase> reviewCaseDbs) {
    // Save review case records
    return reviewCaseDbs.stream().map(reviewCaseDb -> {
      FuncReviewCaseRecord record = new FuncReviewCaseRecord();
      CoreUtils.copyProperties(reviewCaseDb, record, "id");
      record.setReviewCaseId(reviewCaseDb.getId());
      return record;
    }).toList();
  }

  /**
   * Update review cases with new review information.
   * <p>
   * Sets reviewer, date, status, remarks, and summary for each review case.
   */
  private void assembleUpdateReviewCases(List<FuncReviewCase> reviewCasesDb,
      Map<Long, FuncCase> casesDbMap, Map<Long, FuncReviewCase> reviewCaseMap) {
    for (FuncReviewCase reviewCaseDb : reviewCasesDb) {
      FuncReviewCase reviewCase = reviewCaseMap.get(reviewCaseDb.getId());
      FuncCase funcCase = casesDbMap.get(reviewCaseDb.getCaseId());
      FuncCaseDetailSummary detailSummary = joinSupplier.execute(
          () -> getCaseDetailSummary(funcCase));
      detailSummary.setReviewNum(detailSummary.getReviewNum() + 1)
          .setReviewFailNum(detailSummary.getReviewFailNum()
              + (reviewCase.getReviewStatus().isFailed() ? 1 : 0)
          )
          .setReviewRemark(reviewCase.getReviewRemark());
      reviewCaseDb.setReviewerId(getUserId())
          .setReviewDate(LocalDateTime.now())
          .setReviewStatus(reviewCase.getReviewStatus())
          .setReviewRemark(reviewCase.getReviewRemark())
          .setReviewedCase(detailSummary);
    }
  }

  /**
   * Get the repository for functional review cases.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncReviewCase, Long> getRepository() {
    return funcReviewCaseRepo;
  }
}
