package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DAILY_WORKLOAD;
import static cloud.xcan.angus.api.commonlink.TesterConstant.WEEKLY_WORKING_HOURS;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleFailure;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleLeadTime;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleUnplanned;
import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.formatDouble;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.calcWorkingDays;
import static cloud.xcan.angus.spec.utils.WorkingTimeCalculator.calcWorkingHours;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskAssigneeOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
import cloud.xcan.angus.core.tester.domain.kanban.OverdueRiskLevel;
import cloud.xcan.angus.core.tester.domain.kanban.TotalProgressCount;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskEfficiencySummary;
import cloud.xcan.angus.spec.utils.WorkingTimeCalculator;
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

public class KanbanCtoTaskConverter {

  public static void assembleTaskOverview(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    assembleTotalProgressOverview(tasks, overview);

    assembleBackloggedTaskCount(tasks, overview);

    double dailyProcessedWorkload = overview.getBackloggedCount().getDailyProcessedWorkload();
    assembleOverdueAssessmentCount(tasks, dailyProcessedWorkload, overview);

    assembleRecentDeliveryCount(tasks, overview);

    assembleUnplannedWorkCount(tasks, dailyProcessedWorkload, overview);

    assembleFailureAssessmentCount(tasks, overview);

    assembleLeadTimeCount(tasks, overview);

    assembleGroupOverview(tasks, overview);
  }

  public static void assembleAssigneeOverview(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    Map<Long, List<TaskEfficiencySummary>> assigneeGroup = tasks.stream()
        .filter(x -> nonNull(x.getAssigneeId()))
        .collect(Collectors.groupingBy(TaskEfficiencySummary::getAssigneeId));
    for (Entry<Long, List<TaskEfficiencySummary>> entry : assigneeGroup.entrySet()) {
      CtoTaskAssigneeOverview assigneeOverview = new CtoTaskAssigneeOverview();
      assigneeOverview.setAssigneeId(entry.getKey());
      assembleTaskOverview(entry.getValue(), assigneeOverview);
      overview.getAssigneeOverview().add(assigneeOverview);
    }
  }

