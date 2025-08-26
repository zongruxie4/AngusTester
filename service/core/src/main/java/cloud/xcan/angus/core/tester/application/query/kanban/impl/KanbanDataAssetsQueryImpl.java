package cloud.xcan.angus.core.tester.application.query.kanban.impl;

import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCaseCreatorResourcesFilter;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.getTaskCreatorResourcesFilter;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.merge;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_MONTH_FORMAT;
import static cloud.xcan.angus.spec.utils.DateUtils.asDate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.sumMaps;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.pojo.IdAndCreatedDate;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.repository.LongKeyCountSummary;
import cloud.xcan.angus.core.tester.application.query.kanban.KanbanDataAssetsQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCase;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsCategory;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsLabel;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsRanking;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.ResourcesRanking;
import cloud.xcan.angus.core.tester.domain.kanban.ScenarioTimeSeries;
import cloud.xcan.angus.core.tester.domain.kanban.ScriptTimeSeries;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of KanbanDataAssetsQuery for managing data assets dashboard queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing data assets
 * dashboard information, including growth trends, resource rankings, and time series analysis.
 * It handles multiple resource types and provides comprehensive statistical analysis.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Growth trend analysis for multiple resource categories</li>
 *   <li>Resource ranking and user contribution analysis</li>
 *   <li>Time series data generation and formatting</li>
 *   <li>Multi-category resource management (Function, APIs, Task, Scenario, Script, Mock, Data)</li>
 *   <li>Creator-based filtering and organization support</li>
 *   <li>Comprehensive statistical aggregation and analysis</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and statistical calculations.
 * <p>
 * Supports both individual category analysis and comprehensive dashboard data generation
 * with proper error handling and resource validation.
 */
@Biz
public class KanbanDataAssetsQueryImpl implements KanbanDataAssetsQuery {

  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private FuncCaseRepo funcCaseRepo;
  @Resource
  private FuncPlanRepo funcPlanRepo;
  @Resource
  private ServicesRepo servicesRepo;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private TaskSprintRepo taskSprintRepo;
  @Resource
  private TaskRepo taskRepo;
  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private ScriptRepo scriptRepo;
  @Resource
  private MockServiceRepo mockServiceRepo;
  @Resource
  private MockApisRepo mockApisRepo;
  @Resource
  private MockApisResponseRepo mockApisResponseRepo;
  @Resource
  private VariableRepo variableRepo;
  @Resource
  private DatasetRepo datasetRepo;
  @Resource
  private DatasourceRepo datasourceRepo;
  @Resource
  private UserManager userManager;

  /**
   * Generates growth trend analysis for data assets across multiple categories.
   * <p>
   * Provides comprehensive growth trend analysis for various resource types including
   * functional resources, APIs, tasks, scenarios, scripts, mock services, and data assets.
   * Supports time-based analysis with flexible date formatting based on project age.
   * <p>
   * Growth trend resource and label categories:
   * <ul>
   *   <li>Function: Plan, Use Cases</li>
   *   <li>Interface: Service, Interface</li>
   *   <li>Task: Sprint, Task</li>
   *   <li>Scenario: Total, Function, Performance, Stability, Customization</li>
   *   <li>Script: Total, Function, Performance, Stability, Customization</li>
   *   <li>Mock: services, interfaces, files, data sources</li>
   *   <li>Report: Total, Type Grouping</li>
   * </ul>
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param projectId the project ID for filtering resources
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @param category the data assets category to analyze
   * @return Map of DataAssetsLabel to time series data for growth trend analysis
   */
  @Override
  public Map<DataAssetsLabel, List<DataAssetsTimeSeries>> growthTrend(
      AuthObjectType creatorObjectType, Long creatorObjectId, Long projectId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      @NonNullable DataAssetsCategory category) {
    return new BizTemplate<Map<DataAssetsLabel, List<DataAssetsTimeSeries>>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Validate project existence before analysis
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected Map<DataAssetsLabel, List<DataAssetsTimeSeries>> process() {
        // Determine creator IDs based on organization type and ID
        Set<Long> createdBys = null;
        // If no organization person is selected or the parameters are incomplete, it will be treated as invalid
        if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // Build comprehensive filter criteria for different resource types
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> commonDeletedFilters = getCommonDeletedResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> caseFilters = getCaseCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deleted", false),
            equal("serviceDeleted", false));
        Set<SearchCriteria> taskFilters = getTaskCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);

