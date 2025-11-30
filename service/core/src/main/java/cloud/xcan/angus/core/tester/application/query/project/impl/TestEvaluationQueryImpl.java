package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateAvailabilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateCompatibilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateComplianceScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateFunctionalPassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateMaintainabilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateOverallScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculatePerformancePassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateScalabilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateSecurityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateStabilityPassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateUsabilityScore;
import static cloud.xcan.angus.spec.utils.ObjectUtils.formatDouble;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.config.SettingTenantQuery;
import cloud.xcan.angus.core.tester.application.query.project.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TestEvaluationQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSetting;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationSearchRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult.MetricResult;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TestEvaluationQuery for test evaluation query operations.
 * </p>
 * <p>
 * Provides methods for evaluation listing, detail retrieval, and permission checking.
 * </p>
 */
@Biz
public class TestEvaluationQueryImpl implements TestEvaluationQuery {

  @Resource
  private EvaluationRepo evaluationRepo;

  @Resource
  private EvaluationSearchRepo evaluationSearchRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private ModuleQuery moduleQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private SettingTenantQuery settingTenantQuery;

  /**
   * <p>
   * Get detailed evaluation information including configuration and results.
   * </p>
   *
   * @param id Evaluation ID
   * @return Evaluation with complete information
   */
  @Override
  public TestEvaluation detail(Long id) {
    return new BizTemplate<TestEvaluation>() {
      @Override
      protected TestEvaluation process() {
        TestEvaluation evaluation = checkAndFind(id);
        setResourceName(List.of(evaluation));
        return evaluation;
      }
    }.execute();
  }

