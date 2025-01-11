package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.WEEKLY_WORKING_HOURS;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleLeadTime;
import static cloud.xcan.sdf.core.angustester.application.converter.KanbanEfficiencyCaseConverter.assembleUnplanned;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.formatDouble;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.calcWorkingDays;
import static cloud.xcan.sdf.spec.utils.WorkingTimeCalculator.calcWorkingHours;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseEfficiencySummary;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseTesterOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.OverdueRiskLevel;
import cloud.xcan.sdf.core.angustester.domain.kanban.TotalProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProgressCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.sdf.spec.utils.WorkingTimeCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class KanbanCtoCaseConverter {

  public static void assembleCaseOverview(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    assembleTotalProgressOverview(cases, overview);

    assembleBackloggedCaseCount(cases, overview);

    double dailyProcessedWorkload =
        overview.getBackloggedCount().getDailyProcessedWorkload();
    assembleOverdueAssessmentCount(cases, dailyProcessedWorkload, overview);

    assembleRecentDeliveryCount(cases, overview);

    assembleUnplannedWorkCount(cases, dailyProcessedWorkload, overview);

    assembleLeadTimeCount(cases, overview);

    assembleGroupOverview(cases, overview);
  }

  public static void assembleTesterOverview(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    Map<Long, List<FuncCaseEfficiencySummary>> testerGroup = cases.stream()
        .filter(x -> nonNull(x.getTesterId()))
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTesterId));
    for (Entry<Long, List<FuncCaseEfficiencySummary>> entry : testerGroup.entrySet()) {
      CtoCaseTesterOverview testerOverview = new CtoCaseTesterOverview();
      testerOverview.setTesterId(entry.getKey());
      assembleCaseOverview(entry.getValue(), testerOverview);
      overview.getTesterOverview().add(testerOverview);
    }
  }

  public static void assembleTotalProgressOverview(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    TotalProgressCount progressOverview = new TotalProgressCount();
    progressOverview.setTotalNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    progressOverview.setTotalCompletedNum(
        cases.stream().filter(x -> x.getTestResult().isPassed()).count());
    progressOverview.setTotalCompletedRate(progressOverview.calcTotalCompletedRate());

    progressOverview.setTotalWorkload(cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    progressOverview.setTotalCompletedWorkload(
        cases.stream().filter(x -> x.getTestResult().isPassed())
            .map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    progressOverview.setTotalCompletedWorkloadRate(
        progressOverview.calcTotalCompletedWorkloadRate());

    progressOverview.setTotalProgress(progressOverview.getTotalCompletedRate());

    overview.setTotalProgressOverview(progressOverview);
  }

  public static ProgressCount assembleCaseProgressCount0(List<FuncCaseEfficiencySummary> cases) {
    ProgressCount count = new ProgressCount();
    count.setTotalNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    count.setCompletedNum(
        cases.stream().filter(x -> x.getTestResult().isPassed()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getTotalNum()));

    count.setEvalWorkload(cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkload(
        cases.stream().filter(x -> x.getTestResult().isPassed())
            .map(FuncCaseEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));
    return count;
  }

  public static void assembleBackloggedCaseCount(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    BackloggedCount backloggedCaseCount = assembleBackloggedCaseCount0(cases);
    overview.setBackloggedCount(backloggedCaseCount);
  }

  public static @NotNull BackloggedCount assembleBackloggedCaseCount0(
      List<FuncCaseEfficiencySummary> cases) {
    BackloggedCount backloggedCaseCount = new BackloggedCount();

    long totalCaseNum = cases.stream().filter(x -> !x.getTestResult().isCanceled()).count();
    backloggedCaseCount.setTotalNum(totalCaseNum);
    double totalWorkload = cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
    backloggedCaseCount.setTotalWorkload(totalWorkload);
    // Sort in ascending order by completion time
    List<FuncCaseEfficiencySummary> completedCases = cases.stream()
        .filter(x -> x.getTestResult().isPassed() && nonNull(x.getTestResultHandleDate()))
        .sorted(Comparator.comparing(FuncCaseEfficiencySummary::getTestResultHandleDate))
        .collect(Collectors.toList());
    if (isNotEmpty(completedCases)) {
      long processedDays = calcProcessedDays(completedCases);
      backloggedCaseCount.setProcessedInDay(processedDays);
      double dailyProcessedWorkload = calcDailyProcessedWorkload(completedCases, processedDays);
      backloggedCaseCount.setDailyProcessedWorkload(formatDouble(dailyProcessedWorkload, "0.0"));
      long processedNum = completedCases.size();
      double dailyProcessedNum = processedDays == 0 ? 0 : (double) processedNum / processedDays;
      backloggedCaseCount.setDailyProcessedNum(formatDouble(dailyProcessedNum, "0.0"));
    }

    List<FuncCaseEfficiencySummary> backloggedCases = cases.stream()
        .filter(x -> !x.getTestResult().isFinished()).collect(Collectors.toList());
    if (isNotEmpty(backloggedCases)) {
      long backloggedNum = backloggedCases.size();
      backloggedCaseCount.setBackloggedNum(backloggedNum);
      backloggedCaseCount.setBackloggedRate(calcRate(backloggedNum, totalCaseNum));
      double backloggedWorkload = backloggedCases.stream()
          .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue();
      backloggedCaseCount.setBackloggedWorkload(backloggedWorkload);
      backloggedCaseCount.setBackloggedWorkloadRate(calcRate(backloggedWorkload, totalWorkload));
      double backloggedCompletionTime = backloggedCaseCount.getDailyProcessedWorkload() <= 0 ? 0
          : backloggedWorkload / backloggedCaseCount.getDailyProcessedWorkload();
      backloggedCaseCount.setBackloggedCompletionTime(
          formatDouble(backloggedCompletionTime, "0.0"));
    }
    return backloggedCaseCount;
  }

  public static double calcDailyProcessedWorkload(List<FuncCaseEfficiencySummary> cases,
      double default0) {
    double dailyProcessedWorkload = default0;
    // Sort in ascending order by completion time
    List<FuncCaseEfficiencySummary> completedCases = cases.stream()
        .filter(x -> x.getTestResult().isPassed() && nonNull(x.getTestResultHandleDate()))
        .sorted(Comparator.comparing(FuncCaseEfficiencySummary::getTestResultHandleDate))
        .collect(Collectors.toList());
    if (isNotEmpty(completedCases)) {
      long processedDays = calcProcessedDays(completedCases);
      dailyProcessedWorkload = calcDailyProcessedWorkload(completedCases, processedDays);
    }
    return dailyProcessedWorkload < 0 ? DEFAULT_DAILY_WORKLOAD : dailyProcessedWorkload;
  }

  public static long calcProcessedDays(List<FuncCaseEfficiencySummary> completedSortCases) {
    FuncCaseEfficiencySummary minStartCase = null;
    for (FuncCaseEfficiencySummary case0 : completedSortCases) {
      if (nonNull(case0.getCreatedDate()) // Exclude dirty data
          && (minStartCase == null || case0.getCreatedDate()
          .isBefore(minStartCase.getCreatedDate()))) {
        minStartCase = case0;
      }
    }
    if (isNull(minStartCase)) {
      return 0;
    }
    LocalDateTime startTime = minStartCase.getCreatedDate();
    LocalDateTime endTime = completedSortCases.get(completedSortCases.size() - 1)
        .getTestResultHandleDate();
    return calcWorkingDays(startTime, endTime);
  }

  public static double calcDailyProcessedWorkload(List<FuncCaseEfficiencySummary> completedCases,
      long processedDays) {
    if (processedDays == 0) {
      return DEFAULT_DAILY_WORKLOAD;
    }
    double processedWorkload = completedCases.stream()
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
    if (processedWorkload == 0d){
      return DEFAULT_DAILY_WORKLOAD;
    }
    return processedWorkload / processedDays;
  }

  public static void assembleOverdueAssessmentCount(List<FuncCaseEfficiencySummary> cases,
      double dailyProcessedWorkload, CtoCaseOverview overview) {
    OverdueAssessmentCount overdueAssessmentCount = assembleOverdueAssessmentCount0(cases,
        dailyProcessedWorkload);
    overview.setOverdueAssessmentCount(overdueAssessmentCount);
  }

  public static @NotNull OverdueAssessmentCount assembleOverdueAssessmentCount0(
      List<FuncCaseEfficiencySummary> cases, double dailyProcessedWorkload) {
    OverdueAssessmentCount overdueAssessmentCount = new OverdueAssessmentCount();
    overdueAssessmentCount.setTotalNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    overdueAssessmentCount.setTotalWorkload(cases.stream()
        .filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());

    overdueAssessmentCount.setOverdueNum(cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag()
            && !x.getTestResult().isCanceled()).count());
    overdueAssessmentCount.setOverdueRate(overdueAssessmentCount.calcOverdueRate());

    double overdueWorkload = cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag() && !x.getTestResult()
            .isCanceled()).map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    overdueAssessmentCount.setOverdueWorkload(overdueWorkload);
    overdueAssessmentCount.setOverdueWorkloadRate(
        overdueAssessmentCount.calcOverdueWorkloadRate());
    overdueAssessmentCount.setDailyProcessedWorkload(formatDouble(dailyProcessedWorkload, "0.0"));

    double overdueHours = cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag()
            && !x.getTestResult().isCanceled() && nonNull(x.getDeadlineDate())
            && x.getDeadlineDate().isBefore(LocalDateTime.now()))
        .map(case0 -> calcWorkingHours(case0.getDeadlineDate(), LocalDateTime.now()))
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    overdueAssessmentCount.setOverdueTime(formatDouble(overdueHours, "0.0"));
    double overDays = overdueWorkload / dailyProcessedWorkload <= 0
        ? DEFAULT_DAILY_WORKLOAD : dailyProcessedWorkload;
    overdueAssessmentCount.setOverdueWorkloadProcessingTime(
        formatDouble(overDays * WEEKLY_WORKING_HOURS, "0.0")/*To hours*/);
    overdueAssessmentCount.setRiskLevel(OverdueRiskLevel.calcLevel(overDays/* to days*/));
    return overdueAssessmentCount;
  }

  public static void assembleRecentDeliveryCount(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    Map<String, RecentDeliveryCount> recentDeliveryCount = assembleRecentDeliveryCount0(cases);

    overview.setRecentDeliveryCount(recentDeliveryCount);
  }

  public static Map<String, RecentDeliveryCount> assembleRecentDeliveryCount0(
      List<FuncCaseEfficiencySummary> cases) {
    long totalNum = cases.stream().filter(x -> !x.getTestResult().isCanceled()).count();
    double totalWorkload = cases.stream()
        .filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    long totalOverdueNum = cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag()).count();

    Map<String, RecentDeliveryCount> recentDeliveryCount = new HashMap<>();
    RecentDeliveryCount today = assembleRecentDeliveryCount(totalNum, cases, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isToday);
    RecentDeliveryCount lastWeek = assembleRecentDeliveryCount(totalNum, cases, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isLastWeek);
    RecentDeliveryCount lastMonth = assembleRecentDeliveryCount(totalNum, cases, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isLastMonth);

    recentDeliveryCount.put("today", today);
    recentDeliveryCount.put("lastWeek", lastWeek);
    recentDeliveryCount.put("lastMonth", lastMonth);
    return recentDeliveryCount;
  }

  public static RecentDeliveryCount assembleRecentDeliveryCount(long totalNum,
      List<FuncCaseEfficiencySummary> cases, double totalWorkload, long totalOverdueNum,
      Function<LocalDateTime, Boolean> isRecent) {
    RecentDeliveryCount deliveryCount = new RecentDeliveryCount();
    deliveryCount.setTotalNum(totalNum);
    deliveryCount.setCompletedNum(cases.stream()
        .filter(x -> x.getTestResult().isPassed() && isRecent.apply(x.getTestResultHandleDate()))
        .count());
    deliveryCount.setCompletedRate(deliveryCount.calcCompletedRate());

    deliveryCount.setTotalWorkload(totalWorkload);
    deliveryCount.setCompletedWorkload(cases.stream()
        .filter(x -> x.getTestResult().isPassed() && isRecent.apply(x.getTestResultHandleDate()))
        .map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setCompletedWorkloadRate(deliveryCount.calcCompletedWorkloadRate());
    deliveryCount.setSavingWorkload(cases.stream()
        .filter(x -> x.getTestResult().isPassed() && isRecent.apply(x.getTestResultHandleDate()))
        .map(FuncCaseEfficiencySummary::getSavingWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setSavingWorkloadRate(deliveryCount.calcSavingWorkloadRate());

    deliveryCount.setTotalOverdueNum(totalOverdueNum);
    deliveryCount.setOverdueNum(cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag() && x.getTestResult()
            .isPassed() && isRecent.apply(x.getTestResultHandleDate())).count());
    deliveryCount.setOverdueRate(deliveryCount.calcOverdueRate());
    deliveryCount.setOverdueWorkload(cases.stream()
        .filter(x -> nonNull(x.getOverdueFlag()) && x.getOverdueFlag()
            && x.getTestResult().isPassed() && isRecent.apply(x.getTestResultHandleDate()))
        .map(FuncCaseEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setOverdueWorkloadRate(deliveryCount.calcOverdueWorkloadRate());

    return deliveryCount;
  }

  public static void assembleUnplannedWorkCount(List<FuncCaseEfficiencySummary> cases,
      double dailyProcessedWorkload, CtoCaseOverview overview) {
    UnplannedWorkCount unplannedWorkCount = assembleUnplannedWorkCount0(cases,
        dailyProcessedWorkload);
    overview.setUnplannedWorkCount(unplannedWorkCount);
  }

  public static @NotNull UnplannedWorkCount assembleUnplannedWorkCount0(
      List<FuncCaseEfficiencySummary> cases, double dailyProcessedWorkload) {
    UnplannedWorkCount unplannedWorkCount = new UnplannedWorkCount();
    unplannedWorkCount.setTotalNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    unplannedWorkCount.setTotalWorkload(cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    assembleUnplanned(cases, unplannedWorkCount, dailyProcessedWorkload);
    return unplannedWorkCount;
  }

  public static void assembleLeadTimeCount(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    LeadTimeCount leadTimeCount = assembleLeadTimeCount0(cases);
    overview.setLeadTimeCount(leadTimeCount);
  }

  public static @NotNull LeadTimeCount assembleLeadTimeCount0(
      List<FuncCaseEfficiencySummary> cases) {
    LeadTimeCount leadTimeCount = new LeadTimeCount();
    assembleLeadTime(cases, leadTimeCount);
    return leadTimeCount;
  }

  public static void assembleGroupOverview(List<FuncCaseEfficiencySummary> cases,
      CtoCaseOverview overview) {
    Map<CaseTestResult, Integer> resultMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTestResult)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (CaseTestResult value : CaseTestResult.values()) {
      overview.getTotalTestResultCount().put(value, resultMap.getOrDefault(value, 0));
    }

    Map<ReviewStatus, Integer> reviewMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getReviewStatus)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (ReviewStatus value : ReviewStatus.values()) {
      overview.getTotalReviewStatusCount().put(value, reviewMap.getOrDefault(value, 0));
    }
  }


}
