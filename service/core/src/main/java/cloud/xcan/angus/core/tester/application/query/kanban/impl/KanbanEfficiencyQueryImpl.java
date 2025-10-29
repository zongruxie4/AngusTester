package cloud.xcan.angus.core.tester.application.query.kanban.impl;

import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCaseTesterResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleCaseTotalOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleTesterOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyCaseConverter.assembleTesterRanking;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleAssigneeOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleAssigneeRanking;
import static cloud.xcan.angus.core.tester.application.converter.KanbanEfficiencyTaskConverter.assembleTaskTotalOverview;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.getTaskAssigneeResourcesFilter;
import static cloud.xcan.angus.core.tester.application.query.func.impl.FuncCaseQueryImpl.getCaseSummary;
import static cloud.xcan.angus.core.tester.application.query.task.impl.TaskQueryImpl.getTaskSummary;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.kanban.KanbanEfficiencyQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseCountOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseRanking;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseTesterOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskAssigneeOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskCountOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskRanking;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskEfficiencySummary;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of KanbanEfficiencyQuery for managing efficiency dashboard queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing efficiency
 * dashboard data, including task and case efficiency metrics, burn-down charts, and
 * performance rankings. It handles comprehensive efficiency analysis and reporting.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Task efficiency overview with assignee metrics and rankings</li>
 *   <li>Case efficiency overview with tester metrics and rankings</li>
 *   <li>Burn-down chart generation and analysis</li>
 *   <li>Performance ranking and comparison analysis</li>
 *   <li>Test hit analysis and bug correlation</li>
 *   <li>Comprehensive efficiency metrics and statistics</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and efficiency calculations.
 * <p>
 * Supports both individual efficiency analysis and comprehensive dashboard data generation
 * with proper error handling and resource validation.
 */
@Biz
public class KanbanEfficiencyQueryImpl implements KanbanEfficiencyQuery {

  @Resource
  private TaskRepo taskRepo;
  @Resource
  private TaskInfoRepo taskInfoRepo;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private TaskFuncCaseQuery taskFuncCaseQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private JoinSupplier joinSupplier;