  /**
   * <p>
   * List evaluations with optional full-text search and authorization filtering.
   * </p>
   * <p>
   * Only returns non-deleted evaluations. Applies authorization conditions for non-admin users.
   * </p>
   *
   * @param spec           Evaluation search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Full-text search keywords
   * @return Page of evaluations
   */
  @Override
  public Page<TestEvaluation> list(GenericSpecification<TestEvaluation> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<TestEvaluation>>() {

      @Override
      protected Page<TestEvaluation> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();

        Page<TestEvaluation> page = fullTextSearch
            ? evaluationSearchRepo.find(criteria, pageable, TestEvaluation.class, match)
            : evaluationRepo.findAll(spec, pageable);

        setResourceName(page.getContent());
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find an evaluation by ID, throw exception if not found.
   * </p>
   *
   * @param id Evaluation ID
   * @return Evaluation entity
   */
  @Override
  public TestEvaluation checkAndFind(Long id) {
    return evaluationRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TestEvaluation"));
  }

  @Override
  public TestEvaluationResult getEvaluationResult(TestEvaluation evaluation) {
    // Get all cases based on scope and resourceId
    List<FuncCaseInfo> cases = getCasesByScope(evaluation);

    if (isEmpty(cases)){
      return TestEvaluationResult.builder()
          .totalCases(0)
          .overallScore(0.0)
          .metrics(new LinkedHashMap<>())
          .build();
    }

    // Filter cases by time range if specified
    if (nonNull(evaluation.getStartDate()) || nonNull(evaluation.getDeadlineDate())) {
      cases = filterCasesByTimeRange(cases, evaluation.getStartDate(),
          evaluation.getDeadlineDate());
    }

    // Get evaluation indicator weight setting
    TenantSetting settingTenant = settingTenantQuery.findAndInit(evaluation.getTenantId());

    // Calculate metrics for each evaluation purpose
    LinkedHashMap<EvaluationPurpose, MetricResult> metrics = new LinkedHashMap<>();
    for (EvaluationPurpose purpose : evaluation.getPurposes()) {
      TestEvaluationResult.MetricResult metricResult = calculateMetric(cases, purpose,
          settingTenant.getEvaluationWeightData());
      metrics.put(purpose, metricResult);
    }

    // Calculate overall score (weighted average of all metrics)
    Double overallScore = formatDouble(calculateOverallScore(metrics), "0.00");

    // Build evaluation result
    return TestEvaluationResult.builder()
        .totalCases(cases.size())
        .overallScore(overallScore)
        .metrics(metrics)
        .build();
  }

  @Override
  public void setResourceName(List<TestEvaluation> evaluations) {
    if (isNotEmpty(evaluations)) {
      Map<EvaluationScope, Set<Long>> evaluationResourceMap = evaluations.stream()
          .collect(Collectors.groupingBy(TestEvaluation::getScope,
              Collectors.mapping(TestEvaluation::getResourceId, Collectors.toSet())
          ));

      for (Entry<EvaluationScope, Set<Long>> entry : evaluationResourceMap.entrySet()) {
        EvaluationScope scope = entry.getKey();
        Set<Long> resourceIds = entry.getValue();
        Map<Long, ActivityResource> resourceNameMap = switch (scope) {
          case PROJECT ->
              projectQuery.find0ById(resourceIds).stream().map(r -> (ActivityResource) r)
                  .collect(Collectors.toMap(ActivityResource::getId, r -> r));
          case FUNC_PLAN ->
              funcPlanQuery.find0ById(resourceIds).stream().map(r -> (ActivityResource) r)
                  .collect(Collectors.toMap(ActivityResource::getId, r -> r));
          case MODULE ->
              moduleQuery.find0ById(resourceIds).stream().map(r -> (ActivityResource) r)
              .collect(Collectors.toMap(ActivityResource::getId, r -> r));
        };

        for (TestEvaluation evaluation : evaluations) {
          if (evaluation.getScope() == scope) {
            ActivityResource resource = resourceNameMap.get(evaluation.getResourceId());
            evaluation.setResourceName(resource.getName());
          }
        }
      }
    }
  }

  /**
   * Get cases based on evaluation scope and resourceId
   */
  private List<FuncCaseInfo> getCasesByScope(TestEvaluation evaluation) {
    List<Long> caseIds = new ArrayList<>();

    if (evaluation.getScope() == EvaluationScope.PROJECT) {
      // Get all cases in the project
      caseIds = funcCaseInfoRepo.findIdByProjectId(evaluation.getProjectId());
    } else if (evaluation.getScope() == EvaluationScope.FUNC_PLAN) {
      // Get all cases in the plan
      if (nonNull(evaluation.getResourceId())) {
        caseIds = funcCaseInfoRepo.findIdByPlanId(evaluation.getResourceId());
      }
    } else if (evaluation.getScope() == EvaluationScope.MODULE) {
      // Get all cases in the module
      if (nonNull(evaluation.getResourceId())) {
        // Query cases by moduleId
        Set<Long> moduleIds = moduleQuery.findModuleAndSubIds(
            evaluation.getProjectId(), List.of(evaluation.getResourceId()));
        caseIds = funcCaseInfoRepo.findIdByModuleIdIn(moduleIds);
      }
    }
    if (caseIds.isEmpty()) {
      return new ArrayList<>();
    }

    // Filter out deleted cases
    return funcCaseInfoRepo.findByIdIn(caseIds);
  }

  /**
   * Filter cases by time range
   */
  private List<FuncCaseInfo> filterCasesByTimeRange(List<FuncCaseInfo> cases,
      LocalDateTime startDate, LocalDateTime deadlineDate) {
    return cases.stream()
        .filter(c -> {
          LocalDateTime caseDate = c.getCreatedDate();
          if (caseDate == null) {
            return false;
          }
          if (nonNull(startDate) && caseDate.isBefore(startDate)) {
            return false;
          }
          return !nonNull(deadlineDate) || !caseDate.isAfter(deadlineDate);
        }).collect(Collectors.toList());
  }

  /**
   * Calculate metric for a specific evaluation purpose
   */
  private TestEvaluationResult.MetricResult calculateMetric(List<FuncCaseInfo> cases,
      EvaluationPurpose purpose, LinkedHashMap<EvaluationPurpose, Integer> evaluationWeight) {
    TestEvaluationResult.MetricResult.MetricResultBuilder builder =
        TestEvaluationResult.MetricResult.builder();

    TestEvaluationResult.MetricResult metricResult = switch (purpose) {
      case FUNCTIONAL_SCORE -> calculateFunctionalPassedRate(cases, builder);
      case PERFORMANCE_SCORE -> calculatePerformancePassedRate(cases, builder);
      case STABILITY_SCORE -> calculateStabilityPassedRate(cases, builder);
      case SECURITY_SCORE -> calculateSecurityScore(cases, builder);
      case COMPATIBILITY_SCORE -> calculateCompatibilityScore(cases, builder);
      case COMPLIANCE_SCORE -> calculateComplianceScore(cases, builder);
      case AVAILABILITY_SCORE -> calculateAvailabilityScore(cases, builder);
      case USABILITY_SCORE -> calculateUsabilityScore(cases, builder);
      case MAINTAINABILITY_SCORE -> calculateMaintainabilityScore(cases, builder);
      case SCALABILITY_SCORE -> calculateScalabilityScore(cases, builder);
      default -> builder.score(0.0).build();
    };

    metricResult.setWeight(evaluationWeight.getOrDefault(purpose, 0));
    return metricResult;
  }
}

