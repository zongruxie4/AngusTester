package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.EVALUATION;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateCompatibilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateFunctionalPassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateMaintainabilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateOverallScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculatePerformancePassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateScalabilityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateSecurityScore;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateStabilityPassedRate;
import static cloud.xcan.angus.core.tester.application.converter.TestEvaluationConverter.calculateUsabilityScore;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.EvaluationCmd;
import cloud.xcan.angus.core.tester.application.query.project.EvaluationQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for test evaluation management operations.
 * <p>
 * Provides comprehensive CRUD operations for test evaluations including creation, modification,
 * deletion, and result generation.
 * <p>
 * Implements business logic validation, permission checks, activity logging, and transaction
 * management for all evaluation operations.
 */
@Biz
public class EvaluationCmdImpl extends CommCmd<TestEvaluation, Long> implements EvaluationCmd {

  @Resource
  private EvaluationRepo evaluationRepo;

  @Resource
  private EvaluationQuery evaluationQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  /**
   * Adds a new test evaluation to the system.
   * <p>
   * Performs comprehensive validation including project existence and permission checks.
   * <p>
   * Logs evaluation creation activity for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TestEvaluation evaluation) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Verify project exists and user has access
        projectQuery.checkAndFind(evaluation.getProjectId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Generate ID and save evaluation
        IdKey<Long, Object> idKey = insert(evaluation);

        // Log evaluation creation activity for audit
        activityCmd.add(toActivity(EVALUATION, evaluation, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates an existing test evaluation in the system.
   * <p>
   * Validates evaluation existence and user permissions before updating evaluation details.
   * <p>
   * Logs modification activity for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(TestEvaluation evaluation) {
    new BizTemplate<Void>() {
      TestEvaluation evaluationDb;

      @Override
      protected void checkParams() {
        // Ensure the evaluation exists in database
        evaluationDb = evaluationQuery.checkAndFind(evaluation.getId());

        // Verify project exists if projectId is being updated
        if (nonNull(evaluation.getProjectId())) {
          projectQuery.checkAndFind(evaluation.getProjectId());
        }
      }

      @Override
      protected Void process() {
        // Update evaluation information in database
        evaluationRepo.save(copyPropertiesIgnoreNull(evaluation, evaluationDb));

        // Log evaluation update activity for audit
        activityCmd.add(toActivity(EVALUATION, evaluationDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Generates evaluation results based on the evaluation configuration.
   * <p>
   * Calculates metrics for each evaluation purpose and stores the results.
   * <p>
   * This method will be implemented with specific calculation logic for each evaluation purpose.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void generateResult(Long id) {
    new BizTemplate<Void>() {
      TestEvaluation evaluationDb;

      @Override
      protected void checkParams() {
        // Ensure the evaluation exists in database
        evaluationDb = evaluationQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        // Get all cases based on scope and resourceId
        List<FuncCaseInfo> cases = getCasesByScope(evaluationDb);

        // Filter cases by time range if specified
        if (nonNull(evaluationDb.getStartDate()) || nonNull(evaluationDb.getDeadlineDate())) {
          cases = filterCasesByTimeRange(cases, evaluationDb.getStartDate(),
              evaluationDb.getDeadlineDate());
        }

        // Calculate metrics for each evaluation purpose
        LinkedHashMap<EvaluationPurpose, TestEvaluationResult.MetricResult> metrics = new LinkedHashMap<>();
        for (EvaluationPurpose purpose : evaluationDb.getPurposes()) {
          TestEvaluationResult.MetricResult metricResult = calculateMetric(cases, purpose);
          metrics.put(purpose, metricResult);
        }

        // Calculate overall score (weighted average of all metrics)
        Double overallScore = calculateOverallScore(metrics);

        // Build evaluation result
        TestEvaluationResult result = TestEvaluationResult.builder()
            .overallScore(overallScore)
            .metrics(metrics)
            .build();

        // Save result to evaluation
        evaluationDb.setResult(result);
        evaluationRepo.save(evaluationDb);

        // Log result generation activity for audit
        activityCmd.add(toActivity(EVALUATION, evaluationDb, ActivityType.UPDATED));
        return null;
      }

      /**
       * Get cases based on evaluation scope and resourceId
       */
      private List<FuncCaseInfo> getCasesByScope(TestEvaluation evaluation) {
        List<Long> caseIds = new ArrayList<>();

        if (evaluation.getScope() == EvaluationScope.PROJECT) {
          // Get all cases in the project
          caseIds = funcCaseInfoRepo.findIdByProjectId(evaluation.getProjectId());
        } else if (evaluation.getScope() == EvaluationScope.PLAN) {
          // Get all cases in the plan
          if (nonNull(evaluation.getResourceId())) {
            caseIds = funcCaseInfoRepo.findIdByPlanId(evaluation.getResourceId());
          }
        } else if (evaluation.getScope() == EvaluationScope.MODULE) {
          // Get all cases in the module
          if (nonNull(evaluation.getResourceId())) {
            // Query cases by moduleId
            caseIds = funcCaseInfoRepo.findIdByModuleId(evaluation.getResourceId());
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
          EvaluationPurpose purpose) {
        TestEvaluationResult.MetricResult.MetricResultBuilder builder =
            TestEvaluationResult.MetricResult.builder();

        return switch (purpose) {
          case FUNCTIONAL_PASSED_RATE -> calculateFunctionalPassedRate(cases, builder);
          case PERFORMANCE_PASSED_RATE -> calculatePerformancePassedRate(cases, builder);
          case STABILITY_PASSED_RATE -> calculateStabilityPassedRate(cases, builder);
          case SECURITY_SCORE -> calculateSecurityScore(cases, builder);
          case COMPATIBILITY_SCORE -> calculateCompatibilityScore(cases, builder);
          case USABILITY_SCORE -> calculateUsabilityScore(cases, builder);
          case MAINTAINABILITY_SCORE -> calculateMaintainabilityScore(cases, builder);
          case SCALABILITY_SCORE -> calculateScalabilityScore(cases, builder);
          default -> builder.score(0.0).build();
        };
      }
    }.execute();
  }

  /**
   * Deletes a test evaluation from the system.
   * <p>
   * Performs soft delete by marking the evaluation as deleted.
   * <p>
   * Logs deletion activity for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      TestEvaluation evaluationDb;

      @Override
      protected void checkParams() {
        // Check if evaluation exists in database
        evaluationDb = evaluationQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        // Delete evaluation (physical delete)
        evaluationRepo.deleteById(id);

        // Log evaluation deletion activity for audit
        activityCmd.add(toActivity(EVALUATION, evaluationDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<TestEvaluation, Long> getRepository() {
    return this.evaluationRepo;
  }
}