        // Initialize result map and determine time formatting strategy
        Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt = new LinkedHashMap<>();
        boolean createLessThanOneMonth = MONTHS.between(now(), projectDb.getCreatedDate()) < 1;

        // Process different categories based on the specified category
        switch (category) {
          case FUNC: {
            assembleFuncTimeSeries(createdDateStart, createdDateEnd, caseFilters,
                commonDeletedFilters, gt, createLessThanOneMonth);
            break;
          }
          case APIS: {
            assembleApisTimeSeries(createdDateStart, createdDateEnd, apisFilters,
                commonDeletedFilters, gt, createLessThanOneMonth);
            break;
          }
          case TASK: {
            assembleTaskTimeSeries(createdDateStart, createdDateEnd, taskFilters,
                commonDeletedFilters, gt, createLessThanOneMonth);
            break;
          }
          case SCENARIO: {
            assembleScenarioTimeSeries(createdDateStart, createdDateEnd, commonDeletedFilters,
                gt, createLessThanOneMonth);
            break;
          }
          case SCRIPT: {
            assembleScriptTimeSeries(createdDateStart, createdDateEnd, commonFilters,
                gt, createLessThanOneMonth);
            break;
          }
          case MOCK: {
            assembleMockTimeSeries(createdDateStart, createdDateEnd, commonFilters,
                gt, createLessThanOneMonth);
            break;
          }
          case DATA: {
            assembleDataTimeSeries(createdDateStart, createdDateEnd, commonFilters,
                gt, createLessThanOneMonth);
            break;
          }
        }
        return gt;
      }
    }.execute();
  }

  /**
   * Generates comprehensive resource ranking analysis for data assets.
   * <p>
   * Provides detailed ranking analysis across multiple resource types including tasks,
   * cases, APIs, scenarios, scripts, mock APIs, variables, and datasets. Calculates
   * total contribution rankings and user information enrichment.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param creatorObjectType the creator organization type for filtering
   * @param creatorObjectId the creator organization ID for filtering
   * @param projectId the project ID for filtering resources
   * @param createdDateStart start date for filtering
   * @param createdDateEnd end date for filtering
   * @return DataAssetsRanking object with comprehensive ranking analysis and user information
   */
  @Override
  public DataAssetsRanking ranking(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<DataAssetsRanking>() {

      @Override
      protected DataAssetsRanking process() {
        // Determine creator IDs based on organization type and ID
        Set<Long> createdBys = null;
        // If no organization person is selected or the parameters are incomplete, it will be treated as invalid
        if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // Build comprehensive filter criteria for different resource types
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> commonDeletedFilters = getCommonDeletedResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> taskFilters = getTaskCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> caseFilters = getCaseCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deleted", false),
            equal("serviceDeleted", false));

        // Retrieve count maps for different resource types with optimized grouping
        Map<Long, Long> taskCountMap = taskRepo.countByFiltersAndGroup(Task.class,
                LongKeyCountSummary.class, taskFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> caseCountMap = funcCaseRepo.countByFiltersAndGroup(FuncCase.class,
                LongKeyCountSummary.class, caseFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> apisCountMap = apisRepo.countByFiltersAndGroup(Apis.class,
                LongKeyCountSummary.class, apisFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> sceCountMap = scenarioRepo.countByFiltersAndGroup(Scenario.class,
                LongKeyCountSummary.class, commonDeletedFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> scriptCountMap = scriptRepo.countByFiltersAndGroup(Script.class,
                LongKeyCountSummary.class, commonFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> mockApiCountMap = mockApisRepo.countByFiltersAndGroup(MockApis.class,
                LongKeyCountSummary.class, commonFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> variableCountMap = variableRepo.countByFiltersAndGroup(Variable.class,
                LongKeyCountSummary.class, commonFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
        Map<Long, Long> datasetCountMap = datasetRepo.countByFiltersAndGroup(Variable.class,
                LongKeyCountSummary.class, commonFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));

        // Build comprehensive ranking structure
        Map<DataAssetsLabel, List<ResourcesRanking>> ranks = new LinkedHashMap<>();

        // Generate rankings for each resource type with proper sorting
        List<ResourcesRanking> taskCountRank = taskCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.TASK, taskCountRank);

        List<ResourcesRanking> caseCountRank = caseCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.CASES, caseCountRank);

        List<ResourcesRanking> apisCountRank = apisCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.APIS, apisCountRank);

        List<ResourcesRanking> sceCountRank = sceCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.SCENARIO, sceCountRank);

        List<ResourcesRanking> scriptCountRank = scriptCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.SCRIPT, scriptCountRank);

        List<ResourcesRanking> mockApisCountRank = mockApiCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.MOCK_APIS, mockApisCountRank);

        List<ResourcesRanking> variableCountRank = variableCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.DATA_VARIABLE, variableCountRank);

        List<ResourcesRanking> datasetCountRank = datasetCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.DATA_DATASET, datasetCountRank);

        // Calculate total contribution rankings across all resource types
        Map<Long, Long> totalCountMap = sumMaps(taskCountMap, caseCountMap, apisCountMap,
            sceCountMap, scriptCountMap, mockApiCountMap, variableCountMap, datasetCountMap);
        List<ResourcesRanking> totalCountRank = totalCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .toList();
        ranks.put(DataAssetsLabel.TOTAL, totalCountRank);

        // Enrich rankings with user information
        Map<Long, UserInfo> userMap = getAllUserBaseMap(taskCountMap, caseCountMap, apisCountMap,
            sceCountMap, scriptCountMap, mockApiCountMap, variableCountMap, datasetCountMap);
        return new DataAssetsRanking().setUserInfos(userMap).setRankings(ranks);
      }
    }.execute();
  }

  /**
   * Assembles functional resource time series data.
   * <p>
   * Processes functional plans and cases to generate time series data for growth trend analysis.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param caseFilters filter criteria for case queries
   * @param commonDeletedFilters filter criteria for deleted resources
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleFuncTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> caseFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    // Process functional plans time series
    List<IdAndCreatedDate> planTimeSeries = funcPlanRepo.findProjectionByFilters(
        FuncPlan.class, IdAndCreatedDate.class, commonDeletedFilters);
    if (isNotEmpty(planTimeSeries)) {
      gt.put(DataAssetsLabel.PLAN,
          getTimeSeries(createdDateStart, createdDateEnd, planTimeSeries, createLessThanOneMonth));
    }

    // Process functional cases time series
    List<IdAndCreatedDate> caseTimeSeries = funcCaseRepo.findProjectionByFilters(
        FuncCase.class, IdAndCreatedDate.class, caseFilters);
    gt.put(DataAssetsLabel.CASES,
        getTimeSeries(createdDateStart, createdDateEnd, caseTimeSeries, createLessThanOneMonth));
  }

  /**
   * Assembles API resource time series data.
   * <p>
   * Processes services and APIs to generate time series data for growth trend analysis.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param apisFilters filter criteria for API queries
   * @param commonDeletedFilters filter criteria for deleted resources
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleApisTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> apisFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    // Process services time series
    List<IdAndCreatedDate> servicesTimeSeries = servicesRepo.findProjectionByFilters(
        Services.class, IdAndCreatedDate.class, commonDeletedFilters);
    gt.put(DataAssetsLabel.SERVICES,
        getTimeSeries(createdDateStart, createdDateEnd, servicesTimeSeries,
            createLessThanOneMonth));

    // Process APIs time series
    List<IdAndCreatedDate> apisTimeSeries = apisRepo.findProjectionByFilters(
        Apis.class, IdAndCreatedDate.class, apisFilters);
    if (isNotEmpty(apisTimeSeries)) {
      gt.put(DataAssetsLabel.APIS,
          getTimeSeries(createdDateStart, createdDateEnd, apisTimeSeries, createLessThanOneMonth));
    }
  }

  /**
   * Assembles task resource time series data.
   * <p>
   * Processes sprints and tasks to generate time series data for growth trend analysis.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param taskFilters filter criteria for task queries
   * @param commonDeletedFilters filter criteria for deleted resources
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleTaskTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> taskFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    // Process sprint time series
    List<IdAndCreatedDate> sprintTimeSeries = taskSprintRepo.findProjectionByFilters(
        TaskSprint.class, IdAndCreatedDate.class, commonDeletedFilters);
    gt.put(DataAssetsLabel.TASK_SPRINT,
        getTimeSeries(createdDateStart, createdDateEnd, sprintTimeSeries, createLessThanOneMonth));

    // Process task time series
    List<IdAndCreatedDate> taskTimeSeries = taskRepo.findProjectionByFilters(
        Task.class, IdAndCreatedDate.class, taskFilters);
    if (isNotEmpty(taskTimeSeries)) {
      gt.put(DataAssetsLabel.TASK,
          getTimeSeries(createdDateStart, createdDateEnd, taskTimeSeries, createLessThanOneMonth));
    }
  }

  /**
   * Assembles scenario resource time series data.
   * <p>
   * Processes scenarios by type to generate time series data for growth trend analysis.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param filters filter criteria for scenario queries
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleScenarioTimeSeries(LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, Set<SearchCriteria> filters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    // Retrieve scenario time series with type information
    List<ScenarioTimeSeries> scenarioTimeSeries = scenarioRepo.findProjectionByFilters(
        Scenario.class, ScenarioTimeSeries.class, filters);

    // Process total scenarios
    gt.put(DataAssetsLabel.TOTAL, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().map(x -> (IdAndCreatedDate) x).collect(
            Collectors.toList()), createLessThanOneMonth));

    // Process scenarios by testing type
    gt.put(DataAssetsLabel.TEST_FUNCTIONALITY, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isFunctionalTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_PERFORMANCE, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isPerformanceTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_STABILITY, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isStabilityTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_CUSTOMIZATION, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isCustomizedTesting())
            .toList(), createLessThanOneMonth));
  }

  /**
   * Assembles script resource time series data.
   * <p>
   * Processes scripts by type to generate time series data for growth trend analysis.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param filters filter criteria for script queries
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleScriptTimeSeries(LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, Set<SearchCriteria> filters, Map<DataAssetsLabel,
      List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    // Retrieve script time series with type information
    List<ScriptTimeSeries> scriptTimeSeries = scriptRepo.findProjectionByFilters(
        Script.class, ScriptTimeSeries.class, filters);

    // Process total scripts
    gt.put(DataAssetsLabel.TOTAL, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().map(x -> (IdAndCreatedDate) x)
            .toList(), createLessThanOneMonth));

    // Process scripts by testing type
    gt.put(DataAssetsLabel.TEST_FUNCTIONALITY, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isFunctionalTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_PERFORMANCE, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isPerformanceTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_STABILITY, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isStabilityTesting())
            .toList(), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_CUSTOMIZATION, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isCustomizedTesting())
            .toList(), createLessThanOneMonth));
  }

  /**
   * Assembles mock resource time series data.
   * <p>
   * Processes mock services, APIs, responses, and pushback data to generate time series.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param filters filter criteria for mock queries
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleMockTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> filters, Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt,
      boolean createLessThanOneMonth) {
    // Remove projectId filter for mock service queries
    CriteriaUtils.containsAndRemove(filters, "projectId");

    // Process mock services time series
    List<IdAndCreatedDate> msTimeSeries = mockServiceRepo.findProjectionByFilters(
        MockService.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_SERVICE,
        getTimeSeries(createdDateStart, createdDateEnd, msTimeSeries, createLessThanOneMonth));

    // Process mock APIs time series
    List<IdAndCreatedDate> maTimeSeries = mockApisRepo.findProjectionByFilters(
        MockApis.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_APIS,
        getTimeSeries(createdDateStart, createdDateEnd, maTimeSeries, createLessThanOneMonth));

    // Process mock responses time series
    List<IdAndCreatedDate> masTimeSeries = mockApisResponseRepo.findProjectionByFilters(
        MockApisResponse.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_RESPONSE,
        getTimeSeries(createdDateStart, createdDateEnd, masTimeSeries, createLessThanOneMonth));

    // Process mock pushback time series
    List<IdAndCreatedDate> mawTimeSeries = mockApisResponseRepo.findProjectionByFilters(
        MockApisResponse.class, IdAndCreatedDate.class, merge(filters, equal("pushback", 1)));
    gt.put(DataAssetsLabel.MOCK_PUSHBACK,
        getTimeSeries(createdDateStart, createdDateEnd, mawTimeSeries, createLessThanOneMonth));
  }

  /**
   * Assembles data resource time series data.
   * <p>
   * Processes variables, datasets, and datasources to generate time series data.
   *
   * @param createdDateStart start date for time series generation
   * @param createdDateEnd end date for time series generation
   * @param filters filter criteria for data queries
   * @param gt the growth trend map to populate
   * @param createLessThanOneMonth whether project is less than one month old
   */
  private void assembleDataTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> filters, Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt,
      boolean createLessThanOneMonth) {
    // Process variables time series
    List<IdAndCreatedDate> varTimeSeries = variableRepo.findProjectionByFilters(
        Variable.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_VARIABLE,
        getTimeSeries(createdDateStart, createdDateEnd, varTimeSeries, createLessThanOneMonth));

    // Process datasets time series
    List<IdAndCreatedDate> dsTimeSeries = datasetRepo.findProjectionByFilters(
        Dataset.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_DATASET,
        getTimeSeries(createdDateStart, createdDateEnd, dsTimeSeries, createLessThanOneMonth));

    // Process datasources time series
    List<IdAndCreatedDate> dssTimeSeries = datasourceRepo.findProjectionByFilters(
        Datasource.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_DATASOURCE,
        getTimeSeries(createdDateStart, createdDateEnd, dssTimeSeries, createLessThanOneMonth));
  }

  /**
   * Generates time series data with appropriate date formatting.
   * <p>
   * Determines the appropriate date format based on project age and time range,
   * then generates time series data with proper grouping and counting.
   *
   * @param startDate start date for time series generation
   * @param endDate end date for time series generation
   * @param timeSeries list of time series data points
   * @param createLessThanOneMonth whether project is less than one month old
   * @return List of DataAssetsTimeSeries objects with formatted time data
   */
  public static List<DataAssetsTimeSeries> getTimeSeries(LocalDateTime startDate,
      LocalDateTime endDate, List<? extends IdAndCreatedDate> timeSeries, boolean createLessThanOneMonth) {
    LocalDateTime safeEndDate = nullSafe(endDate, now());

    // Determine date format based on project age and time range
    if (createLessThanOneMonth) {
      return getTimeSeriesByFormat(timeSeries, DEFAULT_DAY_FORMAT);
    }
    if (isNull(startDate) || MONTHS.between(endDate, startDate) > 1) {
      return getTimeSeriesByFormat(timeSeries, DEFAULT_MONTH_FORMAT);
    } else if (ChronoUnit.DAYS.between(safeEndDate, startDate) > 1) {
      return getTimeSeriesByFormat(timeSeries, DEFAULT_DAY_FORMAT);
    }
    return getTimeSeriesByFormat(timeSeries, DEFAULT_MONTH_FORMAT);
  }

  /**
   * Generates time series data with specified date format.
   * <p>
   * Groups time series data by formatted date and counts occurrences for each time period.
   *
   * @param timeSeries list of time series data points
   * @param format date format string for grouping
   * @return List of DataAssetsTimeSeries objects with grouped and counted data
   */
  public static List<DataAssetsTimeSeries> getTimeSeriesByFormat
      (List<? extends IdAndCreatedDateBase<?>> timeSeries, String format) {
    // Group time series data by formatted date
    Map<Object, ? extends List<? extends IdAndCreatedDateBase<?>>> timeGroup = timeSeries.stream()
        .collect(groupingBy(x -> format(asDate(x.getCreatedDate()), format),
            Collectors.mapping(Function.identity(), Collectors.toList())));

    // Convert grouped data to sorted map with counts
    Map<String, Integer> sortedTimeGroup = new TreeMap<>();
    for (Entry<Object, ? extends List<? extends IdAndCreatedDateBase<?>>> entry : timeGroup.entrySet()) {
      sortedTimeGroup.put(entry.getKey().toString(), entry.getValue().size());
    }

    // Convert to DataAssetsTimeSeries objects
    return sortedTimeGroup.entrySet().stream()
        .map(x -> new DataAssetsTimeSeries(x.getKey(), x.getValue()))
        .toList();
  }

  /**
   * Retrieves user information for all users across multiple resource types.
   * <p>
   * Collects user IDs from all resource count maps and retrieves user information
   * with privacy protection (mobile and email removed).
   *
   * @param taskCountMap task count map by user
   * @param caseCountMap case count map by user
   * @param apisCountMap API count map by user
   * @param sceCountMap scenario count map by user
   * @param scriptCountMap script count map by user
   * @param mockApiCountMap mock API count map by user
   * @param variableCountMap variable count map by user
   * @param datasetCountMap dataset count map by user
   * @return Map of user ID to UserInfo with privacy protection
   */
  private Map<Long, UserInfo> getAllUserBaseMap(Map<Long, Long> taskCountMap,
      Map<Long, Long> caseCountMap, Map<Long, Long> apisCountMap, Map<Long, Long> sceCountMap,
      Map<Long, Long> scriptCountMap, Map<Long, Long> mockApiCountMap,
      Map<Long, Long> variableCountMap, Map<Long, Long> datasetCountMap) {
    // Collect all user IDs from all resource types
    Set<Long> allUserIds = new HashSet<>();
    allUserIds.addAll(taskCountMap.keySet());
    allUserIds.addAll(caseCountMap.keySet());
    allUserIds.addAll(apisCountMap.keySet());
    allUserIds.addAll(sceCountMap.keySet());
    allUserIds.addAll(scriptCountMap.keySet());
    allUserIds.addAll(mockApiCountMap.keySet());
    allUserIds.addAll(variableCountMap.keySet());
    allUserIds.addAll(datasetCountMap.keySet());

    // Retrieve user information with privacy protection
    return userManager.getValidUserInfoMap(allUserIds).entrySet().stream().collect(Collectors.toMap(
        Entry::getKey, x -> x.getValue().toUserInfo().setMobile(null).setEmail(null)));
  }

  /**
   * Creates a ResourcesRanking object from a map entry.
   * <p>
   * Utility method to convert map entries to ranking objects for consistent processing.
   *
   * @param x the map entry containing user ID and count
   * @return ResourcesRanking object with user ID and count
   */
  private static ResourcesRanking getResourcesRanking(Entry<Long, Long> x) {
    return new ResourcesRanking().setUserId(x.getKey()).setCount(x.getValue());
  }

}
