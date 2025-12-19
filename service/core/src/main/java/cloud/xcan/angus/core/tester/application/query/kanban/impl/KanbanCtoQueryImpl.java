package cloud.xcan.angus.core.tester.application.query.kanban.impl;

import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCaseTesterResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleCaseOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoCaseConverter.assembleTesterOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleAssigneeOverview;
import static cloud.xcan.angus.core.tester.application.converter.KanbanCtoTaskConverter.assembleTaskOverview;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.getTaskAssigneeResourcesFilter;
import static cloud.xcan.angus.core.tester.application.query.issue.impl.TaskQueryImpl.getTaskSummary;
import static cloud.xcan.angus.core.tester.application.query.test.impl.FuncCaseQueryImpl.getCaseSummary;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.kanban.KanbanCtoQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTestQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskEfficiencySummary;
import cloud.xcan.angus.core.tester.domain.kanban.CtoCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseEfficiencySummary;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Implementation of KanbanCtoQuery for managing CTO-level kanban dashboard queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing CTO-level kanban
 * dashboard data, including task and case overviews with efficiency metrics. It handles member
 * management, resource filtering, and comprehensive data aggregation.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Task CTO overview with assignee efficiency metrics</li>
 *   <li>Case CTO overview with tester efficiency metrics</li>
 *   <li>API and scenario test result integration</li>
 *   <li>Member management and user information enrichment</li>
 *   <li>Resource filtering based on organization and date ranges</li>
 *   <li>Comprehensive data aggregation and summary generation</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and data enrichment.
 * <p>
 * Supports both individual overview operations and comprehensive dashboard data generation
 * with proper error handling and resource validation.
 */
@Service
public class KanbanCtoQueryImpl implements KanbanCtoQuery {

  @Resource
  private TaskRepo taskRepo;
  @Resource
  private TaskInfoRepo taskInfoRepo;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private ApisTestQuery apisTestQuery;
  @Resource
  private ScenarioTestQuery scenarioTestQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private JoinSupplier joinSupplier;

  /**
   * Generates CTO-level task overview with comprehensive efficiency metrics.
   * <p>
   * Provides detailed task overview including assignee efficiency, member statistics, and optional
   * detailed task lists and assignee overviews. Integrates with API and scenario test results for
   * complete project visibility.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType    the creator organization type for filtering
   * @param creatorObjectId      the creator organization ID for filtering
   * @param projectId            the project ID for filtering tasks
   * @param planId               optional plan ID for plan-specific filtering
   * @param createdDateStart     start date for filtering
   * @param createdDateEnd       end date for filtering
   * @param joinTaskList         whether to include detailed task list
   * @param joinAssigneeOverview whether to include assignee overview details
   * @return CtoTaskOverview object with comprehensive task metrics and statistics
   */
  @Override
  public CtoTaskOverview taskCtoOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTaskList, boolean joinAssigneeOverview) {
    return new BizTemplate<CtoTaskOverview>() {

      @Override
      protected CtoTaskOverview process() {
        CtoTaskOverview overview = new CtoTaskOverview();

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

        // Set member statistics and user information
        overview.setMemberNum(memberIds.size());
        overview.setAssignees(commonQuery.getUserInfoMap(memberIds));
        overview.setAssigneeNum(tasks.stream().map(TaskEfficiencySummary::getAssigneeId)
            .collect(Collectors.toSet()).size());

        // Assemble comprehensive task overview metrics
        assembleTaskOverview(tasks, overview);

        // Optionally include assignee overview details
        if (joinAssigneeOverview) {
          assembleAssigneeOverview(tasks, overview);
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
   * Generates CTO-level case overview with comprehensive efficiency metrics.
   * <p>
   * Provides detailed case overview including tester efficiency, member statistics, API and
   * scenario test results, and optional detailed case lists and tester overviews. Integrates
   * multiple data sources for complete project visibility.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType  the creator organization type for filtering
   * @param creatorObjectId    the creator organization ID for filtering
   * @param projectId          the project ID for filtering cases
   * @param planId             optional plan ID for plan-specific filtering
   * @param createdDateStart   start date for filtering
   * @param createdDateEnd     end date for filtering
   * @param joinCaseList       whether to include detailed case list
   * @param joinTesterOverview whether to include tester overview details
   * @return CtoCaseOverview object with comprehensive case metrics and statistics
   */
  @Override
  public CtoCaseOverview caseCtoOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCaseList, boolean joinTesterOverview) {
    return new BizTemplate<CtoCaseOverview>() {

      @Override
      protected CtoCaseOverview process() {
        CtoCaseOverview overview = new CtoCaseOverview();

        // Retrieve project member IDs for filtering
        Set<Long> memberIds = projectMemberQuery.getMemberIds(creatorObjectType,
            creatorObjectId, projectId);

        // Integrate API test results for comprehensive overview
        TestApisCount testApisCount = apisTestQuery.countTestResult(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        overview.setApisTestCount(testApisCount);

        // Integrate scenario test results for comprehensive overview
        TestScenarioCount scenarioCount = scenarioTestQuery.countTestResult(projectId,
            creatorObjectType, creatorObjectId, createdDateStart, createdDateEnd);
        overview.setScenarioTestCount(scenarioCount);

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

        // Set member statistics and user information
        overview.setMemberNum(memberIds.size());
        overview.setTesters(commonQuery.getUserInfoMap(memberIds));
        overview.setTesterNum(cases.stream().map(FuncCaseEfficiencySummary::getTesterId)
            .collect(Collectors.toSet()).size());

        // Assemble comprehensive case overview metrics
        assembleCaseOverview(cases, overview);

        // Optionally include tester overview details
        if (joinTesterOverview) {
          assembleTesterOverview(cases, overview);
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
}
