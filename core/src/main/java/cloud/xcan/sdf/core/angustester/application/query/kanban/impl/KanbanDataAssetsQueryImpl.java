package cloud.xcan.sdf.core.angustester.application.query.kanban.impl;

import static cloud.xcan.sdf.api.search.SearchCriteria.equal;
import static cloud.xcan.sdf.api.search.SearchCriteria.merge;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncCaseConverter.getCaseCreatorResourcesFilter;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.getTaskCreatorResourcesFilter;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_MONTH_FORMAT;
import static cloud.xcan.sdf.spec.utils.DateUtils.asDate;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.sumMaps;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDate;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.kanban.KanbanDataAssetsQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisRepo;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetRepo;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsCategory;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsLabel;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsRanking;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.kanban.ResourcesRanking;
import cloud.xcan.sdf.core.angustester.domain.kanban.ScenarioTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.kanban.ScriptTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceRepo;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordInfo;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptRepo;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesRepo;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.sdf.core.jpa.repository.LongKeyCountSummary;
import cloud.xcan.sdf.spec.annotations.NonNullable;
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
import javax.annotation.Resource;

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
  private ReportInfoRepo reportInfoRepo;

  @Resource
  private ReportRecordInfoRepo reportRecordInfoRepo;

  @Resource
  private UserManager userManager;

  /**
   * <pre>
   *  Growth trend resource and label:
   *  - Function: Plan, Use Cases
   *  - Interface: Service, Interface
   *  - Task: Sprint, Task
   *  - Scenario: Total, Function, Performance, Stability, Customization
   *  - Script: Total, Function, Performance, Stability, Customization
   *  - Mock: services, interfaces, files, data sources
   *  - Report: Total, Type Grouping
   * </pre>
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
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected Map<DataAssetsLabel, List<DataAssetsTimeSeries>> process() {
        Set<Long> createdBys = null;
        // If no organization person is selected or the parameters are incomplete, it will be treated as invalid
        if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> commonDeletedFilters = getCommonDeletedResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> caseFilters = getCaseCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deletedFlag", false),
            equal("serviceDeletedFlag", false));
        Set<SearchCriteria> taskFilters = getTaskCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);

        Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt = new LinkedHashMap<>();
        boolean createLessThanOneMonth = MONTHS.between(now(), projectDb.getCreatedDate()) < 1;
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
          case REPORT: {
            assembleReportTimeSeries(createdDateStart, createdDateEnd, commonFilters,
                gt, createLessThanOneMonth);
          }
        }
        return gt;
      }
    }.execute();
  }

  @Override
  public DataAssetsRanking ranking(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<DataAssetsRanking>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected DataAssetsRanking process() {
        Set<Long> createdBys = null;
        // If no organization person is selected or the parameters are incomplete, it will be treated as invalid
        if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> commonDeletedFilters = getCommonDeletedResourcesStatsFilter(projectId,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> taskFilters = getTaskCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> caseFilters = getCaseCreatorResourcesFilter(projectId, null,
            createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> apisFilters = merge(commonFilters, equal("deletedFlag", false),
            equal("serviceDeletedFlag", false));

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
        Map<Long, Long> reportCountMap = datasetRepo.countByFiltersAndGroup(ReportInfo.class,
                LongKeyCountSummary.class, commonFilters, "createdBy", "id").stream()
            .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));

        Map<DataAssetsLabel, List<ResourcesRanking>> ranks = new LinkedHashMap<>();

        List<ResourcesRanking> taskCountRank = taskCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.TASK, taskCountRank);

        List<ResourcesRanking> caseCountRank = caseCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.CASES, caseCountRank);

        List<ResourcesRanking> apisCountRank = apisCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.APIS, apisCountRank);

        List<ResourcesRanking> sceCountRank = sceCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.SCENARIO, sceCountRank);

        List<ResourcesRanking> scriptCountRank = scriptCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.SCRIPT, scriptCountRank);

        List<ResourcesRanking> mockApisCountRank = mockApiCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.MOCK_APIS, mockApisCountRank);

        List<ResourcesRanking> variableCountRank = variableCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.DATA_VARIABLE, variableCountRank);

        List<ResourcesRanking> datasetCountRank = datasetCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.DATA_DATASET, datasetCountRank);

        List<ResourcesRanking> reportCountRank = reportCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.REPORT, reportCountRank);

        Map<Long, Long> totalCountMap = sumMaps(taskCountMap, caseCountMap, apisCountMap,
            sceCountMap, scriptCountMap, mockApiCountMap, variableCountMap, datasetCountMap,
            reportCountMap);
        List<ResourcesRanking> totalCountRank = totalCountMap.entrySet().stream().map(
                KanbanDataAssetsQueryImpl::getResourcesRanking)
            .sorted((u1, u2) -> Long.compare(u2.getCount(), u1.getCount()))
            .collect(Collectors.toList());
        ranks.put(DataAssetsLabel.TOTAL, totalCountRank);

        Map<Long, UserInfo> userMap = getAllUserBaseMap(taskCountMap, caseCountMap, apisCountMap,
            sceCountMap, scriptCountMap, mockApiCountMap, variableCountMap, datasetCountMap,
            reportCountMap);
        return new DataAssetsRanking().setUserInfos(userMap).setRankings(ranks);
      }
    }.execute();
  }

  private void assembleFuncTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> caseFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<IdAndCreatedDate> planTimeSeries = funcPlanRepo.findProjectionByFilters(
        FuncPlan.class, IdAndCreatedDate.class, commonDeletedFilters);
    if (isNotEmpty(planTimeSeries)) {
      gt.put(DataAssetsLabel.PLAN,
          getTimeSeries(createdDateStart, createdDateEnd, planTimeSeries, createLessThanOneMonth));
    }
    List<IdAndCreatedDate> caseTimeSeries = funcCaseRepo.findProjectionByFilters(
        FuncCase.class, IdAndCreatedDate.class, caseFilters);
    gt.put(DataAssetsLabel.CASES,
        getTimeSeries(createdDateStart, createdDateEnd, caseTimeSeries, createLessThanOneMonth));
  }

  private void assembleApisTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> apisFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<IdAndCreatedDate> servicesTimeSeries = servicesRepo.findProjectionByFilters(
        Services.class, IdAndCreatedDate.class, commonDeletedFilters);
    gt.put(DataAssetsLabel.SERVICES,
        getTimeSeries(createdDateStart, createdDateEnd, servicesTimeSeries,
            createLessThanOneMonth));
    List<IdAndCreatedDate> apisTimeSeries = apisRepo.findProjectionByFilters(
        Apis.class, IdAndCreatedDate.class, apisFilters);
    if (isNotEmpty(apisTimeSeries)) {
      gt.put(DataAssetsLabel.APIS,
          getTimeSeries(createdDateStart, createdDateEnd, apisTimeSeries, createLessThanOneMonth));
    }
  }

  private void assembleTaskTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> taskFilters, Set<SearchCriteria> commonDeletedFilters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<IdAndCreatedDate> sprintTimeSeries = taskSprintRepo.findProjectionByFilters(
        TaskSprint.class, IdAndCreatedDate.class, commonDeletedFilters);
    gt.put(DataAssetsLabel.TASK_SPRINT,
        getTimeSeries(createdDateStart, createdDateEnd, sprintTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> taskTimeSeries = taskRepo.findProjectionByFilters(
        Task.class, IdAndCreatedDate.class, taskFilters);
    if (isNotEmpty(taskTimeSeries)) {
      gt.put(DataAssetsLabel.TASK,
          getTimeSeries(createdDateStart, createdDateEnd, taskTimeSeries, createLessThanOneMonth));
    }
  }

  private void assembleScenarioTimeSeries(LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, Set<SearchCriteria> filters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<ScenarioTimeSeries> scenarioTimeSeries = scenarioRepo.findProjectionByFilters(
        Scenario.class, ScenarioTimeSeries.class, filters);
    gt.put(DataAssetsLabel.TOTAL, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().map(x -> (IdAndCreatedDate) x).collect(
            Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_FUNCTIONALITY, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isFunctionalTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_PERFORMANCE, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isPerformanceTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_STABILITY, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isStabilityTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_CUSTOMIZATION, getTimeSeries(createdDateStart, createdDateEnd,
        scenarioTimeSeries.stream().filter(x -> x.getScriptType().isCustomizedTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
  }

  private void assembleScriptTimeSeries(LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, Set<SearchCriteria> filters, Map<DataAssetsLabel,
      List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<ScriptTimeSeries> scriptTimeSeries = scriptRepo.findProjectionByFilters(
        Script.class, ScriptTimeSeries.class, filters);
    gt.put(DataAssetsLabel.TOTAL, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().map(x -> (IdAndCreatedDate) x)
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_FUNCTIONALITY, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isFunctionalTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_PERFORMANCE, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isPerformanceTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_STABILITY, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isStabilityTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
    gt.put(DataAssetsLabel.TEST_CUSTOMIZATION, getTimeSeries(createdDateStart, createdDateEnd,
        scriptTimeSeries.stream().filter(x -> x.getType().isCustomizedTesting())
            .collect(Collectors.toList()), createLessThanOneMonth));
  }

  private void assembleMockTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> filters, Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt,
      boolean createLessThanOneMonth) {
    CriteriaUtils.containsAndRemove(filters, "projectId");
    List<IdAndCreatedDate> msTimeSeries = mockServiceRepo.findProjectionByFilters(
        MockService.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_SERVICE,
        getTimeSeries(createdDateStart, createdDateEnd, msTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> maTimeSeries = mockApisRepo.findProjectionByFilters(
        MockApis.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_APIS,
        getTimeSeries(createdDateStart, createdDateEnd, maTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> masTimeSeries = mockApisResponseRepo.findProjectionByFilters(
        MockApisResponse.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.MOCK_RESPONSE,
        getTimeSeries(createdDateStart, createdDateEnd, masTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> mawTimeSeries = mockApisResponseRepo.findProjectionByFilters(
        MockApisResponse.class, IdAndCreatedDate.class, merge(filters, equal("pushbackFlag", 1)));
    gt.put(DataAssetsLabel.MOCK_PUSHBACK,
        getTimeSeries(createdDateStart, createdDateEnd, mawTimeSeries, createLessThanOneMonth));
  }

  private void assembleDataTimeSeries(LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      Set<SearchCriteria> filters, Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt,
      boolean createLessThanOneMonth) {
    List<IdAndCreatedDate> varTimeSeries = variableRepo.findProjectionByFilters(
        Variable.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_VARIABLE,
        getTimeSeries(createdDateStart, createdDateEnd, varTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> dsTimeSeries = datasetRepo.findProjectionByFilters(
        Dataset.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_DATASET,
        getTimeSeries(createdDateStart, createdDateEnd, dsTimeSeries, createLessThanOneMonth));
    /*List<IdAndCreatedDate> masTimeSeries = mockApisResponseRepo.findProjectionByFilters(
        MockApisResponse.class, IdAndCreatedDate.class, filters);*/
    /*gt.put(DataAssetsLabel.DATA_FILE,
        getTimeSeries(createdDateStart, createdDateEnd, masTimeSeries));*/
    List<IdAndCreatedDate> dssTimeSeries = datasourceRepo.findProjectionByFilters(
        Datasource.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.DATA_DATASOURCE,
        getTimeSeries(createdDateStart, createdDateEnd, dssTimeSeries, createLessThanOneMonth));
  }

  private void assembleReportTimeSeries(LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, Set<SearchCriteria> filters,
      Map<DataAssetsLabel, List<DataAssetsTimeSeries>> gt, boolean createLessThanOneMonth) {
    List<IdAndCreatedDate> varTimeSeries = reportInfoRepo.findProjectionByFilters(
        ReportInfo.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.REPORT,
        getTimeSeries(createdDateStart, createdDateEnd, varTimeSeries, createLessThanOneMonth));
    List<IdAndCreatedDate> dsTimeSeries = reportRecordInfoRepo.findProjectionByFilters(
        ReportRecordInfo.class, IdAndCreatedDate.class, filters);
    gt.put(DataAssetsLabel.REPORT_RECORD,
        getTimeSeries(createdDateStart, createdDateEnd, dsTimeSeries, createLessThanOneMonth));
  }

  public static List<DataAssetsTimeSeries> getTimeSeries(LocalDateTime startDate,
      LocalDateTime endDate, List<IdAndCreatedDate> timeSeries, boolean createLessThanOneMonth) {
    LocalDateTime safeEndDate = nullSafe(endDate, now());
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

  public static List<DataAssetsTimeSeries> getTimeSeriesByFormat
      (List<? extends IdAndCreatedDateBase<?>> timeSeries, String format) {
    Map<Object, ? extends List<? extends IdAndCreatedDateBase<?>>> timeGroup = timeSeries.stream()
        .collect(groupingBy(x -> format(asDate(x.getCreatedDate()), format),
            Collectors.mapping(Function.identity(), Collectors.toList())));
    Map<String, Integer> sortedTimeGroup = new TreeMap<>();
    for (Entry<Object, ? extends List<? extends IdAndCreatedDateBase<?>>> entry : timeGroup.entrySet()) {
      sortedTimeGroup.put(entry.getKey().toString(), entry.getValue().size());
    }
    return sortedTimeGroup.entrySet().stream()
        .map(x -> new DataAssetsTimeSeries(x.getKey(), x.getValue()))
        .collect(Collectors.toList());
  }

  private Map<Long, UserInfo> getAllUserBaseMap(Map<Long, Long> taskCountMap,
      Map<Long, Long> caseCountMap, Map<Long, Long> apisCountMap, Map<Long, Long> sceCountMap,
      Map<Long, Long> scriptCountMap, Map<Long, Long> mockApiCountMap,
      Map<Long, Long> variableCountMap, Map<Long, Long> datasetCountMap,
      Map<Long, Long> reportCountMap) {
    Set<Long> allUserIds = new HashSet<>();
    allUserIds.addAll(taskCountMap.keySet());
    allUserIds.addAll(caseCountMap.keySet());
    allUserIds.addAll(apisCountMap.keySet());
    allUserIds.addAll(sceCountMap.keySet());
    allUserIds.addAll(scriptCountMap.keySet());
    allUserIds.addAll(mockApiCountMap.keySet());
    allUserIds.addAll(variableCountMap.keySet());
    allUserIds.addAll(datasetCountMap.keySet());
    allUserIds.addAll(reportCountMap.keySet());
    return userManager.getValidUserInfoMap(allUserIds).entrySet().stream().collect(Collectors.toMap(
        Entry::getKey, x -> x.getValue().toUserInfo().setMobile(null).setEmail(null)));
  }

  private static ResourcesRanking getResourcesRanking(Entry<Long, Long> x) {
    return new ResourcesRanking().setUserId(x.getKey()).setCount(x.getValue());
  }

}
