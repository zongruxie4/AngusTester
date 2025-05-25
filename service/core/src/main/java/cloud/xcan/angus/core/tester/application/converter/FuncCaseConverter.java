package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.core.tester.application.cmd.func.impl.FuncCaseCmdImpl.getCaseCode;
import static cloud.xcan.angus.core.tester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.in;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.DateUtils.asDate;
import static cloud.xcan.angus.spec.utils.DateUtils.getLocalDateTime;
import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.convert;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.StringUtils.isNumeric;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastMonth;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.isLastWeek;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

import cloud.xcan.angus.api.commonlink.user.User;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.FuncTargetType;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncLastResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterCount;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncTesterProgressCount;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseSummary;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.kanban.DataTimeSeries;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class FuncCaseConverter {

  public static void assembleAddInfo(List<FuncCase> cases, FuncPlan planDb) {
    // Set case code and name prefix
    for (FuncCase case0 : cases) {
      if (isEmpty(case0.getCode())) {
        case0.setCode(getCaseCode());
      }
      if (isNotEmpty(planDb.getCasePrefix())
          && !case0.getName().startsWith(planDb.getCasePrefix())) {
        case0.setName(planDb.getCasePrefix() + case0.getName());
      }
      case0.setModuleId(nullSafe(case0.getModuleId(), -1L));
      case0.setReview(planDb.getReview());
      case0.setReviewStatus(planDb.getReview()
          ? nullSafe(case0.getReviewStatus(), ReviewStatus.PENDING) : null);
      case0.setProjectId(planDb.getProjectId())
          .setPlanAuth(planDb.getAuth())
          .setEvalWorkloadMethod(planDb.getEvalWorkloadMethod());
      case0.setUnplanned(!planDb.getStatus().isStarted());
      case0.setCreatedBy(nullSafe(case0.getCreatedBy(), getUserId()))
          .setCreatedDate(nullSafe(case0.getCreatedDate(), LocalDateTime.now()));
    }
  }

  public static void assembleUpdateInfo(List<FuncCase> cases, List<FuncCase> updatedCasesDb) {
    CoreUtils.batchCopyPropertiesIgnoreNull(cases, updatedCasesDb);
    for (FuncCase funcCaseDb : updatedCasesDb) {
      if (nonNull(funcCaseDb.getActualWorkload()) && isNull(funcCaseDb.getEvalWorkload())) {
        funcCaseDb.setEvalWorkload(funcCaseDb.getActualWorkload());
      }
      funcCaseDb.setOverdue(funcCaseDb.getDeadlineDate().isBefore(LocalDateTime.now()));
    }
  }

  public static FuncCase setReplaceInfo(FuncCase caseDb, FuncCase case0) {
    caseDb.setId(case0.getId())
        //.setPlanId(case0.getPlanId())
        .setModuleId(nullSafe(case0.getModuleId(), -1L))
        .setSoftwareVersion(case0.getSoftwareVersion())
        // .setCaseType(case0.getCaseType())
        .setName(case0.getName())
        //.setCode(case0.getCode())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getDeadlineDate().isBefore(LocalDateTime.now()))
        //.setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setStepView(case0.getStepView())
        .setSteps(case0.getSteps())
        .setDescription(case0.getDescription())
        //.setReview(case0.getReview())
        //.setReviewerId(case0.getReviewerId())
        //.setReviewDate(case0.getReviewDate())
        //.setReviewStatus(case0.getReviewStatus())
        //.setReviewRemark(case0.getReviewRemark())
        //.setReviewNum(case0.getReviewNum())
        //.setReviewFailNum(case0.getReviewFailNum())
        .setTesterId(case0.getTesterId())
        .setDeveloperId(case0.getDeveloperId())
        //.setTestNum(case0.getTestNum())
        //.setTestFailNum(case0.getTestFailNum())
        //.setTestResult(case0.getTestResult())
        //.setTestRemark(case0.getTestRemark())
        //.setTestResultHandleDate(case0.getTestResultHandleDate())
        .setAttachments(case0.getAttachments())
    //.setFavourite(case0.getFavourite())
    //.setFollow(case0.getFollow())
    //.setDirDeleted(case0.getDirDeleted())
    //.setPlanDeleted(case0.getPlanDeleted())
    //.setDeleted(case0.getDeleted())
    //.setDeletedBy(case0.getDeletedBy())
    //.setDeletedDate(case0.getDeletedDate())
    ;
    if (nonNull(caseDb.getActualWorkload()) && isNull(caseDb.getEvalWorkload())) {
      caseDb.setEvalWorkload(caseDb.getActualWorkload());
    }
    return caseDb;
  }

  public static void assembleExampleFuncPlan(Long projectId, Long id,
      FuncPlan plan, List<User> users) {
    Long currentUserId = isUserAction() ? getUserId() : users.get(0).getId();
    plan.setId(id).setProjectId(projectId)
        .setOwnerId(currentUserId)
        .setTesterResponsibilities(getTesterResponsibilities(plan, users))
        .setDeleted(false)
        .setDeadlineDate(LocalDateTime.now().plusDays(30))
        .setTenantId(getOptTenantId())
        .setCreatedBy(currentUserId).setCreatedDate(LocalDateTime.now())
        .setLastModifiedBy(currentUserId).setLastModifiedDate(LocalDateTime.now());
  }

  private static LinkedHashMap<Long, String> getTesterResponsibilities(FuncPlan plan,
      List<User> users) {
    int testerNum = Math.min(plan.getTesterResponsibilities().size(), users.size());
    LinkedHashMap<Long, String> testerResponsibilities = new LinkedHashMap<>();
    List<String> responsibilities = new ArrayList<>(plan.getTesterResponsibilities().values());
    for (int i = 0; i < testerNum; i++) {
      testerResponsibilities.put(users.get(i).getId(), responsibilities.get(i));
    }
    return testerResponsibilities;
  }

  public static void assembleExampleFuncCase(Long projectId, Long id,
      FuncCase case0, FuncPlan plan, List<User> users) {
    Random random = new Random();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime createdDate = now.minusHours(random.nextInt(5 * 24));
    LocalDateTime deadlineDate = now.plusHours(random.nextInt(10 * 24));
    LocalDateTime finishedDate = createdDate.plusHours(random.nextInt(24));
    finishedDate = finishedDate.isBefore(now) ? now.plusMinutes(1) : finishedDate;
    CaseTestResult result = nullSafe(case0.getTestResult(), CaseTestResult.PENDING);
    case0.setId(id).setProjectId(projectId)
        .setCode(getCaseCode()).setVersion(1) // In order to establish a baseline starting from 1
        .setPlanId(plan.getId())
        .setReviewerId(case0.getReview()
            && nonNull(case0.getReviewStatus()) && !case0.getReviewStatus().isPending()
            ? users.get(random.nextInt(users.size())).getId() : null)
        .setTesterId(users.get(random.nextInt(users.size())).getId())
        .setDeveloperId(users.get(random.nextInt(users.size())).getId())
        .setDeadlineDate(deadlineDate)
        .setTestResult(result)
        .setTestResultHandleDate(result.isFinished() ? finishedDate : null)
        .setTenantId(getOptTenantId())
        .setCreatedBy(users.get(random.nextInt(users.size())).getId())
        .setCreatedDate(createdDate)
        .setLastModifiedBy(users.get(random.nextInt(users.size())).getId())
        .setLastModifiedDate(result.isFinished() ? finishedDate : createdDate);
  }

  public static void assembleExampleFuncReview(Long projectId, Long id,
      FuncReview review, FuncPlan plan, List<User> users) {
    Long currentUserId = isUserAction() ? getUserId() : users.get(0).getId();
    review.setId(id).setProjectId(projectId)
        .setPlanId(plan.getId()).setOwnerId(currentUserId)
        .setParticipantIds(new LinkedHashSet<>(users.stream().map(User::getId)
            .collect(Collectors.toSet())))
        .setCreatedBy(currentUserId).setLastModifiedBy(currentUserId);
  }

  public static void assembleExampleFuncBaseline(Long projectId, Long id,
      FuncBaseline baseline, FuncPlan plan, List<FuncCase> cases, List<User> users) {
    Long currentUserId = isUserAction() ? getUserId() : users.get(0).getId();
    baseline.setId(id).setProjectId(projectId)
        .setPlanId(plan.getId()).setEstablished(false).setTenantId(getOptTenantId())
        .setCreatedBy(currentUserId).setLastModifiedBy(currentUserId);
    LinkedHashSet<Long> baselineCaseIds = new LinkedHashSet<>();
    int baselineCasesNum = cases.size() / 2;
    for (int j = 0; j < baselineCasesNum; j++) {
      baselineCaseIds.add(cases.get(j * 2).getId());
    }
    baseline.setCaseIds(baselineCaseIds);
  }

  public static FuncTrash toFuncCaseTrash(FuncCaseInfo caseDb) {
    return new FuncTrash()
        .setProjectId(caseDb.getProjectId())
        .setPlanId(caseDb.getPlanId())
        .setTargetType(FuncTargetType.CASE)
        .setTargetId(caseDb.getId())
        .setTargetName(caseDb.getName())
        .setCreatedBy(caseDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static FuncCase toCloneCase(FuncCase caseDb) {
    FuncCase newCase = CoreUtils.copyProperties(caseDb, new FuncCase());
    return newCase.setId(getBean(CachedUidGenerator.class).getUID())
        .setCode(getCaseCode())
        .setActualWorkload(null)
        .setReview(caseDb.getReview())
        .setReviewerId(null)
        .setReviewDate(null)
        .setReviewStatus(nonNull(caseDb.getReviewStatus()) ? ReviewStatus.PENDING : null)
        .setReviewRemark(null)
        .setReviewNum(0)
        .setReviewFailNum(0)
        .setTesterId(caseDb.getTesterId())
        .setTestNum(0)
        .setTestFailNum(0)
        .setTestResult(CaseTestResult.PENDING)
        .setTestRemark(null)
        .setTestResultHandleDate(null)
        .setPlanDeleted(caseDb.getPlanDeleted())
        .setDeleted(false)
        .setCreatedBy(getUserId())
        .setCreatedDate(LocalDateTime.now())
        /*.setName(newCase.getName() + "-CLONE." + randomAlphanumeric(6))*/;
  }

  public static void setReviewInfoAndStatus(FuncCase caseDb, FuncCase case0) {
    if (case0.getReviewStatus().isFailed() || case0.getReviewStatus().isPassed()) {
      caseDb.setReviewDate(LocalDateTime.now());
    }
    caseDb.setReviewerId(getUserId())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewDate(LocalDateTime.now()) // Last review time.
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(caseDb.getReviewNum() + 1)
        .setReviewFailNum(caseDb.getReviewFailNum() + (case0.getReviewStatus().isFailed() ? 1 : 0));
  }

  public static void setTestInfoAndStatus(FuncCase caseDb, FuncCase case0, boolean replace) {
    if (case0.getTestResult().isTestAction()) {
      caseDb.setTestResultHandleDate(LocalDateTime.now()); // Last test time.
    }
    caseDb.setTestResult(case0.getTestResult())
        .setEvalWorkload(replace ? case0.getEvalWorkload()
            : nullSafe(case0.getEvalWorkload(), caseDb.getEvalWorkload()))
        .setActualWorkload(replace ? case0.getActualWorkload()
            : nullSafe(caseDb.getActualWorkload(), caseDb.getEvalWorkload()))
        .setTestRemark(replace ? case0.getTestRemark() : caseDb.getTestRemark())
        .setTestNum(caseDb.getTestNum() + (case0.getTestResult().isTestAction() ? 1 : 0))
        .setTestFailNum(caseDb.getTestFailNum() + (case0.getTestResult().isNotPassed() ? 1 : 0));
    if (nonNull(caseDb.getActualWorkload()) && isNull(caseDb.getEvalWorkload())) {
      caseDb.setEvalWorkload(caseDb.getActualWorkload());
    }
    if (caseDb.getTestResult().isFinished()) {
      caseDb.setEvalWorkload(nullSafe(caseDb.getEvalWorkload(), caseDb.getActualWorkload()));
      caseDb.setActualWorkload(nullSafe(caseDb.getActualWorkload(), caseDb.getEvalWorkload()));
    }
  }

  public static FuncCaseSummary toCaseSummary(FuncCaseInfo case0) {
    return new FuncCaseSummary().setId(case0.getId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setProjectId(case0.getProjectId())
        .setPlanId(case0.getPlanId())
        .setModuleId(case0.getModuleId())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        //.setPrecondition(case0.getPrecondition())
        //.setSteps(case0.getSteps())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTesterId(case0.getTesterId())
        .setDeveloperId(case0.getDeveloperId())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        //.setAttachments(case0.getAttachments())
        //.setTags(ObjectUtils.isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
        //    .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
        //    .collect(Collectors.toList()) : Collections.emptyList())
        //.setRefMap(case0.getRefMap())
        //.setFavourite(case0.getFavourite())
        //.setFollow(case0.getFollow())
        //.setCommentNum(case0.getCommentNum())
        //.setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedDate(case0.getCreatedDate())
        //.setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static FuncCaseDetailSummary toCaseDetailSummary(FuncCase case0) {
    return new FuncCaseDetailSummary().setId(case0.getId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setProjectId(case0.getProjectId())
        .setPlanId(case0.getPlanId())
        .setModuleId(case0.getModuleId())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setSteps(case0.getSteps())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTesterId(case0.getTesterId())
        .setDeveloperId(case0.getDeveloperId())
        .setUnplanned(case0.getUnplanned())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        .setAttachments(case0.getAttachments())
        .setTags(ObjectUtils.isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
            .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .collect(Collectors.toList()) : Collections.emptyList())
        .setAssocTasks(case0.getAssocTasks())
        .setAssocCases(case0.getAssocCases())
        .setFavourite(case0.getFavourite())
        .setFollow(case0.getFollow())
        .setCommentNum(case0.getCommentNum())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedByName(case0.getCreatedByName())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static FuncCaseCount objectArrToCount(List<Object[]> groupByResult,
      List<Object[]> overdueResult, List<Object[]> oneTimePassReviewedResult,
      List<Object[]> alreadyTestedResult, List<Object[]> oneTimePassTestResult,
      List<Object[]> sumResult, List<Object[]> completedWorkloadResult) {
    FuncCaseCount statistics = new FuncCaseCount();
    if (isEmpty(groupByResult) && isEmpty(overdueResult)
        && isEmpty(oneTimePassTestResult) && isEmpty(sumResult)) {
      return statistics;
    }

    // Statistics by review_status
    Map<String, Integer> reviewStatusMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[0], String.class), x -> convert(x[2], Integer.class), Integer::sum));
    statistics.setPendingReviewNum(nullSafe(reviewStatusMap.get(ReviewStatus.PENDING.name()), 0))
        .setPassedReviewNum(nullSafe(reviewStatusMap.get(ReviewStatus.PASSED.name()), 0))
        .setFailedReviewNum(nullSafe(reviewStatusMap.get(ReviewStatus.FAILED.name()), 0))
        .setTotalReviewCaseNum(statistics.getPendingReviewNum() + statistics.getPassedReviewNum()
            + statistics.getFailedReviewNum()) // -> equal review=1
        .setTotalReviewedCaseNum(statistics.getPassedReviewNum() + statistics.getFailedReviewNum());

    // Statistics by test_result
    Map<String, Integer> testResultMap = groupByResult.stream().collect(Collectors.toMap(
        x -> convert(x[1], String.class), x -> convert(x[2], Integer.class), Integer::sum));
    statistics.setPendingTestNum(nullSafe(testResultMap.get(CaseTestResult.PENDING.name()), 0))
        .setPassedTestNum(nullSafe(testResultMap.get(CaseTestResult.PASSED.name()), 0))
        .setNotPassedTestNum(nullSafe(testResultMap.get(CaseTestResult.NOT_PASSED.name()), 0))
        .setBlockedTestNum(nullSafe(testResultMap.get(CaseTestResult.BLOCKED.name()), 0))
        .setCanceledTestNum(nullSafe(testResultMap.get(CaseTestResult.CANCELED.name()), 0))
        .setValidCaseNum(statistics.getPendingTestNum() + statistics.getPassedTestNum()
                + statistics.getNotPassedTestNum() + statistics.getBlockedTestNum()
            /*+ statistics.getCanceledTestNum()*/);

    statistics.setTotalCaseNum(statistics.getValidCaseNum() + statistics.getCanceledTestNum());

    // Statistics are overdue
    if (!overdueResult.isEmpty()) {
      Integer overdues = convert(overdueResult.get(0), Integer.class);
      statistics.setOverdueNum(nullSafe(overdues, 0));
    }

    // Statistics one-time pass review number
    if (!oneTimePassReviewedResult.isEmpty()) {
      Integer oneTimePass = convert(oneTimePassReviewedResult.get(0), Integer.class);
      statistics.setOneTimePassReviewNum(nullSafe(oneTimePass, 0));
      if (statistics.getTotalReviewedCaseNum() > 0) {
        // Statistics one-time pass rate
        statistics.setOneTimePassReviewRate(BigDecimal.valueOf((double)
                statistics.getOneTimePassReviewNum() / statistics.getTotalReviewedCaseNum())
            // X 100 -> %
            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
      }
    }

    // Statistics already tested case number
    if (!alreadyTestedResult.isEmpty()) {
      Integer alreadyTested = convert(alreadyTestedResult.get(0), Integer.class);
      statistics.setTotalTestedCaseNum(nullSafe(alreadyTested, 0));
    }

    // Statistics one-time pass test number
    if (!oneTimePassTestResult.isEmpty()) {
      Integer oneTimePass = convert(oneTimePassTestResult.get(0), Integer.class);
      statistics.setOneTimePassedTestNum(nullSafe(oneTimePass, 0));
      if (statistics.getTotalTestedCaseNum() > 0) {
        // Statistics one-time pass rate
        statistics.setOneTimePassedTestRate(BigDecimal.valueOf((double)
                statistics.getOneTimePassedTestNum() / statistics.getTotalTestedCaseNum())
            // X 100 -> %
            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
      }

      //      // Fix:: Ignore test cases and do not participate in calculations
      //      int actualCaseNum = statistics.getTotalCaseNum() - statistics.getIgnoredTestNum();
      //      if (actualCaseNum > 0) {
      //        // Statistics one-time pass rate
      //        statistics.setOneTimePassTestRate(BigDecimal
      //            .valueOf((double) statistics.getOneTimePassTestNum() / actualCaseNum)
      //            // X 100 -> %
      //            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
      //      }
    }

    // Statistics task workload and process times
    if (!sumResult.isEmpty()) {
      statistics.setTotalTestTimes(nullSafe(convert(sumResult.get(0)[0], Integer.class), 0));
      statistics.setTotalTestFailTimes(nullSafe(convert(sumResult.get(0)[1], Integer.class), 0));
      statistics.setTotalReviewTimes(nullSafe(convert(sumResult.get(0)[2], Integer.class), 0));
      Double evalWorkload = convert(sumResult.get(0)[3], Double.class);
      statistics.setEvalWorkload(
          nonNull(evalWorkload) ? BigDecimal.valueOf(evalWorkload) : BigDecimal.ZERO);
      Double actualWorkload = convert(sumResult.get(0)[4], Double.class);
      statistics.setActualWorkload(
          nonNull(actualWorkload) ? BigDecimal.valueOf(actualWorkload) : BigDecimal.ZERO);
    }
    if (!completedWorkloadResult.isEmpty()) {
      Double completedWorkload = convert(completedWorkloadResult.get(0), Double.class);
      statistics.setCompletedWorkload(
          nonNull(completedWorkload) ? BigDecimal.valueOf(completedWorkload) : BigDecimal.ZERO);
    }
    return statistics;
  }

  public static List<Long> objectArrToGroup(ArrayList<Object> result) {
    return isEmpty(result) ? Collections.emptyList()
        : result.stream().filter(Objects::nonNull).map(x -> ((BigInteger) x).longValue())
            .collect(Collectors.toList());
  }

  public static @NotNull List<FuncCase> importToDomain(CachedUidGenerator uidGenerator,
      FuncPlan planDb, List<String[]> data, int nameIdx,
      int moduleIdx, Map<String, Module> modulesMap,
      int testerIdx, Map<String, List<UserBase>> testerMap,
      int developerIdx, Map<String, List<UserBase>> developerMap,
      int reviwerIdx, Map<String, List<UserBase>> reviwerMap,
      int priorityIdx, int deadlineIdx, int preconditionIdx, int stepIdx, int descriptionIdx,
      int evalWorkloadIdx, int actualWorkloadIdx, int testResultIdx, int reviewStatusIdx,
      int testProcessDateIdx, int reviewDateIdx,
      int creatorIdx, Map<String, List<UserBase>> creatorsMap, int createdDateIdx,
      int tagsIdx, Map<String, List<Tag>> tagsMap,
      int tasksIdx, Map<String, List<TaskInfo>> tasksMap,
      int casesIdx, Map<String, List<FuncCaseInfo>> casesMap) {
    List<FuncCase> cases = new ArrayList<>();
    Locale zhLocale = SupportedLanguage.zh_CN.toLocale();
    for (String[] row : data) {
      FuncCase funcCase = new FuncCase().setId(uidGenerator.getUID())
          .setProjectId(planDb.getProjectId()).setCode(getTaskCode())
          .setPlanId(planDb.getId()).setPlanAuth(planDb.getAuth())
          .setName(row[nameIdx]) // Required
          .setVersion(1)
          .setTesterId(testerMap.get(row[testerIdx]).get(0).getId()) // Required
          .setDeveloperId(developerMap.get(row[developerIdx]).get(0).getId()) // Required
          .setReviewerId(reviwerIdx != -1 && nonNull(reviwerMap.get(row[reviwerIdx]))
              ? reviwerMap.get(row[reviwerIdx]).get(0).getId() : null)
          .setPriority(nullSafe(Priority.ofMessage(row[priorityIdx], zhLocale), Priority.DEFAULT))
          .setDeadlineDate(getLocalDateTime(row[deadlineIdx])) // Required
          .setPrecondition(preconditionIdx != -1 ? row[preconditionIdx] : null)
          .setSteps(stepIdx != -1 ? toSteps(row[stepIdx]) : null)
          .setDescription(descriptionIdx != -1 ? row[descriptionIdx] : null)
          .setEvalWorkloadMethod(planDb.getEvalWorkloadMethod())
          .setEvalWorkload(evalWorkloadIdx != -1 && isNumeric(row[evalWorkloadIdx])
              ? BigDecimal.valueOf(Double.parseDouble(row[evalWorkloadIdx])) : null)
          .setActualWorkload(actualWorkloadIdx != -1 && isNumeric(row[actualWorkloadIdx])
              ? BigDecimal.valueOf(Double.parseDouble(row[actualWorkloadIdx])) : null)
          .setTestResult(testResultIdx != -1 && isNotEmpty(row[testResultIdx])
              ? CaseTestResult.ofMessage(row[testResultIdx], zhLocale) : CaseTestResult.PENDING)
          .setReviewStatus(reviewStatusIdx != -1 && isNotEmpty(row[reviewStatusIdx])
              ? ReviewStatus.ofMessage(row[reviewStatusIdx], zhLocale) : ReviewStatus.PENDING)
          .setTestResultHandleDate(testProcessDateIdx != -1 && isNotEmpty(row[testProcessDateIdx])
              ? getLocalDateTime(row[testProcessDateIdx]) : null)
          .setReviewDate(reviewDateIdx != -1 && isNotEmpty(row[reviewDateIdx])
              ? getLocalDateTime(row[reviewDateIdx]) : null);
      funcCase.setCreatedBy(creatorIdx != -1 && nonNull(creatorsMap.get(row[creatorIdx]))
          ? creatorsMap.get(row[creatorIdx]).get(0).getId() : getUserId());
      funcCase.setCreatedDate(createdDateIdx != -1 && isNotEmpty(row[createdDateIdx])
          ? getLocalDateTime(row[createdDateIdx]) : LocalDateTime.now());

      funcCase.setModuleId(moduleIdx != -1 && isNotEmpty(row[moduleIdx])
          ? modulesMap.get(row[moduleIdx]).getId() : -1L);

      List<String> taskTags = isNotEmpty(row[tagsIdx])
          ? List.of(row[tagsIdx].split("##")) : null;
      if (isNotEmpty(taskTags)) {
        funcCase.setTagTargets(taskTags.stream().filter(x -> nonNull(tagsMap.get(x)))
            .map(x -> new TagTarget().setId(uidGenerator.getUID())
                .setTargetId(funcCase.getId()).setTagId(tagsMap.get(x).get(0).getId()))
            .collect(Collectors.toList()));
      }

      List<String> taskNames0 = isNotEmpty(row[tasksIdx])
          ? List.of(row[tasksIdx].split("##")) : null;
      if (isNotEmpty(taskNames0)) {
        List<Long> refTaskIds = taskNames0.stream().filter(x -> nonNull(tasksMap.get(x)))
            .map(x -> tasksMap.get(x).get(0).getId()).collect(Collectors.toList());
        funcCase.setRefTaskIds(new LinkedHashSet<>(refTaskIds));
      }
      List<String> caseNames0 = isNotEmpty(row[casesIdx])
          ? List.of(row[casesIdx].split("##")) : null;
      if (isNotEmpty(caseNames0)) {
        List<Long> refCaseIds = caseNames0.stream().filter(x -> nonNull(casesMap.get(x)))
            .map(x -> casesMap.get(x).get(0).getId()).collect(Collectors.toList());
        funcCase.setRefCaseIds(new LinkedHashSet<>(refCaseIds));
      }

      funcCase.setOverdue(false)
          .setReview(planDb.getReview())
          .setReviewNum(0)
          .setReviewFailNum(0)
          .setTestNum(0)
          .setTestFailNum(0)
          .setPlanDeleted(false)
          .setDeleted(false);
      cases.add(funcCase);
    }
    return cases;
  }

  private static List<CaseTestStep> toSteps(String steps) {
    if (isEmpty(steps)) {
      return null;
    }
    // Delete import step line break
    steps = steps.replaceAll("@@\n", "@@");
    String[] stepsArray = steps.split("@@");
    if (isEmpty(stepsArray)) {
      return null;
    }
    List<CaseTestStep> testSteps = new ArrayList<>();
    for (String step : stepsArray) {
      String[] stepArray = step.split("##");
      if (stepArray.length > 0) {
        testSteps.add(new CaseTestStep().setStep(stepArray[0])
            .setExpectedResult(stepArray.length > 1 ? stepArray[1] : null));
      }
    }
    return testSteps;
  }

  public static void assembleTimeSeriesByFormat(Map<BurnDownResourceType, BurnDownChartCount> chartMap,
      List<FuncCaseEfficiencySummary> validCases, LocalDateTime safeCreatedDateStart,
      LocalDateTime safeCreatedDateEnd) {
    List<String> days = new ArrayList<>();
    LocalDateTime nextDay = safeCreatedDateStart;
    do {
      days.add(format(asDate(nextDay), DEFAULT_DAY_FORMAT));
      nextDay = nextDay.plusDays(1);
    } while (nextDay.isBefore(safeCreatedDateEnd));
    String lastDay = format(asDate(safeCreatedDateEnd), DEFAULT_DAY_FORMAT);
    if (!days.contains(lastDay)) {
      days.add(lastDay);
    }

    Map<String, List<FuncCaseEfficiencySummary>> dayCompletedGroup = validCases.stream()
        .filter(x -> x.getTestResult().isPassed())
        .collect(groupingBy(x -> format(asDate(x.getCreatedDate()), DEFAULT_DAY_FORMAT),
            Collectors.mapping(Function.identity(), Collectors.toList())));

    String currentDay = format(new Date(), DEFAULT_DAY_FORMAT);

    // Num burndown chart
    BurnDownChartCount numChart = new BurnDownChartCount();
    long total = validCases.size();
    long completed = validCases.stream().filter(x -> x.getTestResult().isPassed()).count();
    numChart.setTotal(total).setCompleted(completed).setRemained(total - completed)
        .setStartDate(safeCreatedDateStart).setEndDate(safeCreatedDateEnd);
    double dayExpectedNum = (double) validCases.size() / days.size();
    List<DataTimeSeries> allExpectedNum = new ArrayList<>();
    List<DataTimeSeries> allRemainingNum = new ArrayList<>();
    int remainingNum = validCases.size();
    boolean isFuture = true;
    for (int i = 0; i < days.size(); i++) {
      allExpectedNum.add(new DataTimeSeries(days.get(i),
          (int) ((double) validCases.size() - ((i + 1) * dayExpectedNum))));
      if (isFuture) {
        if (currentDay.equals(days.get(i))) {
          remainingNum = remainingNum - (dayCompletedGroup.containsKey(days.get(i))
              ? dayCompletedGroup.get(days.get(i)).size() : 0);
          allRemainingNum.add(new DataTimeSeries(days.get(i), remainingNum));
          isFuture = false;
          continue;
        }
        remainingNum = remainingNum - (dayCompletedGroup.containsKey(days.get(i))
            ? dayCompletedGroup.get(days.get(i)).size() : 0);
        allRemainingNum.add(new DataTimeSeries(days.get(i), remainingNum));
      }
    }
    numChart.setExpected(allExpectedNum).setRemaining(allRemainingNum);
    chartMap.put(BurnDownResourceType.NUM, numChart);

    // Workload burndown chart
    BurnDownChartCount workloadChart = new BurnDownChartCount();
    BigDecimal totalWorkload = validCases.stream().map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal completedWorkload = validCases.stream().filter(x -> x.getTestResult().isPassed())
        .map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    workloadChart.setTotal(totalWorkload).setCompleted(completedWorkload)
        .setRemained(totalWorkload.subtract(completedWorkload))
        .setStartDate(safeCreatedDateStart).setEndDate(safeCreatedDateEnd);
    BigDecimal dayExpectedWorkload = ((BigDecimal) workloadChart.getTotal())
        .divide(BigDecimal.valueOf(days.size()), 2, RoundingMode.HALF_UP);
    List<DataTimeSeries> allExpectedWorkload = new ArrayList<>();
    List<DataTimeSeries> allRemainingWorkload = new ArrayList<>();
    BigDecimal remainingWorkload = (BigDecimal) workloadChart.getTotal();
    isFuture = true;
    for (int i = 0; i < days.size(); i++) {
      BigDecimal expectedWorkload = ((BigDecimal) workloadChart.getTotal())
          .subtract(dayExpectedWorkload.multiply(BigDecimal.valueOf(i + 1)));
      allExpectedWorkload.add(new DataTimeSeries(days.get(i),
          expectedWorkload.doubleValue() > 1 ? expectedWorkload : BigDecimal.ZERO));
      if (isFuture) {
        if (currentDay.equals(days.get(i))) {
          remainingWorkload = remainingWorkload.subtract(dayCompletedGroup.containsKey(days.get(i))
              ? dayCompletedGroup.get(days.get(i)).stream()
              .map(FuncCaseEfficiencySummary::getEvalWorkload)
              .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO);
          allRemainingWorkload.add(new DataTimeSeries(days.get(0), remainingWorkload));
          isFuture = false;
          continue;
        }
        remainingWorkload = remainingWorkload.subtract(dayCompletedGroup.containsKey(days.get(i))
            ? dayCompletedGroup.get(days.get(i)).stream()
            .map(FuncCaseEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO);
        allRemainingWorkload.add(new DataTimeSeries(days.get(0), remainingWorkload));
      }
    }
    workloadChart.setExpected(allExpectedWorkload).setRemaining(allRemainingWorkload);
    chartMap.put(BurnDownResourceType.WORKLOAD, workloadChart);
  }

  /**
   * Number of statistical plan
   */
  public static void countCreationPlan(FuncLastResourceCreationCount result, List<FuncPlan> plans) {
    result.setAllPlan(plans.size())
        .setPlanByLastMonth(plans.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setPlanByLastWeek(plans.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<FuncPlanStatus, List<FuncPlan>> statusMap = plans.stream()
        .collect(Collectors.groupingBy(FuncPlan::getStatus));
    for (FuncPlanStatus value : FuncPlanStatus.values()) {
      result.getPlanByStatus().put(value, statusMap.getOrDefault(value, emptyList()).size());
    }
  }

  /**
   * Number of statistical case
   */
  public static void countCreationCase(FuncLastResourceCreationCount result,
      List<FuncCaseEfficiencySummary> cases) {
    result.setAllCase(cases.size())
        .setCaseByOverdue(cases.stream().filter(FuncCaseEfficiencySummary::getOverdue).count())
        .setCaseByLastWeek(cases.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setCaseByLastMonth(cases.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<CaseTestResult, List<FuncCaseEfficiencySummary>> resultMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTestResult));
    for (CaseTestResult value : CaseTestResult.values()) {
      result.getCaseByTestResult().put(value, resultMap.getOrDefault(value, emptyList()).size());
    }
    Map<ReviewStatus, List<FuncCaseEfficiencySummary>> reviewMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getReviewStatus));
    for (ReviewStatus value : ReviewStatus.values()) {
      result.getCaseByReviewStatus().put(value, reviewMap.getOrDefault(value, emptyList()).size());
    }
    Map<Priority, List<FuncCaseEfficiencySummary>> priorityMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getPriority));
    for (Priority value : Priority.values()) {
      result.getCaseByPriority().put(value, priorityMap.getOrDefault(value, emptyList()).size());
    }
  }

  /**
   * Number of statistical review
   */
  public static void countCreationReview(FuncLastResourceCreationCount result,
      List<FuncReview> reviews) {
    result.setAllReview(reviews.size())
        .setReviewByLastMonth(reviews.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setReviewByLastWeek(reviews.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
    Map<FuncPlanStatus, List<FuncReview>> statusMap = reviews.stream()
        .collect(Collectors.groupingBy(FuncReview::getStatus));
    for (FuncPlanStatus value : FuncPlanStatus.values()) {
      if (value.isSupportInReview()){
        result.getReviewByStatus().put(value, statusMap.getOrDefault(value, emptyList()).size());
      }
    }
  }

  /**
   * Number of statistical baseline
   */
  public static void countCreationBaseline(FuncLastResourceCreationCount result,
      List<FuncBaseline> baselines) {
    result.setAllBaseline(baselines.size())
        .setBaselineByLastWeek(
            baselines.stream().filter(x -> isLastWeek(x.getCreatedDate())).count())
        .setBaselineByLastMonth(
            baselines.stream().filter(x -> isLastMonth(x.getCreatedDate())).count());
  }

  public static @NotNull FuncTesterCount assembleCaseTesterCount(Long testerId,
      Map<Long, UserBase> userMaps, Map<Long, List<FuncCaseEfficiencySummary>> testerTaskMap) {
    FuncTesterCount count = new FuncTesterCount();
    UserBase assignee = userMaps.get(testerId);
    count.setTesterId(testerId);
    count.setTesterName(nonNull(assignee) ? assignee.getFullName() : null);
    count.setTesterAvatar(nonNull(assignee) ? assignee.getAvatar() : null);

    List<FuncCaseEfficiencySummary> testerCases = testerTaskMap.get(testerId);
    count.setTotalCaseNum(testerCases.size());
    List<FuncCaseEfficiencySummary> validTesterCases = testerCases.stream()
        .filter(x -> !x.getTestResult().isCanceled()).collect(Collectors.toList());
    count.setValidCaseNum(validTesterCases.size());

    count.setEvalWorkload(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setActualWorkload(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkload(
        validTesterCases.stream().filter(x -> x.getTestResult().isPassed())
            .map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));
    count.setSavingWorkload(count.getEvalWorkload() - count.getActualWorkload());
    count.setSavingWorkloadRate(calcRate(count.getSavingWorkload(), count.getEvalWorkload()));

    count.setReviewNum(validTesterCases.stream().filter(x -> nonNull(x.getReviewStatus())).count());
    count.setPassedReviewNum(validTesterCases.stream()
        .filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed()).count());
    count.setPassedReviewRate(calcRate(count.getPassedReviewNum(), count.getReviewNum()));

    count.setReviewTimes(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getReviewNum)
            .reduce(0, Integer::sum));
    count.setReviewFailTimes(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getReviewFailNum)
            .reduce(0, Integer::sum));
    count.setOneTimePassedReviewNum(validTesterCases.stream()
        .filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed()
            && x.getReviewFailNum() == 0).count());
    count.setOneTimePassedReviewRate(
        calcRate(count.getOneTimePassedReviewNum(), count.getReviewNum()));

    count.setPassedTestNum(
        validTesterCases.stream().filter(x -> x.getTestResult().isPassed()).count());
    count.setPassedTestRate(calcRate(count.getPassedTestNum(), count.getValidCaseNum()));
    count.setFailedTestNum(
        validTesterCases.stream().filter(x -> !x.getTestResult().isPassed()).count());
    count.setFailedTestRate(calcRate(count.getFailedTestNum(), count.getValidCaseNum()));

    count.setTestTimes(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getTestNum)
            .reduce(0, Integer::sum));
    count.setTestFailTimes(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getTestFailNum)
            .reduce(0, Integer::sum));
    count.setOneTimePassedTestNum(validTesterCases.stream()
        .filter(x -> x.getTestResult().isPassed() && x.getTestFailNum() == 0).count());
    count.setOneTimePassedTestRate(
        calcRate(count.getOneTimePassedTestNum(), count.getValidCaseNum()));

    count.setOverdueNum(
        validTesterCases.stream().filter(FuncCaseEfficiencySummary::getOverdue).count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getValidCaseNum()));
    return count;
  }

  public static @NotNull FuncTesterProgressCount assembleCaseTesterProgressCount(Long testerId,
      Map<Long, UserBase> userMaps, Map<Long, List<FuncCaseEfficiencySummary>> testerTaskMap) {
    FuncTesterProgressCount count = new FuncTesterProgressCount();
    UserBase assignee = userMaps.get(testerId);
    count.setTesterId(testerId);
    count.setTesterName(nonNull(assignee) ? assignee.getFullName() : null);
    count.setTesterAvatar(nonNull(assignee) ? assignee.getAvatar() : null);

    List<FuncCaseEfficiencySummary> testerCases = testerTaskMap.get(testerId);
    count.setTotalCaseNum(testerCases.size());
    List<FuncCaseEfficiencySummary> validTesterCases = testerCases.stream()
        .filter(x -> !x.getTestResult().isCanceled()).collect(Collectors.toList());
    count.setValidCaseNum(validTesterCases.size());

    count.setEvalWorkload(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getEvalWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setActualWorkload(
        validTesterCases.stream().map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkload(
        validTesterCases.stream().filter(x -> x.getTestResult().isPassed())
            .map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));
    count.setSavingWorkload(count.getEvalWorkload() - count.getActualWorkload());
    count.setSavingWorkloadRate(calcRate(count.getSavingWorkload(), count.getEvalWorkload()));

    count.setPassedTestNum(
        validTesterCases.stream().filter(x -> x.getTestResult().isPassed()).count());
    count.setPassedTestRate(calcRate(count.getPassedTestNum(), count.getValidCaseNum()));

    count.setOverdueNum(
        validTesterCases.stream().filter(FuncCaseEfficiencySummary::getOverdue).count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getValidCaseNum()));
    return count;
  }

  @Deprecated
  public static Set<SearchCriteria> getCaseResourcesFilter(Long projectId, Long planId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdBys) {
    Set<SearchCriteria> filters = getCommonResourcesStatsFilter(projectId, startDate,
        endDate, createdBys);
    if (nonNull(planId)) {
      filters.add(equal("planId", planId));
    }
    filters.add(equal("deleted", false));
    filters.add(equal("planDeleted", false));
    return filters;
  }

  public static Set<SearchCriteria> getCaseTesterResourcesFilter(Long projectId, Long planId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> testerIds) {
    Set<SearchCriteria> filters = getCaseResourcesFilter(
        projectId, planId, startDate, endDate);
    if (isNotEmpty(testerIds)){
      filters.add(in("testerId", testerIds));
    }
    return filters;
  }

  public static Set<SearchCriteria> getCaseCreatorResourcesFilter(Long projectId, Long planId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdByIds) {
    Set<SearchCriteria> filters = getCaseResourcesFilter(projectId,
        planId, startDate, endDate);
    if (isNotEmpty(createdByIds)){
      filters.add(in("createdBy", createdByIds));
    }
    return filters;
  }

  public static @NotNull Set<SearchCriteria> getCaseResourcesFilter(Long projectId,
      Long planId, LocalDateTime startDate, LocalDateTime endDate) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(planId)) {
      filters.add(equal("planId", planId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    filters.add(equal("deleted", false));
    filters.add(equal("planDeleted", false));
    return filters;
  }

  public static Set<SearchCriteria> getCommonCreatorResourcesFilter(Long projectId,
      Long planId, LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdIds) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(planId)) {
      filters.add(equal("planId", planId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    if (isNotEmpty(createdIds)) {
      filters.add(in("createdBy", createdIds));
    }
    return filters;
  }
}
