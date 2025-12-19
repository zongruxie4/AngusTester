package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.EVALUATION;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreTenantAuditing;
import static java.util.Objects.nonNull;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.TestEvaluationCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TestEvaluationQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
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
@Service
public class TestEvaluationCmdImpl extends CommCmd<TestEvaluation, Long> implements
    TestEvaluationCmd {

  @Resource
  private EvaluationRepo evaluationRepo;

  @Resource
  private TestEvaluationQuery evaluationQuery;

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
        evaluationRepo.save(copyPropertiesIgnoreTenantAuditing(evaluation, evaluationDb, "result"));

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
        TestEvaluationResult result = evaluationQuery.getEvaluationResult(evaluationDb, true);

        // Save result to evaluation
        evaluationDb.setResult(result);
        evaluationRepo.save(evaluationDb);

        // Log result generation activity for audit
        activityCmd.add(toActivity(EVALUATION, evaluationDb, ActivityType.UPDATED));
        return null;
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

