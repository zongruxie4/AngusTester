package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.angus.api.commonlink.TesterConstant.WEEKLY_WORKING_HOURS;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.calcDailyProcessedWorkload;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.calcProcessedDays;
import static cloud.xcan.angus.core.tester.application.query.kanban.impl.KanbanDataAssetsQueryImpl.getTimeSeriesByFormat;
import static cloud.xcan.angus.core.tester.domain.kanban.AssigneeRanking.rank;
import static cloud.xcan.angus.core.tester.domain.task.TaskStatus.CANCELED;
import static cloud.xcan.angus.core.tester.domain.task.TaskStatus.COMPLETED;
import static cloud.xcan.angus.core.tester.domain.task.TaskStatus.CONFIRMING;
import static cloud.xcan.angus.core.tester.domain.task.TaskStatus.IN_PROGRESS;
import static cloud.xcan.angus.core.tester.domain.task.TaskStatus.PENDING;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.formatDouble;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.calcWorkingHours;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskAssigneeOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskCountOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskRanking;
import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.count.BugCount;
import cloud.xcan.angus.core.tester.domain.task.count.BugCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.BugDetail;
import cloud.xcan.angus.core.tester.domain.task.count.CoreKpiCount;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentDetail;
import cloud.xcan.angus.core.tester.domain.task.count.GrowthTrendCount;
import cloud.xcan.angus.core.tester.domain.task.count.GrowthTrendDetail;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.ProcessingEfficiencyCount;
import cloud.xcan.angus.core.tester.domain.task.count.ProcessingEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.ResourceCreationCount;
import cloud.xcan.angus.core.tester.domain.task.count.ResourceCreationDetail;
import cloud.xcan.angus.core.tester.domain.task.count.TesterSubmittedBugCount;
import cloud.xcan.angus.core.tester.domain.task.count.TesterSubmittedBugDetail;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadCountBase;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskEfficiencySummary;
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

public class KanbanEfficiencyTaskConverter {

  public static void assembleTaskTotalOverview(List<TaskEfficiencySummary> tasks,
      EfficiencyTaskOverview overview) {
    EfficiencyTaskCountOverview totalOverview = assembleTaskCountOverview(tasks);
    overview.setTotalOverview(totalOverview);
    assembleGroupOverview(tasks, overview);
  }

