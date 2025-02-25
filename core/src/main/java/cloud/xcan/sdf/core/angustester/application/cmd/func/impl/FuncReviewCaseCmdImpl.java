package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncReviewCaseConverter.setReviewInfoAndStatus;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncReviewCaseConverter.toReviewCase;
import static cloud.xcan.sdf.core.angustester.application.query.func.impl.FuncCaseQueryImpl.getCaseDetailSummary;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCaseRecordCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewRepo;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.review.record.FuncReviewCaseRecord;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.transaction.Transactional;

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
            .collect(Collectors.toList());
        List<IdKey<Long, Object>> idKeys = batchInsert(reviewCases);

        // Save activities
        activityCmd.addAll(toActivities(FUNC_CASE, funcCasesDb, ActivityType.FUNC_REVIEW_ADD,
            funcReviewDb.getName()));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void review(List<FuncReviewCase> reviewCases) {
    new BizTemplate<Void>() {
      List<Long> reviewCaseIds;
      List<FuncReviewCase> reviewCasesDb;
      Long reviewId;
      FuncReview reviewDb;
      List<FuncCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the review cases exists
        reviewCaseIds = reviewCases.stream().map(FuncReviewCase::getId)
            .collect(Collectors.toList());
        reviewCasesDb = funcReviewCaseQuery.checkAndFind(reviewCaseIds);
        // Check the review and cases is consistent
        Set<Long> planIds = reviewCasesDb.stream().map(FuncReviewCase::getPlanId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(planIds.size() == 1,
            "Only supports review one plan result");
        Set<Long> reviewIds = reviewCasesDb.stream().map(FuncReviewCase::getReviewId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(reviewIds.size() == 1,
            "Only supports modification one case review result");
        reviewId = reviewIds.iterator().next();
        // Check the review exists
        reviewDb = funcReviewQuery.checkAndFind(reviewIds.iterator().next());
        // Check the reset review permission
        funcPlanAuthQuery.checkReviewAuth(getUserId(), planIds.iterator().next());
        // Check the case exists
        casesDb = funcCaseQuery.checkAndFind(reviewCasesDb.stream().map(FuncReviewCase::getCaseId)
            .collect(Collectors.toList()));
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

        // Update review case record

        List<FuncReviewCaseRecord> reviewCaseRecords = assembleAddReviewCaseRecord(reviewCasesDb);
        funcReviewCaseRecordCmd.add0(reviewCaseRecords);

        // Save case review status
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
            .collect(Collectors.toList());

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
            reviewCasesDb.stream().map(FuncReviewCase::getCaseId).collect(Collectors.toList()));
        activityCmd.addAll(toActivities(FUNC_CASE, caseInfos, ActivityType.FUNC_REVIEW_DELETE,
            reviewDb.getName()));
        return null;
      }
    }.execute();
  }

  public static List<FuncReviewCaseRecord> assembleAddReviewCaseRecord(
      List<FuncReviewCase> reviewCaseDbs) {
    // Save review case records
    return reviewCaseDbs.stream().map(reviewCaseDb -> {
      FuncReviewCaseRecord record = new FuncReviewCaseRecord();
      CoreUtils.copyProperties(reviewCaseDb, record, "id");
      record.setReviewCaseId(reviewCaseDb.getId());
      return record;
    }).collect(Collectors.toList());
  }

  private void assembleUpdateReviewCases(List<FuncReviewCase> reviewCasesDb,
      Map<Long, FuncCase> casesDbMap, Map<Long, FuncReviewCase> reviewCaseMap) {
    for (FuncReviewCase reviewCaseDb : reviewCasesDb) {
      FuncReviewCase reviewCase = reviewCaseMap.get(reviewCaseDb.getId());
      reviewCaseDb.setReviewerId(getUserId())
          .setReviewDate(LocalDateTime.now())
          .setReviewStatus(reviewCase.getReviewStatus())
          .setReviewRemark(reviewCase.getReviewRemark())
          .setReviewedCase(joinSupplier.execute(
              () -> getCaseDetailSummary(casesDbMap.get(reviewCaseDb.getCaseId()))));
    }
  }

  @Override
  protected BaseRepository<FuncReviewCase, Long> getRepository() {
    return funcReviewCaseRepo;
  }
}