  public static void assembleTotalProgressOverview(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    TotalProgressCount progressOverview = new TotalProgressCount();
    if (isEmpty(tasks)){ // Set Default
      overview.setTotalProgressOverview(progressOverview);
      return;
    }

    progressOverview.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    progressOverview.setTotalCompletedNum(
        tasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    progressOverview.setTotalCompletedRate(progressOverview.calcTotalCompletedRate());

    progressOverview.setTotalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    progressOverview.setTotalCompletedWorkload(
        tasks.stream().filter(x -> x.getStatus().isCompleted())
            .map(TaskEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    progressOverview.setTotalCompletedWorkloadRate(
        progressOverview.calcTotalCompletedWorkloadRate());

    progressOverview.setTotalProgress(progressOverview.getTotalCompletedRate());

    overview.setTotalProgressOverview(progressOverview);
  }

  public static ProgressCount assembleTaskProgressCount0(List<TaskEfficiencySummary> tasks) {
    ProgressCount count = new ProgressCount();
    if (isEmpty(tasks)){ // Set Default
      return count;
    }

    count.setTotalNum(tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    count.setCompletedNum(tasks.stream().filter(x -> x.getStatus().isCompleted()).count());
    count.setCompletedRate(calcRate(count.getCompletedNum(), count.getTotalNum()));

    count.setEvalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    count.setCompletedWorkload(
        tasks.stream().filter(x -> x.getStatus().isCompleted())
            .map(TaskEfficiencySummary::getActualWorkload)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    count.setCompletedWorkloadRate(calcRate(count.getCompletedWorkload(), count.getEvalWorkload()));
    return count;
  }

  public static void assembleBackloggedTaskCount(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    BackloggedCount backloggedTaskCount = assembleBackloggedTaskCount0(tasks);
    overview.setBackloggedCount(backloggedTaskCount);
  }

  public static BackloggedCount assembleBackloggedTaskCount0(
      List<TaskEfficiencySummary> tasks) {
    BackloggedCount backloggedTaskCount = new BackloggedCount();
    if (isEmpty(tasks)){ // Set Default
      return backloggedTaskCount;
    }

    long totalTaskNum = tasks.stream().filter(x -> !x.getStatus().isCanceled()).count();
    backloggedTaskCount.setTotalNum(totalTaskNum);
    double totalWorkload = tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
    backloggedTaskCount.setTotalWorkload(totalWorkload);
    // Sort in ascending order by completion time
    List<TaskEfficiencySummary> completedTasks = tasks.stream()
        .filter(x -> x.getStatus().isCompleted()
            && nonNull(x.getStartDate()) && nonNull(x.getCompletedDate()))
        .sorted(Comparator.comparing(TaskEfficiencySummary::getCompletedDate))
        .toList();
    if (isNotEmpty(completedTasks)) {
      long processedDays = calcProcessedDays(completedTasks);
      backloggedTaskCount.setProcessedInDay(processedDays);
      double dailyProcessedWorkload = calcDailyProcessedWorkload(completedTasks, processedDays);
      backloggedTaskCount.setDailyProcessedWorkload(formatDouble(dailyProcessedWorkload, "0.0"));
      long processedNum = completedTasks.size();
      double dailyProcessedNum = processedDays == 0 ? 0 : (double) processedNum / processedDays;
      backloggedTaskCount.setDailyProcessedNum(formatDouble(dailyProcessedNum, "0.0"));
    }

    List<TaskEfficiencySummary> backloggedTasks = tasks.stream()
        .filter(x -> !x.getStatus().isFinished()).toList();
    if (isNotEmpty(backloggedTasks)) {
      long backloggedNum = backloggedTasks.size();
      backloggedTaskCount.setBackloggedNum(backloggedNum);
      backloggedTaskCount.setBackloggedRate(calcRate(backloggedNum, totalTaskNum));
      double backloggedWorkload = backloggedTasks.stream()
          .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
          .doubleValue();
      backloggedTaskCount.setBackloggedWorkload(backloggedWorkload);
      backloggedTaskCount.setBackloggedWorkloadRate(calcRate(backloggedWorkload, totalWorkload));
      double backloggedCompletionTime = backloggedTaskCount.getDailyProcessedWorkload() <= 0 ? 0
          : backloggedWorkload / backloggedTaskCount.getDailyProcessedWorkload();
      backloggedTaskCount.setBackloggedCompletionTime(
          formatDouble(backloggedCompletionTime, "0.0"));
    }
    return backloggedTaskCount;
  }

  public static double calcDailyProcessedWorkload(List<TaskEfficiencySummary> tasks,
      double default0) {
    double dailyProcessedWorkload = default0;
    // Sort in ascending order by completion time
    List<TaskEfficiencySummary> completedTasks = tasks.stream()
        .filter(x -> x.getStatus().isCompleted()
            && nonNull(x.getStartDate()) && nonNull(x.getCompletedDate()))
        .sorted(Comparator.comparing(TaskEfficiencySummary::getCompletedDate))
        .toList();
    if (isNotEmpty(completedTasks)) {
      long processedDays = calcProcessedDays(completedTasks);
      dailyProcessedWorkload = calcDailyProcessedWorkload(completedTasks, processedDays);
    }
    return dailyProcessedWorkload < 0 ? DEFAULT_DAILY_WORKLOAD : dailyProcessedWorkload;
  }

  public static long calcProcessedDays(List<TaskEfficiencySummary> completedSortTasks) {
    TaskEfficiencySummary minStartTask = null;
    for (TaskEfficiencySummary task : completedSortTasks) {
      if (nonNull(task.getStartDate()) // Exclude dirty data
          && (minStartTask == null || task.getStartDate().isBefore(minStartTask.getStartDate()))) {
        minStartTask = task;
      }
    }
    if (isNull(minStartTask)) {
      return 0;
    }
    LocalDateTime startTime = minStartTask.getStartDate();
    LocalDateTime endTime = completedSortTasks.get(completedSortTasks.size() - 1)
        .getCompletedDate();
    return calcWorkingDays(startTime, endTime);
  }

  public static double calcDailyProcessedWorkload(List<TaskEfficiencySummary> completedTasks,
      long processedDays) {
    if (processedDays == 0) {
      return DEFAULT_DAILY_WORKLOAD;
    }
    double processedWorkload = completedTasks.stream()
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
    if (processedWorkload == 0d){
      return DEFAULT_DAILY_WORKLOAD;
    }
    return processedWorkload / processedDays;
  }

  public static void assembleOverdueAssessmentCount(List<TaskEfficiencySummary> tasks,
      double dailyProcessedWorkload, CtoTaskOverview overview) {
    OverdueAssessmentCount overdueAssessmentCount = assembleOverdueAssessmentCount0(
        tasks, dailyProcessedWorkload);
    overview.setOverdueAssessmentCount(overdueAssessmentCount);
  }

  public static @NotNull OverdueAssessmentCount assembleOverdueAssessmentCount0(
      List<TaskEfficiencySummary> tasks, double dailyProcessedWorkload) {
    OverdueAssessmentCount overdueAssessmentCount = new OverdueAssessmentCount();
    if (isEmpty(tasks)){ // Set Default
      return overdueAssessmentCount;
    }

    overdueAssessmentCount.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    overdueAssessmentCount.setTotalWorkload(tasks.stream()
        .filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());

    overdueAssessmentCount.setOverdueNum(tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue()
            && !x.getStatus().isCanceled()).count());
    overdueAssessmentCount.setOverdueRate(overdueAssessmentCount.calcOverdueRate());

    double overdueWorkload = tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue() && !x.getStatus()
            .isCanceled()).map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    overdueAssessmentCount.setOverdueWorkload(overdueWorkload);
    overdueAssessmentCount.setOverdueWorkloadRate(
        overdueAssessmentCount.calcOverdueWorkloadRate());
    overdueAssessmentCount.setDailyProcessedWorkload(formatDouble(dailyProcessedWorkload, "0.0"));

    double overdueHours = tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue()
            && !x.getStatus().isCanceled() && nonNull(x.getDeadlineDate())
            && x.getDeadlineDate().isBefore(LocalDateTime.now()))
        .map(task -> calcWorkingHours(task.getDeadlineDate(), LocalDateTime.now()))
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    overdueAssessmentCount.setOverdueTime(formatDouble(overdueHours, "0.0"));
    double overDays = overdueWorkload / dailyProcessedWorkload <= 0
        ? DEFAULT_DAILY_WORKLOAD : dailyProcessedWorkload;
    overdueAssessmentCount.setOverdueWorkloadProcessingTime(
        formatDouble(overDays * WEEKLY_WORKING_HOURS, "0.0")/*To hours*/);
    overdueAssessmentCount.setRiskLevel(OverdueRiskLevel.calcLevel(overDays/* to days*/));
    return overdueAssessmentCount;
  }

  public static void assembleRecentDeliveryCount(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    Map<String, RecentDeliveryCount> recentDeliveryCount = assembleRecentDeliveryCount0(tasks);
    overview.setRecentDeliveryCount(recentDeliveryCount);
  }

  public static @NotNull Map<String, RecentDeliveryCount> assembleRecentDeliveryCount0(
      List<TaskEfficiencySummary> tasks) {
    Map<String, RecentDeliveryCount> recentDeliveryCount = new HashMap<>();
    if (isEmpty(tasks)){ // Set Default
      recentDeliveryCount.put("today", new RecentDeliveryCount());
      recentDeliveryCount.put("lastWeek", new RecentDeliveryCount());
      recentDeliveryCount.put("lastMonth", new RecentDeliveryCount());
      return recentDeliveryCount;
    }

    long totalNum = tasks.stream().filter(x -> !x.getStatus().isCanceled()).count();
    double totalWorkload = tasks.stream()
        .filter(x -> !x.getStatus().isCanceled()).map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    long totalOverdueNum = tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue()).count();

    RecentDeliveryCount today = assembleRecentDeliveryCount(totalNum, tasks, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isToday);
    RecentDeliveryCount lastWeek = assembleRecentDeliveryCount(totalNum, tasks, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isLastWeek);
    RecentDeliveryCount lastMonth = assembleRecentDeliveryCount(totalNum, tasks, totalWorkload,
        totalOverdueNum, WorkingTimeCalculator::isLastMonth);

    recentDeliveryCount.put("today", today);
    recentDeliveryCount.put("lastWeek", lastWeek);
    recentDeliveryCount.put("lastMonth", lastMonth);
    return recentDeliveryCount;
  }

  public static RecentDeliveryCount assembleRecentDeliveryCount(long totalNum,
      List<TaskEfficiencySummary> tasks, double totalWorkload, long totalOverdueNum,
      Function<LocalDateTime, Boolean> isRecent) {
    RecentDeliveryCount deliveryCount = new RecentDeliveryCount();
    if (isEmpty(tasks)){ // Set Default
      return deliveryCount;
    }

    deliveryCount.setTotalNum(totalNum);
    deliveryCount.setCompletedNum(tasks.stream()
        .filter(x -> x.getStatus().isCompleted() && nonNull(x.getCompletedDate()) && isRecent.apply(
            x.getCompletedDate())).count());
    deliveryCount.setCompletedRate(deliveryCount.calcCompletedRate());

    deliveryCount.setTotalWorkload(totalWorkload);
    deliveryCount.setCompletedWorkload(tasks.stream()
        .filter(x -> x.getStatus().isCompleted() && nonNull(x.getCompletedDate()) && isRecent.apply(
            x.getCompletedDate()))
        .map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setCompletedWorkloadRate(deliveryCount.calcCompletedWorkloadRate());
    deliveryCount.setSavingWorkload(tasks.stream()
        .filter(x -> x.getStatus().isCompleted() && nonNull(x.getCompletedDate()) && isRecent.apply(
            x.getCompletedDate()))
        .map(TaskEfficiencySummary::getSavingWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setSavingWorkloadRate(deliveryCount.calcSavingWorkloadRate());

    deliveryCount.setTotalOverdueNum(totalOverdueNum);
    deliveryCount.setOverdueNum(tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue() && x.getStatus()
            .isCompleted() && nonNull(x.getCompletedDate()) && isRecent.apply(x.getCompletedDate()))
        .count());
    deliveryCount.setOverdueRate(deliveryCount.calcOverdueRate());
    deliveryCount.setOverdueWorkload(tasks.stream()
        .filter(x -> nonNull(x.getOverdue()) && x.getOverdue()
            && x.getStatus().isCompleted() && nonNull(x.getCompletedDate()) && isRecent.apply(
            x.getCompletedDate()))
        .map(TaskEfficiencySummary::getEvalWorkload)
        .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());
    deliveryCount.setOverdueWorkloadRate(deliveryCount.calcOverdueWorkloadRate());
    return deliveryCount;
  }

  public static void assembleUnplannedWorkCount(List<TaskEfficiencySummary> tasks,
      double dailyProcessedWorkload, CtoTaskOverview overview) {
    UnplannedWorkCount unplannedWorkCount = assembleUnplannedWorkCount0(tasks,
        dailyProcessedWorkload);

    overview.setUnplannedWorkCount(unplannedWorkCount);
  }

  public static @NotNull UnplannedWorkCount assembleUnplannedWorkCount0(
      List<TaskEfficiencySummary> tasks, double dailyProcessedWorkload) {
    UnplannedWorkCount unplannedWorkCount = new UnplannedWorkCount();
    if (isEmpty(tasks)){ // Set Default
      return unplannedWorkCount;
    }
    unplannedWorkCount.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    unplannedWorkCount.setTotalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    assembleUnplanned(tasks, unplannedWorkCount, dailyProcessedWorkload);
    return unplannedWorkCount;
  }

  public static void assembleFailureAssessmentCount(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    FailureAssessmentCount failureAssessmentCount = assembleFailureAssessmentCount0(tasks);
    overview.setFailureAssessmentCount(failureAssessmentCount);
  }

  public static @NotNull FailureAssessmentCount assembleFailureAssessmentCount0(
      List<TaskEfficiencySummary> tasks) {
    FailureAssessmentCount failureAssessmentCount = new FailureAssessmentCount();
    if (isEmpty(tasks)){ // Set Default
      return failureAssessmentCount;
    }
    failureAssessmentCount.setTotalNum(
        tasks.stream().filter(x -> !x.getStatus().isCanceled()).count());
    failureAssessmentCount.setTotalWorkload(tasks.stream().filter(x -> !x.getStatus().isCanceled())
        .map(TaskEfficiencySummary::getEvalWorkload).reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue());
    assembleFailure(tasks, failureAssessmentCount);
    return failureAssessmentCount;
  }

  public static void assembleLeadTimeCount(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
    LeadTimeCount leadTimeCount = assembleLeadTimeCount0(tasks);
    overview.setLeadTimeCount(leadTimeCount);
  }

  public static @NotNull LeadTimeCount assembleLeadTimeCount0(
      List<TaskEfficiencySummary> tasks) {
    LeadTimeCount leadTimeCount = new LeadTimeCount();
    if (isEmpty(tasks)){ // Set Default
      return leadTimeCount;
    }
    assembleLeadTime(tasks, leadTimeCount);
    return leadTimeCount;
  }

  public static void assembleGroupOverview(List<TaskEfficiencySummary> tasks,
      CtoTaskOverview overview) {
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
  }

}