  public static void assembleAssigneeOverview(List<TaskEfficiencySummary> tasks,
      EfficiencyTaskOverview overview) {
    Map<Long, List<TaskEfficiencySummary>> assigneeGroup = tasks.stream()
        .filter(x -> nonNull(x.getAssigneeId()))
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getAssigneeId));
    for (Entry<Long, List<TaskEfficiencySummary>> entry : assigneeGroup.entrySet()) {
      EfficiencyTaskAssigneeOverview assigneeOverview = new EfficiencyTaskAssigneeOverview();

      assembleAssigneeOverview(entry, assigneeOverview);

      overview.getAssigneeOverview().add(assigneeOverview);
    }
  }

  public static void assembleAssigneeOverview(Entry<Long, List<TaskEfficiencySummary>> entry,
      EfficiencyTaskAssigneeOverview assigneeOverview) {
    assigneeOverview.setAssigneeId(entry.getKey());

    EfficiencyTaskCountOverview assigneeTotalOverview = assembleTaskCountOverview(entry.getValue());
    assigneeOverview.setTotalOverview(assigneeTotalOverview);

    assembleGroupOverview(entry.getValue(), assigneeOverview);
  }

  public static EfficiencyTaskCountOverview assembleTaskCountOverview(
      List<? extends TaskEfficiencySummary> tasks) {
    EfficiencyTaskCountOverview totalOverview = new EfficiencyTaskCountOverview();
    // Total
    totalOverview.setTotalTaskNum(tasks.size());
    totalOverview.setValidTaskNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    // Status
    assembleStatusOverview(tasks, totalOverview);
    // Overdue
    assembleOverdueOverview(tasks, totalOverview);
    // Processing efficiency
    assembleProcessingEfficiency(tasks, totalOverview);
    // Workload
    assembleWorkload(tasks, totalOverview);
    // Bug
    assembleBugCount(tasks, totalOverview);
    // Failure (Bug Handling Times)
    assembleFailure(tasks, totalOverview);
    // Lead Time
    assembleLeadTime(tasks, totalOverview);
    // Unplanned(exclude bug)
    assembleUnplanned(tasks, totalOverview, totalOverview.getDailyProcessedWorkload());
    return totalOverview;
  }

  public static void assembleStatusOverview(List<? extends TaskEfficiencySummary> tasks,
      EfficiencyTaskCountOverview totalOverview) {
    Map<TaskStatus, List<TaskEfficiencySummary>> statusGroup = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getStatus));
    totalOverview.setPendingNum(statusGroup.getOrDefault(PENDING, emptyList()).size());
    totalOverview.setInProgressNum(statusGroup.getOrDefault(IN_PROGRESS, emptyList()).size());
    totalOverview.setConfirmingNum(statusGroup.getOrDefault(CONFIRMING, emptyList()).size());
    totalOverview.setCompletedNum(statusGroup.getOrDefault(COMPLETED, emptyList()).size());
    totalOverview.setCanceledNum(statusGroup.getOrDefault(CANCELED, emptyList()).size());
  }

  public static void assembleOverdueOverview(List<? extends TaskEfficiencySummary> tasks,
      EfficiencyTaskCountOverview totalOverview) {
    totalOverview.setOverdueNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()
        && nonNull(x.getOverdue()) && x.getOverdue()).count());
    totalOverview.setOverdueRate(totalOverview.calcOverdueRate());
  }

  public static void assembleProcessingEfficiency(List<? extends TaskEfficiencySummary> tasks,
      EfficiencyTaskCountOverview totalOverview) {
    assembleProcessingEfficiencyOverview0(tasks, totalOverview);

    totalOverview.setProgress(totalOverview.getCompletedRate());
  }

  public static ProcessingEfficiencyCount assembleProcessingEfficiencyOverview(
      List<? extends TaskEfficiencySummary> tasks) {
    ProcessingEfficiencyCount count = new ProcessingEfficiencyCount();
    count.setTotalNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    assembleProcessingEfficiencyOverview0(tasks, count);
    return count;
  }

  public static void assembleProcessingEfficiencyOverview0(
      List<? extends TaskEfficiencySummary> tasks, ProcessingEfficiencyCountBase totalOverview) {
    totalOverview.setCompletedNum(tasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    totalOverview.setCompletedRate(totalOverview.calcComplatedRate());
    totalOverview.setOneTimePassedNum(
        tasks.stream().filter(x -> x.getStatus().isCompleted() && x.getFailNum() == 0).count());
    totalOverview.setOneTimePassedRate(totalOverview.calcOneTimePassedRate());
    totalOverview.setTwoTimePassedNum(
        tasks.stream().filter(x -> x.getStatus().isCompleted() && x.getFailNum() == 1).count());
    totalOverview.setTwoTimePassedRate(totalOverview.calcTwoTimePassedRate());
    totalOverview.setOneTimeNotPassedNum(
        tasks.stream().filter(x -> x.getStatus().isCompleted() && x.getFailNum() > 0).count());
    totalOverview.setOneTimeNotPassedRate(totalOverview.calcOneTimeNotPassedRate());
  }

  public static void assembleWorkload(List<? extends TaskEfficiencySummary> tasks,
      EfficiencyTaskCountOverview totalOverview) {
    assembleWorkload0(tasks, totalOverview);
    List<TaskEfficiencySummary> completedTasks = tasks.stream()
        .filter(x -> x.getStatus().isCompleted()
            && nonNull(x.getStartDate()) && nonNull(x.getCompletedDate()))
        .sorted(Comparator.comparing(TaskEfficiencySummary::getCompletedDate))
        .collect(Collectors.toList());
    long processedDays = calcProcessedDays(completedTasks);
    double dailyProcessedWorkload = calcDailyProcessedWorkload(completedTasks, processedDays);
    totalOverview.setDailyProcessedWorkload(dailyProcessedWorkload);
  }

  public static void assembleWorkload0(List<? extends TaskEfficiencySummary> tasks,
      WorkloadCountBase totalOverview) {
    totalOverview.setEvalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setActualWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getActualWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setCompletedWorkload(tasks.stream().filter(x -> x.getStatus().isCompleted())
        .map(TaskEfficiencySummary::getActualWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setCompletedWorkloadRate(totalOverview.calcCompletedWorkloadRate());
    totalOverview.setSavingWorkload(
        totalOverview.getEvalWorkload() - totalOverview.getActualWorkload());
    totalOverview.setSavingWorkloadRate(totalOverview.calcSavingWorkloadRate());
    totalOverview.setInvalidWorkload(tasks.stream().filter(x -> x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    totalOverview.setInvalidWorkloadRate(totalOverview.calcInvalidWorkloadRate());
  }

  public static CoreKpiCount assembleTaskCoreKpiOverview(
      List<? extends TaskEfficiencySummary> tasks) {
    CoreKpiCount count = new CoreKpiCount();
    count.setTotalNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    count.setCompletedNum(tasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getTotalNum()));

    count.setEvalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkload(tasks.stream().filter(x -> x.getStatus().isCompleted())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));

    count.setOverdueNum(tasks.stream().filter(
            x -> nonNull(x.getOverdue()) && x.getOverdue() && !x.getStatus().isCanceled())
        .count());
    count.setOverdueRate(calcRate(count.getOverdueNum(), count.getTotalNum()));
    count.setCompletedOverdueNum(tasks.stream().filter(
            x -> nonNull(x.getOverdue()) && x.getOverdue() && x.getStatus().isCompleted())
        .count());
    count.setCompletedOverdueRate(calcRate(count.getCompletedOverdueNum(), count.getOverdueNum()));

    count.setBugNum(
        tasks.stream().filter(x -> x.getTaskType().isBug() && !x.getStatus().isCanceled()).count());
    count.setBugRate(calcRate(count.getBugNum(), count.getTotalNum()));
    count.setCompletedBugNum(
        tasks.stream().filter(x -> x.getTaskType().isBug() && x.getStatus().isCompleted()).count());
    count.setCompletedBugRate(calcRate(count.getCompletedBugNum(), count.getBugNum()));
    return count;
  }

  public static TesterSubmittedBugCount assembleTesterBugOverview(
      List<? extends TaskEfficiencySummary> tasks) {
    TesterSubmittedBugCount bugCount = new TesterSubmittedBugCount();
    bugCount.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    bugCount.setTotalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    assembleBugCount(tasks, bugCount);
    return bugCount;
  }

  public static BugCount assembleBugOverview(List<? extends TaskEfficiencySummary> tasks) {
    BugCount bugCount = new BugCount();
    bugCount.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    bugCount.setTotalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    assembleBugCount(tasks, bugCount);
    return bugCount;
  }

  public static void assembleBugCount(List<? extends TaskEfficiencySummary> tasks,
      BugCountBase totalOverview) {
    totalOverview.setBugNum(tasks.stream().filter(x -> x.getTaskType().isBug()).count());
    totalOverview.setBugRate(totalOverview.calcBugRate());
    List<? extends TaskEfficiencySummary> validBugs = tasks.stream()
        .filter(x -> x.getTaskType().isBug() && !x.getStatus().isCanceled())
        .collect(Collectors.toList());
    if (isNotEmpty(validBugs)) {
      long validBugNum = validBugs.size();
      totalOverview.setValidBugNum(validBugNum);
      totalOverview.setValidBugRate(totalOverview.calcValidBugRate());
      totalOverview.setInvalidBugNum(tasks.stream()
          .filter(x -> x.getTaskType().isBug() && x.getStatus().isCanceled()).count());
      totalOverview.setInvalidBugRate(totalOverview.calcInvalidBugRate());
      totalOverview.setBugWorkload(validBugs.stream()
          .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue());
      totalOverview.setBugWorkloadRate(totalOverview.calcBugWorkloadRate());
      totalOverview.setMissingBugNum(validBugs.stream()
          .filter(x -> nonNull(x.getMissingBug()) && x.getMissingBug()).count());
      totalOverview.setMissingBugRate(totalOverview.calcMissingBugRate());
      totalOverview.setOneTimePassedBugNum(validBugs.stream()
          .filter(x -> x.getStatus().isCompleted() && x.getFailNum() == 0).count());
      totalOverview.setTotalPassedBugNum(tasks.stream().filter(x -> x.getTaskType().isBug()
          && x.getStatus().isCompleted()).count());
      totalOverview.setOneTimePassedBugRate(totalOverview.calcOneTimePassedBugRate());

      Map<BugLevel, Integer> bugLevelMap = validBugs.stream()
          .collect(Collectors.groupingBy(TaskEfficiencySummary::getBugLevel)).entrySet()
          .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
      for (BugLevel value : BugLevel.values()) {
        int bugLevelNum = bugLevelMap.getOrDefault(value, 0);
        totalOverview.getBugLevelCount().put(value, bugLevelNum);
        totalOverview.getBugLevelRate().put(value, calcRate(bugLevelNum, validBugNum));
      }
    }
  }

  public static BugDetail assembleBugDetail(String name, BugCount assignee, BugCount count) {
    BugDetail detail = new BugDetail();
    detail.setName(name);
    CoreUtils.copyProperties(assignee, detail);
    for (Entry<BugLevel, Integer> entry : count.getBugLevelCount().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          detail.setCriticalNum(entry.getValue());
          break;
        case MAJOR:
          detail.setMajorNum(entry.getValue());
          break;
        case MINOR:
          detail.setMinorNum(entry.getValue());
          break;
        case TRIVIAL:
          detail.setTrivialNum(entry.getValue());
      }
    }
    for (Entry<BugLevel, Double> entry : count.getBugLevelRate().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          detail.setCriticalRate(entry.getValue());
          break;
        case MAJOR:
          detail.setMajorRate(entry.getValue());
          break;
        case MINOR:
          detail.setMinorRate(entry.getValue());
          break;
        case TRIVIAL:
          detail.setTrivialRate(entry.getValue());
      }
    }
    return detail;
  }

  public static @NotNull TesterSubmittedBugDetail assembleTesterSubmittedBugDetail(
      String name, TesterSubmittedBugCount total) {
    TesterSubmittedBugDetail totalDetail = new TesterSubmittedBugDetail();
    totalDetail.setName(name);
    CoreUtils.copyProperties(total, totalDetail);
    for (Entry<BugLevel, Integer> entry : total.getBugLevelCount().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          totalDetail.setCriticalNum(entry.getValue());
          break;
        case MAJOR:
          totalDetail.setMajorNum(entry.getValue());
          break;
        case MINOR:
          totalDetail.setMinorNum(entry.getValue());
          break;
        case TRIVIAL:
          totalDetail.setTrivialNum(entry.getValue());
      }
    }
    for (Entry<BugLevel, Double> entry : total.getBugLevelRate().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          totalDetail.setCriticalRate(entry.getValue());
          break;
        case MAJOR:
          totalDetail.setMajorRate(entry.getValue());
          break;
        case MINOR:
          totalDetail.setMinorRate(entry.getValue());
          break;
        case TRIVIAL:
          totalDetail.setTrivialRate(entry.getValue());
      }
    }
    return totalDetail;
  }

  public static void assembleFailure(List<? extends TaskEfficiencySummary> tasks,
      FailureAssessmentCountBase totalOverview) {
    List<? extends TaskEfficiencySummary> validBugs = tasks.stream()
        .filter(x -> x.getTaskType().isBug() && !x.getStatus().isCanceled())
        .collect(Collectors.toList());
    if (isNotEmpty(validBugs)) {
      int failureNum = validBugs.stream().map(TaskEfficiencySummary::getActualTotalNum)
          .reduce(0, Integer::sum);
      totalOverview.setBugNum(validBugs.size());
      totalOverview.setFailureNum(failureNum);
      // Do need to multiply the number of failure ???
      totalOverview.setFailureWorkload(validBugs.stream()
          .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue());
      totalOverview.setOneTimeFailureNum(
          validBugs.stream().filter(x -> x.getStatus().isCompleted() && x.getActualTotalNum() == 1)
              .count());
      totalOverview.setOneTimeFailureRate(totalOverview.calcOneTimeFailureRate());
      totalOverview.setTwoTimeFailureNum(
          validBugs.stream().filter(x -> x.getStatus().isCompleted() && x.getActualTotalNum() == 2)
              .count());
      totalOverview.setTwoTimeFailureRate(totalOverview.calcTwoTimeFailureRate());
      totalOverview.setFailureCompletedNum(
          validBugs.stream().filter(x -> x.getStatus().isCompleted()).count());
      totalOverview.setFailureCompletedRate(totalOverview.calcFailureCompletedRate());
      totalOverview.setFailureOverdueNum(
          validBugs.stream().filter(x -> nonNull(x.getOverdue()) && x.getOverdue())
              .count());
      totalOverview.setFailureOverdueRate(totalOverview.calcFailureOverdueRate());
      ListStatistics scores = new ListStatistics();
      List<TaskEfficiencySummary> validCompletedBugs = validBugs.stream()
          .filter(x -> x.getStatus().isCompleted() && nonNull(x.getStartDate())
              && nonNull(x.getCompletedDate())).collect(Collectors.toList());
      for (TaskEfficiencySummary bug : validCompletedBugs) {
        scores.addValue(
            calcWorkingHours(bug.getStartDate(), bug.getCompletedDate()).doubleValue());
      }
      totalOverview.setFailureTotalTime(formatDouble(scores.getSum(), "0.0"));
      totalOverview.setFailureAvgTime(formatDouble(scores.getMean(), "0.0"));
      totalOverview.setFailureMinTime(formatDouble(scores.getMin(), "0.0"));
      totalOverview.setFailureMaxTime(formatDouble(scores.getMax(), "0.0"));
      Map<BugLevel, List<TaskEfficiencySummary>> bugLevelMap = validBugs.stream()
          .collect(Collectors.groupingBy(TaskEfficiencySummary::getBugLevel));
      for (BugLevel value : BugLevel.values()) {
        if (bugLevelMap.containsKey(value)) {
          int levelFailureNum = bugLevelMap.get(value).stream()
              .map(TaskEfficiencySummary::getActualTotalNum).reduce(0, Integer::sum);
          totalOverview.getFailureLevelCount().put(value, levelFailureNum);
          totalOverview.getFailureLevelRate().put(value, calcRate(levelFailureNum, failureNum));
        } else {
          totalOverview.getFailureLevelCount().put(value, 0);
          totalOverview.getFailureLevelRate().put(value, 0d);
        }
      }
    }
  }

  public static @NotNull FailureAssessmentDetail assembleTaskFailureAssessmentDetail(
      String name, FailureAssessmentCount count) {
    FailureAssessmentDetail detail = new FailureAssessmentDetail();
    detail.setName(name);
    CoreUtils.copyProperties(count, detail);
    for (Entry<BugLevel, Integer> entry : count.getFailureLevelCount().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          detail.setCriticalNum(entry.getValue());
          break;
        case MAJOR:
          detail.setMajorNum(entry.getValue());
          break;
        case MINOR:
          detail.setMinorNum(entry.getValue());
          break;
        case TRIVIAL:
          detail.setTrivialNum(entry.getValue());
      }
    }
    for (Entry<BugLevel, Double> entry : count.getFailureLevelRate().entrySet()) {
      switch (entry.getKey()) {
        case CRITICAL:
          detail.setCriticalRate(entry.getValue());
          break;
        case MAJOR:
          detail.setMajorRate(entry.getValue());
          break;
        case MINOR:
          detail.setMinorRate(entry.getValue());
          break;
        case TRIVIAL:
          detail.setTrivialRate(entry.getValue());
      }
    }
    return detail;
  }

  public static void assembleLeadTime(List<? extends TaskEfficiencySummary> tasks,
      LeadTimeCountBase totalOverview) {
    List<? extends TaskEfficiencySummary> completedTasks = tasks.stream()
        .filter(x -> x.getStatus().isCompleted() && nonNull(x.getStartDate())
            && nonNull(x.getCompletedDate())).collect(Collectors.toList());
    int userNum = tasks.stream().map(TaskEfficiencySummary::getAssigneeId).collect(
        Collectors.toSet()).size();
    totalOverview.setUserNum(userNum);

    if (isNotEmpty(completedTasks)) {
      ListStatistics scores = new ListStatistics();
      for (TaskEfficiencySummary completedTask : completedTasks) {
        scores.addValue(calcWorkingHours(completedTask.getStartDate(),
            completedTask.getCompletedDate()).doubleValue());
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

  public static void assembleUnplanned(List<? extends TaskEfficiencySummary> tasks,
      UnplannedWorkCountBase totalOverview, double dailyProcessedWorkload) {
    List<? extends TaskEfficiencySummary> unplannedTasks = tasks.stream()
        .filter(x -> !x.getStatus().isCanceled() && !x.getTaskType().isBug()
            && x.getUnplanned()).collect(Collectors.toList());
    if (isNotEmpty(unplannedTasks)) {
      totalOverview.setUnplannedNum(unplannedTasks.size());
      totalOverview.setUnplannedRate(totalOverview.calcUnplannedRate());
      totalOverview.setUnplannedCompletedNum(unplannedTasks.stream()
          .filter(x -> x.getStatus().isCompleted()).count());
      totalOverview.setUnplannedCompletedRate(totalOverview.calcUnplannedCompletedRate());
      totalOverview.setUnplannedWorkload(unplannedTasks.stream()
          .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue());
      totalOverview.setUnplannedWorkloadRate(totalOverview.calcUnplannedWorkloadRate());
      totalOverview.setUnplannedWorkloadCompleted(unplannedTasks.stream()
          .filter(x -> x.getStatus().isCompleted()).map(TaskEfficiencySummary::getEvalWorkload)
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

  public static GrowthTrendCount assembleGrowthTrendCount(List<TaskEfficiencySummary> tasks) {
    GrowthTrendCount count = new GrowthTrendCount();
    Map<TaskType, List<TaskEfficiencySummary>> typeTasks = tasks.stream().collect(
        groupingBy(TaskEfficiencySummary::getTaskType));
    count.setRequirementNum(typeTasks.getOrDefault(TaskType.REQUIREMENT, emptyList()).size());
    count.setStoryNum(typeTasks.getOrDefault(TaskType.STORY, emptyList()).size());
    count.setTaskNum(typeTasks.getOrDefault(TaskType.TASK, emptyList()).size());
    count.setBugNum(typeTasks.getOrDefault(TaskType.BUG, emptyList()).size());
    count.setApiTestNum(typeTasks.getOrDefault(TaskType.API_TEST, emptyList()).size());
    count.setScenarioTestNum(
        typeTasks.getOrDefault(TaskType.SCENARIO_TEST, emptyList()).size());
    count.setTotalNum(tasks.size());
    for (TaskType value : TaskType.values()) {
      count.getTimeSeries().put(value.getValue(),
          getTimeSeriesByFormat(typeTasks.getOrDefault(value, emptyList()),
              DEFAULT_DAY_FORMAT));
    }
    count.getTimeSeries().put("TOTAL", getTimeSeriesByFormat(tasks, DEFAULT_DAY_FORMAT));
    return count;
  }

  public static @NotNull GrowthTrendDetail assembleGrowthTrendDetail(
      String name, GrowthTrendCount count, List<DataAssetsTimeSeries> allTimeSeries) {
    GrowthTrendDetail detail = new GrowthTrendDetail();
    detail.setName(name);
    CoreUtils.copyProperties(count, detail, "timeSeries");
    List<String> timeSeriesDetails = new ArrayList<>();
    Map<String, Integer> requirementMap = count.getTimeSeries()
        .getOrDefault(TaskType.REQUIREMENT.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> storyMap = count.getTimeSeries()
        .getOrDefault(TaskType.STORY.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> taskMap = count.getTimeSeries()
        .getOrDefault(TaskType.TASK.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> bugMap = count.getTimeSeries()
        .getOrDefault(TaskType.BUG.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> apiTestMap = count.getTimeSeries()
        .getOrDefault(TaskType.API_TEST.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> sceTestMap = count.getTimeSeries()
        .getOrDefault(TaskType.SCENARIO_TEST.getValue(), emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    Map<String, Integer> totalMap = count.getTimeSeries()
        .getOrDefault("TOTAL", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries,
                DataAssetsTimeSeries::getValue));
    StringBuilder ts;
    for (DataAssetsTimeSeries time : allTimeSeries) {
      ts = new StringBuilder();
      ts.append(requirementMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(storyMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(taskMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(bugMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(apiTestMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(sceTestMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(totalMap.getOrDefault(time.getTimeSeries(), 0));
      timeSeriesDetails.add(ts.toString());
    }
    detail.setTimeSeriesDetail(timeSeriesDetails);
    return detail;
  }

  public static ResourceCreationCount assembleResourceCreationCount(
      List<TaskEfficiencySummary> tasks, List<TaskSprint> sprints, List<TaskMeeting> meetings) {
    ResourceCreationCount total = new ResourceCreationCount();
    total.setBacklogNum(
        tasks.stream().filter(x -> isNull(x.getBacklog()) || x.getBacklog()).count());
    total.setTaskNum(tasks.size() - total.getBacklogNum());
    total.setSprintNum(sprints.size());
    total.setMeetingNum(meetings.size());
    total.setTotalNum(total.getBacklogNum() + total.getTaskNum() + total.getSprintNum()
        + total.getMeetingNum());
    total.getTimeSeries().put("BACKLOG", getTimeSeriesByFormat(
        tasks.stream().filter(x -> isNull(x.getBacklog()) || x.getBacklog())
            .collect(Collectors.toList()), DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("SPRINT", getTimeSeriesByFormat(sprints, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("TASK", getTimeSeriesByFormat(
        tasks.stream().filter(x -> nonNull(x.getBacklog()) && !x.getBacklog())
            .collect(Collectors.toList()), DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("MEETING", getTimeSeriesByFormat(meetings, DEFAULT_DAY_FORMAT));
    total.getTimeSeries().put("ANALYSIS", getTimeSeriesByFormat(sprints, DEFAULT_DAY_FORMAT));
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
        .collect(Collectors.toList());
    total.getTimeSeries().put("TOTAL", finalTotalTs);
    return total;
  }

  public static ResourceCreationDetail assembleResourceCreationDetail(
      String name, ResourceCreationCount count, List<DataAssetsTimeSeries> allTimeSeries) {
    ResourceCreationDetail detail = new ResourceCreationDetail();
    detail.setName(name);
    CoreUtils.copyProperties(count, detail, "timeSeries");
    List<String> timeSeriesDetails = new ArrayList<>();
    Map<String, Integer> backlogMap = count.getTimeSeries()
        .getOrDefault("BACKLOG", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> taskMap = count.getTimeSeries()
        .getOrDefault("TASK", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> sprintMap = count.getTimeSeries()
        .getOrDefault("SPRINT", emptyList()).stream().collect(
            Collectors.toMap(DataAssetsTimeSeries::getTimeSeries, DataAssetsTimeSeries::getValue));
    Map<String, Integer> meetingMap = count.getTimeSeries()
        .getOrDefault("MEETING", emptyList()).stream().collect(
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
      ts.append(backlogMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(sprintMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(taskMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(meetingMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(analysisMap.getOrDefault(time.getTimeSeries(), 0)).append(",")
          .append(totalMap.getOrDefault(time.getTimeSeries(), 0));
      timeSeriesDetails.add(ts.toString());
    }
    detail.setTimeSeriesDetail(timeSeriesDetails);
    return detail;
  }

  public static void assembleGroupOverview(List<TaskEfficiencySummary> tasks,
      EfficiencyTaskOverview overview) {
    Map<TaskStatus, Integer> statusMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getStatus)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (TaskStatus value : TaskStatus.values()) {
      overview.getTotalStatusCount().put(value, statusMap.getOrDefault(value, 0));
    }

    Map<TaskType, Integer> taskTypeMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getTaskType)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (TaskType value : TaskType.values()) {
      overview.getTotalTypeCount().put(value, taskTypeMap.getOrDefault(value, 0));
    }

    Map<Priority, Integer> taskPriorityMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getPriority)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (Priority value : Priority.values()) {
      overview.getTotalPriorityCount().put(value, taskPriorityMap.getOrDefault(value, 0));
    }
  }

  public static void assembleGroupOverview(List<TaskEfficiencySummary> tasks,
      EfficiencyTaskAssigneeOverview overview) {
    Map<TaskStatus, Integer> statusMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getStatus)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (TaskStatus value : TaskStatus.values()) {
      overview.getStatusOverview().put(value, statusMap.getOrDefault(value, 0));
    }

    Map<TaskType, Integer> taskTypeMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getTaskType)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (TaskType value : TaskType.values()) {
      overview.getTypeOverview().put(value, taskTypeMap.getOrDefault(value, 0));
    }

    Map<Priority, Integer> taskPriorityMap = tasks.stream()
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getPriority)).entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> x.getValue().size()));
    for (Priority value : Priority.values()) {
      overview.getPriorityOverview().put(value, taskPriorityMap.getOrDefault(value, 0));
    }
  }

  public static EfficiencyTaskRanking assembleAssigneeRanking(EfficiencyTaskOverview overview) {
    EfficiencyTaskRanking assigneeRanking = new EfficiencyTaskRanking();

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getValidTaskNum(),
            o1.getTotalOverview().getValidTaskNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getValidTaskNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getValidTaskNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getCompletedNum(),
            o1.getTotalOverview().getCompletedNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getCompletedNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getCompletedNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getProgress(),
            o1.getTotalOverview().getProgress()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getCompletedRateRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getProgress()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getActualWorkload(),
            o1.getTotalOverview().getActualWorkload()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getActualWorkloadRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getActualWorkload()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getCompletedWorkload(),
            o1.getTotalOverview().getCompletedWorkload()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getCompletedWorkloadRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getCompletedWorkload()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getSavingWorkload(),
            o1.getTotalOverview().getSavingWorkload()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getSavingWorkloadRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getSavingWorkload()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getOverdueNum(),
            o1.getTotalOverview().getOverdueNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOverdueNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOverdueNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getOverdueRate(),
            o1.getTotalOverview().getOverdueRate()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOverdueRateRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOverdueRate()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getOneTimePassedNum(),
            o1.getTotalOverview().getOneTimePassedNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOneTimePassedNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOneTimePassedNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getOneTimePassedRate(),
            o1.getTotalOverview().getOneTimePassedRate()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOneTimePassedRateRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOneTimePassedRate()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getOneTimeNotPassedNum(),
            o1.getTotalOverview().getOneTimeNotPassedNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOneTimeUnPassedNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOneTimeNotPassedNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getOneTimeNotPassedRate(),
            o1.getTotalOverview().getOneTimeNotPassedRate()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getOneTimeUnPassedRateRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getOneTimeNotPassedRate()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Long.compare(o2.getTotalOverview().getValidBugNum(),
            o1.getTotalOverview().getValidBugNum()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getValidBugNumRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getValidBugNum()));
    }

    overview.getAssigneeOverview().sort(
        (o1, o2) -> Double.compare(o2.getTotalOverview().getValidBugRate(),
            o1.getTotalOverview().getValidBugRate()));
    for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
      assigneeRanking.getValidBugRateRank().add(rank(assigneeOverview.getAssigneeId(),
          assigneeOverview.getTotalOverview().getValidBugRate()));
    }
    return assigneeRanking;
  }

}
