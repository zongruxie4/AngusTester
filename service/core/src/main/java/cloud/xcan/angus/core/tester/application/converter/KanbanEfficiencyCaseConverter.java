package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.angus.api.commonlink.TesterConstant.WEEKLY_WORKING_HOURS;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.calcDailyProcessedWorkload;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.calcProcessedDays;
import static cloud.xcan.angus.core.tester.application.query.kanban.impl.KanbanDataAssetsQueryImpl.getTimeSeriesByFormat;
import static cloud.xcan.angus.core.tester.domain.kanban.AssigneeRanking.rank;
import static cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult.NOT_PASSED;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.formatDouble;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.calcWorkingHours;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.issue.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.issue.count.TestCaseHitCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.issue.count.WorkloadCountBase;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseCountOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseRanking;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseTesterOverview;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.count.CoreKpiCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.GrowthTrendDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ResourceCreationDetail;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.ReviewEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.test.cases.count.TestingEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.metrics.statistics.ListStatistics;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class KanbanEfficiencyCaseConverter {

  public static void assembleCaseTotalOverview(List<FuncCaseEfficiencySummary> cases,
      List<CaseTestHit> testHits, EfficiencyCaseOverview overview) {
    EfficiencyCaseCountOverview totalOverview = assembleCaseCountOverview(cases, testHits);
    overview.setTotalOverview(totalOverview);
    assembleGroupOverview(cases, overview);
  }

  public static void assembleTesterOverview(List<FuncCaseEfficiencySummary> cases,
      List<CaseTestHit> testHits, EfficiencyCaseOverview overview) {
    Map<Long, List<FuncCaseEfficiencySummary>> testerGroup = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTesterId));
    Map<Long, List<CaseTestHit>> testHitsMap = isEmpty(testHits) ? emptyMap()
        : testHits.stream().collect(Collectors.groupingBy(CaseTestHit::getBugCreatedBy));
    for (Entry<Long, List<FuncCaseEfficiencySummary>> entry : testerGroup.entrySet()) {
      EfficiencyCaseTesterOverview testerOverview = new EfficiencyCaseTesterOverview();

      assembleTesterOverview(entry, testHitsMap.get(entry.getKey()), testerOverview);

      overview.getTesterOverview().add(testerOverview);
    }
  }

  public static void assembleTesterOverview(Entry<Long, List<FuncCaseEfficiencySummary>> entry,
      List<CaseTestHit> testHits, EfficiencyCaseTesterOverview testerOverview) {
    testerOverview.setTesterId(entry.getKey());

    EfficiencyCaseCountOverview testerTotalOverview
        = assembleCaseCountOverview(entry.getValue(), testHits);
    testerOverview.setStatusOverview(testerTotalOverview);

    assembleGroupOverview(entry, testerOverview);
  }

  public static EfficiencyCaseCountOverview assembleCaseCountOverview(
      List<? extends FuncCaseEfficiencySummary> cases, List<CaseTestHit> testHits) {
    EfficiencyCaseCountOverview totalOverview = new EfficiencyCaseCountOverview();
    // Total
    totalOverview.setTotalCaseNum(cases.size());
    totalOverview.setValidCaseNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    // Status
    assembleStatusOverview(cases, totalOverview);
    // Overdue
    assembleOverdueOverview(cases, totalOverview);
    // Testing efficiency
    assembleTestingEfficiency(cases, totalOverview);
    // Review
    assembleReviewCount(cases, totalOverview);
    // Review efficiency
    assembleReviewEfficiency(cases, totalOverview);
    // Workload
    assembleWorkload(cases, totalOverview);
    // Lead time
    assembleLeadTime(cases, totalOverview);
    // Unplanned
    assembleUnplanned(cases, totalOverview, totalOverview.getDailyProcessedWorkload());
    // Test hits
    assembleTestHits(cases.size(), testHits, totalOverview);
    return totalOverview;
  }

  public static void assembleStatusOverview(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    Map<CaseTestResult, List<FuncCaseEfficiencySummary>> sg = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTestResult));
    totalOverview.setPendingTestNum(sg.getOrDefault(CaseTestResult.PENDING, emptyList()).size());
    totalOverview.setPassedTestNum(sg.getOrDefault(CaseTestResult.PASSED, emptyList()).size());
    totalOverview.setNotPassedTestNum(sg.getOrDefault(NOT_PASSED, emptyList()).size());
    totalOverview.setBlockedTestNum(sg.getOrDefault(CaseTestResult.BLOCKED, emptyList()).size());
    totalOverview.setCanceledTestNum(sg.getOrDefault(CaseTestResult.CANCELED, emptyList()).size());
  }

  public static void assembleOverdueOverview(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    totalOverview.setOverdueNum(
        cases.stream().filter(x -> !x.getTestResult().isCanceled()
            && nonNull(x.getOverdue()) && x.getOverdue()).count());
    totalOverview.setOverdueRate(totalOverview.calcOverdueRate());
  }

  public static void assembleTestingEfficiency(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    totalOverview.setProgress(totalOverview.calcProgress());
    totalOverview.setValidCaseRate(totalOverview.calcValidCaseRate());
    totalOverview.setInvalidCaseNum(
        cases.stream().filter(x -> x.getTestResult().isCanceled()).count());
    totalOverview.setInvalidCaseRate(totalOverview.calcInvalidCaseRate());
    assembleTestingEfficiencyOverview0(cases, totalOverview);
  }

  public static TestingEfficiencyCount assembleTestingEfficiencyOverview(
      List<? extends FuncCaseEfficiencySummary> cases) {
    TestingEfficiencyCount count = new TestingEfficiencyCount();
    count.setTotalNum(cases.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    assembleTestingEfficiencyOverview0(cases, count);
    return count;
  }

  private static void assembleTestingEfficiencyOverview0(
      List<? extends FuncCaseEfficiencySummary> cases, TestingEfficiencyCountBase totalOverview) {
    totalOverview.setPassedTestNum(
        cases.stream().filter(x -> x.getTestResult().isPassed()).count());
    totalOverview.setPassedTestRate(totalOverview.calcPassedTestRate());
    totalOverview.setOneTimePassedNum(
        cases.stream().filter(x -> x.getTestResult().isPassed() && x.getTestFailNum() == 0)
            .count());
    totalOverview.setOneTimePassedRate(totalOverview.calcOneTimePassedRate());
    totalOverview.setTwoTimePassedNum(
        cases.stream().filter(x -> x.getTestResult().isPassed() && x.getTestFailNum() == 1)
            .count());
    totalOverview.setTwoTimePassedRate(totalOverview.calcTwoTimePassedRate());
    totalOverview.setOneTimeNotPassedNum(
        cases.stream().filter(x -> x.getTestResult().isPassed() && x.getTestFailNum() > 0).count());
    totalOverview.setOneTimeNotPassedRate(totalOverview.calcOneTimeNotPassedRate());
  }

  private static void assembleReviewCount(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    Map<ReviewStatus, List<FuncCaseEfficiencySummary>> rg = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getReviewStatus));
    totalOverview.setPendingReviewNum(rg.getOrDefault(ReviewStatus.PENDING, emptyList()).size());
    totalOverview.setPassedReviewNum(rg.getOrDefault(ReviewStatus.PASSED, emptyList()).size());
    totalOverview.setFailedReviewNum(rg.getOrDefault(ReviewStatus.FAILED, emptyList()).size());
    totalOverview.setTotalReviewCaseNum(cases.stream()
        .filter(x -> nonNull(x.getReview()) && x.getReview()).count());
    totalOverview.setTotalNotReviewCaseNum(cases.stream()
        .filter(x -> isNull(x.getReview()) || !x.getReview()).count());
    totalOverview.setTotalReviewedCaseNum(totalOverview.getFailedReviewNum()
        + totalOverview.getPassedReviewNum());
  }

  public static void assembleReviewEfficiency(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    totalOverview.setReviewProgress(totalOverview.calcReviewProgress());
    totalOverview.setTotalReviewTimes(
        cases.stream().map(FuncCaseEfficiencySummary::getReviewNum).reduce(0, Integer::sum));
    totalOverview.setTotalReviewFailTimes(
        cases.stream().map(FuncCaseEfficiencySummary::getReviewFailNum).reduce(0, Integer::sum));

    assembleReviewEfficiencyOverview0(cases, totalOverview);
  }

  public static ReviewEfficiencyCount assembleReviewEfficiencyOverview(
      List<? extends FuncCaseEfficiencySummary> cases) {
    ReviewEfficiencyCount count = new ReviewEfficiencyCount();
    count.setTotalNum(cases.stream().filter(x -> x.getReview()
        && !x.getTestResult().isCanceled()).count());
    assembleReviewEfficiencyOverview0(cases, count);
    return count;
  }

  private static void assembleReviewEfficiencyOverview0(List<? extends FuncCaseEfficiencySummary> cases,
      ReviewEfficiencyCountBase totalOverview) {
    totalOverview.setPassedReviewNum(
        cases.stream().filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed())
            .count());
    totalOverview.setPassedReviewRate(totalOverview.calcPassedReviewRate());
    totalOverview.setOneTimePassedReviewNum(
        cases.stream().filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed()
            && x.getReviewFailNum() == 0).count());
    totalOverview.setOneTimePassedReviewRate(totalOverview.calcOneTimePassedReviewRate());
    totalOverview.setTwoTimePassedReviewNum(
        cases.stream().filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed()
            && x.getReviewFailNum() == 1).count());
    totalOverview.setTwoTimePassedReviewRate(totalOverview.calcTwoTimePassedReviewRate());
    totalOverview.setOneTimeNotPassedReviewNum(
        cases.stream().filter(x -> nonNull(x.getReviewStatus()) && x.getReviewStatus().isPassed()
            && x.getTestFailNum() > 0).count());
    totalOverview.setOneTimeNotPassedReviewRate(totalOverview.calcOneTimeNotPassedReviewRate());
  }

  public static void assembleWorkload(List<? extends FuncCaseEfficiencySummary> cases,
      EfficiencyCaseCountOverview totalOverview) {
    assembleWorkload0(cases, totalOverview);
    List<? extends FuncCaseEfficiencySummary> completedCases = cases.stream()
        .filter(x -> x.getTestResult().isPassed() && nonNull(x.getTestResultHandleDate()))
        .sorted(Comparator.comparing(FuncCaseEfficiencySummary::getTestResultHandleDate))
        .toList();
    long processedDays = calcProcessedDays(completedCases);
    double dailyProcessedWorkload = calcDailyProcessedWorkload(completedCases, processedDays);
    totalOverview.setDailyProcessedWorkload(dailyProcessedWorkload);
  }

  public static void assembleWorkload0(List<? extends FuncCaseEfficiencySummary> cases,
      WorkloadCountBase totalOverview) {
    totalOverview.setEvalWorkload(cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setActualWorkload(cases.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getActualWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setCompletedWorkload(cases.stream().filter(x -> x.getTestResult().isPassed())
        .map(FuncCaseEfficiencySummary::getActualWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setCompletedWorkloadRate(totalOverview.calcCompletedWorkloadRate());
    totalOverview.setSavingWorkload(
        totalOverview.getEvalWorkload() - totalOverview.getActualWorkload());
    totalOverview.setSavingWorkloadRate(totalOverview.calcSavingWorkloadRate());
    totalOverview.setInvalidWorkload(cases.stream().filter(x -> x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setInvalidWorkloadRate(totalOverview.calcInvalidWorkloadRate());
  }

  public static CoreKpiCount assembleCaseCoreKpiOverview(
      List<? extends FuncCaseEfficiencySummary> tasks) {
    CoreKpiCount count = new CoreKpiCount();
    count.setTotalNum(tasks.stream().filter(x -> !x.getTestResult().isCanceled()).count());
    count.setCompletedNum(tasks.stream().filter(x -> x.getTestResult().isPassed()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getTotalNum()));

    count.setEvalWorkload(tasks.stream().filter(x -> !x.getTestResult().isCanceled())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkload(tasks.stream().filter(x -> x.getTestResult().isPassed())
        .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));

    count.setOverdueNum(tasks.stream().filter(
            x -> nonNull(x.getOverdue()) && x.getOverdue() && !x.getTestResult().isCanceled())
        .count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getTotalNum()));
    count.setCompletedOverdueNum(tasks.stream().filter(
            x -> nonNull(x.getOverdue()) && x.getOverdue() && x.getTestResult().isPassed())
        .count());
    count.setCompletedOverdueRate(calcRate(count.getCompletedOverdueNum(), count.getOverdueNum()));

    count.setReviewNum(
        tasks.stream().filter(x -> nonNull(x.getReview()) && x.getReview()).count());
    count.setReviewRate(calcRate(count.getReviewNum(), count.getTotalNum()));
    count.setCompletedReviewNum(tasks.stream().filter(
            x -> nonNull(x.getReview()) && x.getReview() && x.getReviewStatus().isPassed())
        .count());
    count.setCompletedReviewRate(calcRate(count.getCompletedReviewNum(), count.getReviewNum()));
    return count;
  }

  public static void assembleLeadTime(List<? extends FuncCaseEfficiencySummary> cases,
      LeadTimeCountBase totalOverview) {
    List<? extends FuncCaseEfficiencySummary> completedCases = cases.stream()
        .filter(x -> x.getTestResult().isPassed() && nonNull(x.getCreatedDate())
            && nonNull(x.getTestResultHandleDate())).toList();
    int userNum = cases.stream().map(FuncCaseEfficiencySummary::getTesterId).collect(
        Collectors.toSet()).size();
    totalOverview.setUserNum(userNum);

    if (isNotEmpty(completedCases)) {
      ListStatistics scores = new ListStatistics();
      for (FuncCaseEfficiencySummary completedCase : completedCases) {
        scores.addValue(calcWorkingHours(completedCase.getCreatedDate(),
            completedCase.getTestResultHandleDate()).doubleValue());
      }
      totalOverview.setUserAvgProcessingTime(formatDouble(scores.getSum() / userNum, "0.0"));
      totalOverview.setTotalProcessingTime(formatDouble(scores.getSum(), "0.0"));
      totalOverview.setAvgProcessingTime(formatDouble(scores.getMean(), "0.0"));
      totalOverview.setMinProcessingTime(formatDouble(scores.getMin(), "0.0"));
      totalOverview.setMaxProcessingTime(formatDouble(scores.getMax(), "0.0"));
      totalOverview.setP50ProcessingTime(formatDouble(scores.getPercentile(50), "0.0"));
      totalOverview.setP75ProcessingTime(formatDouble(scores.getPercentile(75), "0.0"));
      totalOverview.setP90ProcessingTime(formatDouble(scores.getPercentile(90), "0.0"));
      totalOverview.setP95ProcessingTime(formatDouble(scores.getPercentile(95), "0.0"));
      totalOverview.setP99ProcessingTime(formatDouble(scores.getPercentile(99), "0.0"));
    }
  }

  public static void assembleUnplanned(List<? extends FuncCaseEfficiencySummary> cases,
      UnplannedWorkCountBase totalOverview, double dailyProcessedWorkload) {
    List<? extends FuncCaseEfficiencySummary> unplannedTasks = cases.stream()
        .filter(x -> !x.getTestResult().isCanceled() && x.getUnplanned())
        .toList();
    if (isNotEmpty(unplannedTasks)) {
      totalOverview.setUnplannedNum(unplannedTasks.size());
      totalOverview.setUnplannedRate(totalOverview.calcUnplannedRate());
      totalOverview.setUnplannedCompletedNum(unplannedTasks.stream()
          .filter(x -> x.getTestResult().isPassed()).count());
      totalOverview.setUnplannedCompletedRate(totalOverview.calcUnplannedCompletedRate());
      totalOverview.setUnplannedWorkload(unplannedTasks.stream()
          .map(FuncCaseEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue());
      totalOverview.setUnplannedWorkloadRate(totalOverview.calcUnplannedWorkloadRate());
      totalOverview.setUnplannedWorkloadCompleted(unplannedTasks.stream()
          .filter(x -> x.getTestResult().isPassed()).map(FuncCaseEfficiencySummary::getEvalWorkload)
          .reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue());
      totalOverview.setUnplannedWorkloadCompletedRate(
          totalOverview.calcUnplannedWorkloadCompletedRate());
      totalOverview.setDailyProcessedWorkload(formatDouble(dailyProcessedWorkload, "0.0"));
      double safeDailyProcessedWorkload = dailyProcessedWorkload <= 0
          ? DEFAULT_DAILY_WORKLOAD : dailyProcessedWorkload;
      double overDays = totalOverview.getUnplannedWorkload() / safeDailyProcessedWorkload;
      totalOverview.setUnplannedWorkloadProcessingTime(
          formatDouble(overDays * WEEKLY_WORKING_HOURS, "0.0")/*To hours*/);
    }
  }

  public static void assembleTestHits(int caseNum, List<CaseTestHit> testHits,
      TestCaseHitCount totalOverview) {
    totalOverview.setTestCaseHitNum(isNotEmpty(testHits) ?
        testHits.stream().map(CaseTestHit::getBugId).collect(Collectors.toSet()).size() : 0);
    totalOverview.setTestCaseHitRate(calcRate(totalOverview.getTestCaseHitNum(), caseNum));
  }

  public static GrowthTrendCount assembleGrowthTrendCount(List<FuncCaseEfficiencySummary> cases) {
    GrowthTrendCount count = new GrowthTrendCount();
    count.setTotalNum(cases.size());
    count.getTimeSeries().addAll(getTimeSeriesByFormat(cases, DEFAULT_DAY_FORMAT));
    return count;
  }

  public static @NotNull GrowthTrendDetail assembleGrowthTrendDetail(
      String name, GrowthTrendCount count) {
    GrowthTrendDetail detail = new GrowthTrendDetail();
    detail.setName(name);
    CoreUtils.copyProperties(count, detail, "timeSeries");
    detail.setTimeSeriesDetail(count.getTimeSeries().stream().map(DataAssetsTimeSeries::getValue)
        .toList());
    return detail;
  }

  public static ResourceCreationCount assembleResourceCreationCount(
      List<FuncPlan> plans, List<FuncCaseEfficiencySummary> cases, List<FuncReview> reviews,
      List<FuncBaseline> baselines) {
    ResourceCreationCount total = new ResourceCreationCount();
    total.setPlanNum(plans.size());
    total.setCaseNum(cases.size());
    total.setReviewNum(reviews.size());
    total.setBaselineNum(baselines.size());
    total.setTotalNum(total.getPlanNum() + total.getCaseNum() + total.getReviewNum()
        + total.getBaselineNum() + total.getAnalysisNum());
    total.getTimeSeries().put("PLAN", getTimeSeriesByFormat(plans, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("CASE", getTimeSeriesByFormat(cases, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("REVIEW", getTimeSeriesByFormat(reviews, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("BASELINE", getTimeSeriesByFormat(baselines, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("ANALYSIS", getTimeSeriesByFormat(plans, DEFAULT_DAY_FORMAT));
    Map<String, List<DataAssetsTimeSeries>> totalTs = total.getTimeSeries().values().stream()
        .flatMap(Collection::stream).toList()
        .stream().collect(Collectors.groupingBy(DataAssetsTimeSeries::getTimeSeries));
    Map<String, Integer> sortedTotalTs = new TreeMap<>();
    for (Entry<String, List<DataAssetsTimeSeries>> entry : totalTs.entrySet()) {
      sortedTotalTs.put(entry.getKey(),
          entry.getValue().stream().map(DataAssetsTimeSeries::getValue).reduce(Integer::sum)
              .orElse(0));
    }
    List<DataAssetsTimeSeries> finalTotalTs = sortedTotalTs.entrySet().stream()
        .map(x -> new DataAssetsTimeSeries(x.getKey(), x.getValue()))
        .toList();
    total.getTimeSeries().put("TOTAL", finalTotalTs);
    return total;
  }

  public static ResourceCreationDetail assembleResourceCreationDetail(
      String name, ResourceCreationCount count, List<DataAssetsTimeSeries> allTimeSeries) {
    ResourceCreationDetail detail = new ResourceCreationDetail();
    detail.setName(name);
    CoreUtils.copyProperties(count, detail, "timeSeries");
    List<String> timeSeriesDetails = new ArrayList<>();
    Map<String, Integer> planMap = count.getTimeSeries()
        .getOrDefault("PLAN", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> caseMap = count.getTimeSeries()
        .getOrDefault("CASE", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> reviewMap = count.getTimeSeries()
        .getOrDefault("REVIEW", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> baselineMap = count.getTimeSeries()
        .getOrDefault("BASELINE", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> analysisMap = count.getTimeSeries()
        .getOrDefault("ANALYSIS", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> totalMap = count.getTimeSeries()
        .getOrDefault("TOTAL", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    StringBuilder ts;
    for (DataAssetsTimeSeries time : allTimeSeries) {
      ts = new StringBuilder();
      ts.append(planMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(caseMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(reviewMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(baselineMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(analysisMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(totalMap.getOrDefault(time.getTimeSeries(), 0));
      timeSeriesDetails.add(ts.toString());
    }
    detail.setTimeSeriesDetail(timeSeriesDetails);
    return detail;
  }

  public static void assembleGroupOverview(List<FuncCaseEfficiencySummary> cases,
      EfficiencyCaseOverview overview) {
    Map<Priority, Integer> casePriorityMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getPriority)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (Priority value : Priority.values()) {
      overview.getTotalPriorityCount().put(value, casePriorityMap.getOrDefault(value, 0));
    }

    Map<CaseTestResult, Integer> testResultMap = cases.stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTestResult)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (CaseTestResult value : CaseTestResult.values()) {
      overview.getTotalTestResultCount().put(value, testResultMap.getOrDefault(value, 0));
    }
  }

  public static void assembleGroupOverview(Entry<Long, List<FuncCaseEfficiencySummary>> entry,
      EfficiencyCaseTesterOverview testerOverview) {
    Map<Priority, Integer> casePriorityMap = entry.getValue().stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getPriority)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (Priority value : Priority.values()) {
      testerOverview.getPriorityOverview().put(value, casePriorityMap.getOrDefault(value, 0));
    }

    Map<CaseTestResult, Integer> testResultMap = entry.getValue().stream()
        .collect(Collectors.groupingBy(FuncCaseEfficiencySummary::getTestResult)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (CaseTestResult value : CaseTestResult.values()) {
      testerOverview.getTestResultOverview().put(value, testResultMap.getOrDefault(value, 0));
    }
  }


  public static void assembleTesterRanking(EfficiencyCaseOverview overview,
      EfficiencyCaseRanking testerRanking) {
    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getValidCaseNum(),
            o1.getStatusOverview().getValidCaseNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getValidCaseNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getValidCaseNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getPassedTestNum(),
            o1.getStatusOverview().getPassedTestNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getPassedTestNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getPassedTestNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getProgress(),
            o1.getStatusOverview().getProgress()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getPassedTestRateRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getProgress()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getActualWorkload(),
            o1.getStatusOverview().getActualWorkload()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getActualWorkloadRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getActualWorkload()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getCompletedWorkload(),
            o1.getStatusOverview().getCompletedWorkload()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getCompletedWorkloadRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getCompletedWorkload()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getSavingWorkload(),
            o1.getStatusOverview().getSavingWorkload()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getSavingWorkloadRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getSavingWorkload()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getOverdueNum(),
            o1.getStatusOverview().getOverdueNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOverdueNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOverdueNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getOverdueRate(),
            o1.getStatusOverview().getOverdueRate()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOverdueRateRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOverdueRate()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getOneTimePassedNum(),
            o1.getStatusOverview().getOneTimePassedNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimePassedTestNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimePassedNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getOneTimePassedRate(),
            o1.getStatusOverview().getOneTimePassedRate()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimePassedTestRateRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimePassedRate()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getOneTimeNotPassedNum(),
            o1.getStatusOverview().getOneTimeNotPassedNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimeUnPassedTestNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimeNotPassedNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(o2.getStatusOverview().getOneTimeNotPassedRate(),
            o1.getStatusOverview().getOneTimeNotPassedRate()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimeUnPassedTestRateRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimeNotPassedRate()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Long.compare(o2.getStatusOverview().getOneTimePassedReviewNum(),
            o1.getStatusOverview().getOneTimePassedReviewNum()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimePassedReviewNumRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimePassedReviewNum()));
    }

    overview.getTesterOverview().sort(
        (o1, o2) -> Double.compare(
            o2.getStatusOverview().getOneTimePassedReviewRate(),
            o1.getStatusOverview().getOneTimePassedReviewRate()));
    for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
      testerRanking.getOneTimePassedReviewRateRank().add(rank(testerOverview.getTesterId(),
          testerOverview.getStatusOverview().getOneTimePassedReviewRate()));
    }
  }

}