  /**
   * Generates comprehensive task efficiency overview with detailed metrics.
   * <p>
   * Provides detailed task efficiency analysis including assignee performance, rankings,
   * burn-down charts, and optional detailed task lists. Integrates multiple data sources
   * for complete efficiency visibility.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param projectId the project ID for filtering tasks (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTaskList whether to include detailed task list
   * @param joinAssigneeOverview whether to include assignee overview details
   * @param joinRanking whether to include assignee ranking analysis
   * @param joinBurnDownChar whether to include burn-down chart analysis
   * @return EfficiencyTaskOverview object with comprehensive task efficiency metrics
   */
  @Override
  public EfficiencyTaskOverview taskEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, @NonNullable Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTaskList,
      boolean joinAssigneeOverview, boolean joinRanking, boolean joinBurnDownChar) {
    return new BizTemplate<EfficiencyTaskOverview>() {

      @Override
      protected EfficiencyTaskOverview process() {
        EfficiencyTaskOverview overview = new EfficiencyTaskOverview();

        // Retrieve project member IDs for filtering
        Set<Long> memberIds = projectMemberQuery.getMemberIds(creatorObjectType,
            creatorObjectId, projectId);

        // Build comprehensive filter criteria for task queries
        // Note: All project data will be queried when creator is null
        Set<SearchCriteria> allFilters = getTaskAssigneeResourcesFilter(projectId, planId,
            createdDateStart, createdDateEnd, isNull(creatorObjectId) || isNull(creatorObjectType)
                ? null : memberIds);

        // Retrieve task efficiency summaries with optimized projection
        List<TaskEfficiencySummary> tasks = taskRepo.findProjectionByFilters(TaskInfo.class,
            TaskEfficiencySummary.class, allFilters);

        // Add members who have tasks but have been removed from the project
        // This ensures complete member coverage for historical data
        memberIds.addAll(tasks.stream().map(TaskEfficiencySummary::getAssigneeId)
            .filter(Objects::nonNull).collect(Collectors.toSet()));

        // Set assignee information for the overview
        overview.setAssignees(commonQuery.getUserInfoMap(memberIds));

        // Handle empty task scenario with default overview
        if (isEmpty(tasks)) {
          overview.setTotalOverview(new EfficiencyTaskCountOverview());
          return overview;
        }

        // Assemble comprehensive task total overview metrics
        assembleTaskTotalOverview(tasks, overview);

        // Optionally include assignee overview details (required for ranking)
        if (joinAssigneeOverview || joinRanking/*be dependent*/) {
          assembleAssigneeOverview(tasks, overview);
        }

        // Optionally include assignee ranking analysis
        if (joinRanking) {
          EfficiencyTaskRanking assigneeRanking = assembleAssigneeRanking(overview);
          overview.setAssigneeRanking(assigneeRanking);
        }

        // Optionally include burn-down chart analysis
        if (joinBurnDownChar) {
          assembleBurnDownChart(overview, projectId, planId, creatorObjectType, creatorObjectId,
              createdDateStart, createdDateEnd);
        }

        // Optionally include detailed task list with summaries
        if (joinTaskList) {
          List<TaskInfo> taskInfos = taskInfoRepo.findAllByFilters(allFilters);
          overview.setTasks(joinSupplier.execute(() -> getTaskSummary(taskInfos)));
        }
        return overview;
      }
    }.execute();
  }

  /**
   * Generates comprehensive case efficiency overview with detailed metrics.
   * <p>
   * Provides detailed case efficiency analysis including tester performance, rankings,
   * burn-down charts, test hit analysis, and optional detailed case lists. Integrates
   * multiple data sources for complete efficiency visibility.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param projectId the project ID for filtering cases (required)
   * @param planId optional plan ID for plan-specific filtering
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param joinTestHit whether to include test hit and bug analysis
   * @param joinCaseList whether to include detailed case list
   * @param joinTesterOverview whether to include tester overview details
   * @param joinRanking whether to include tester ranking analysis
   * @param joinBurnDownChar whether to include burn-down chart analysis
   * @return EfficiencyCaseOverview object with comprehensive case efficiency metrics
   */
  @Override
  public EfficiencyCaseOverview caseEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, @NonNullable Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, boolean joinTestHit,
      boolean joinCaseList, boolean joinTesterOverview, boolean joinRanking,
      boolean joinBurnDownChar) {
    return new BizTemplate<EfficiencyCaseOverview>() {

      @Override
      protected EfficiencyCaseOverview process() {
        EfficiencyCaseOverview overview = new EfficiencyCaseOverview();

        // Retrieve project member IDs for filtering
        Set<Long> memberIds = projectMemberQuery.getMemberIds(creatorObjectType,
            creatorObjectId, projectId);

        // Build comprehensive filter criteria for case queries
        // Note: All project data will be queried when creator is null
        Set<SearchCriteria> allFilters = getCaseTesterResourcesFilter(projectId, planId,
            createdDateStart, createdDateEnd, isNull(creatorObjectId) || isNull(creatorObjectType)
                ? null : memberIds);

        // Retrieve case efficiency summaries with optimized projection
        List<FuncCaseEfficiencySummary> cases = funcCaseRepo.findProjectionByFilters(
            FuncCaseInfo.class, FuncCaseEfficiencySummary.class, allFilters);

        // Add members who have cases but have been removed from the project
        // This ensures complete member coverage for historical data
        memberIds.addAll(cases.stream().map(FuncCaseEfficiencySummary::getTesterId)
            .filter(Objects::nonNull).collect(Collectors.toSet()));

        // Set tester information for the overview
        overview.setTesters(commonQuery.getUserInfoMap(memberIds));

        // Handle empty case scenario with default overview
        if (isEmpty(cases)) {
          overview.setTotalOverview(new EfficiencyCaseCountOverview());
          return overview;
        }

        // Optionally retrieve test hit and bug correlation data
        List<CaseTestHit> testHits = joinTestHit ? taskFuncCaseQuery.findCaseHitBugs(
            cases.stream().map(FuncCaseEfficiencySummary::getId).collect(Collectors.toSet()))
            : null;

        // Assemble comprehensive case total overview metrics
        assembleCaseTotalOverview(cases, testHits, overview);

        // Optionally include tester overview details (required for ranking)
        if (joinTesterOverview || joinRanking/*be dependent*/) {
          assembleTesterOverview(cases, testHits, overview);
        }

        // Optionally include tester ranking analysis
        if (joinRanking) {
          EfficiencyCaseRanking testerRanking = new EfficiencyCaseRanking();
          assembleTesterRanking(overview, testerRanking);

          overview.setTesterRanking(testerRanking);
        }

        // Optionally include burn-down chart analysis
        if (joinBurnDownChar) {
          assembleBurnDownChart(overview, projectId, planId, creatorObjectType, creatorObjectId,
              createdDateStart, createdDateEnd);
        }

        // Optionally include detailed case list with summaries
        if (joinCaseList) {
          List<FuncCaseInfo> caseInfos = funcCaseInfoRepo.findAllByFilters(allFilters);
          overview.setCases(joinSupplier.execute(() -> getCaseSummary(caseInfos)));
        }
        return overview;
      }
    }.execute();
  }

  /**
   * Assembles burn-down chart data for task efficiency overview.
   * <p>
   * Generates comprehensive burn-down chart analysis for task overview including
   * total burn-down charts and individual assignee burn-down charts when appropriate.
   *
   * @param overview the task efficiency overview to populate with burn-down data
   * @param projectId the project ID for burn-down analysis
   * @param planId optional plan ID for plan-specific analysis
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param createdDateStart start date for analysis
   * @param createdDateEnd end date for analysis
   */
  private void assembleBurnDownChart(EfficiencyTaskOverview overview, Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    // Retrieve project information for date range validation
    Project projectDb = projectQuery.checkAndFind(projectId);

    // Generate total burn-down charts for the project
    Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts
        = taskQuery.burndownChart(projectId, planId, creatorObjectType, creatorObjectId,
        nullSafe(createdDateStart, projectDb.getStartDate()),
        nullSafe(createdDateEnd, projectDb.getDeadlineDate()),
        false, false).getTotalBurnDownCharts();
    overview.setBurnDownCharts(burnDownCharts);

    // Generate individual assignee burn-down charts when not filtering by specific user
    if (isNotEmpty(overview.getAssigneeOverview())) {
      for (EfficiencyTaskAssigneeOverview assigneeOverview : overview.getAssigneeOverview()) {
        if (!AuthObjectType.USER.equals(creatorObjectType)) {
          Map<BurnDownResourceType, BurnDownChartCount> assigneeOBurnDownCharts
              = taskQuery.burndownChart(projectId, planId,
              creatorObjectType, assigneeOverview.getAssigneeId(),
              nullSafe(createdDateStart, projectDb.getStartDate()),
              nullSafe(createdDateEnd, projectDb.getDeadlineDate()),
              false, false).getTotalBurnDownCharts();
          assigneeOverview.setBurnDownCharts(assigneeOBurnDownCharts);
        }
      }
    }
  }

  /**
   * Assembles burn-down chart data for case efficiency overview.
   * <p>
   * Generates comprehensive burn-down chart analysis for case overview including
   * total burn-down charts and individual tester burn-down charts when appropriate.
   *
   * @param overview the case efficiency overview to populate with burn-down data
   * @param projectId the project ID for burn-down analysis
   * @param planId optional plan ID for plan-specific analysis
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param createdDateStart start date for analysis
   * @param createdDateEnd end date for analysis
   */
  private void assembleBurnDownChart(EfficiencyCaseOverview overview, Long projectId, Long planId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    // Retrieve project information for date range validation
    Project projectDb = projectQuery.checkAndFind(projectId);

    // Generate total burn-down charts for the project
    Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts = funcCaseQuery.burndownChart(
            projectId, planId, creatorObjectType, creatorObjectId,
            nullSafe(createdDateStart, projectDb.getStartDate()),
            nullSafe(createdDateEnd, projectDb.getDeadlineDate()), false, false)
        .getTotalBurnDownCharts();
    overview.setBurnDownCharts(burnDownCharts);

    // Generate individual tester burn-down charts when not filtering by specific user
    if (isNotEmpty(overview.getTesterOverview())) {
      for (EfficiencyCaseTesterOverview testerOverview : overview.getTesterOverview()) {
        if (!AuthObjectType.USER.equals(creatorObjectType)) {
          Map<BurnDownResourceType, BurnDownChartCount> assigneeOBurnDownCharts
              = funcCaseQuery.burndownChart(projectId, planId,
                  creatorObjectType, testerOverview.getTesterId(),
                  nullSafe(createdDateStart, projectDb.getStartDate()),
                  nullSafe(createdDateEnd, projectDb.getDeadlineDate()), false, false)
              .getTotalBurnDownCharts();
          testerOverview.setBurnDownCharts(assigneeOBurnDownCharts);
        }
      }
    }
  }

}
