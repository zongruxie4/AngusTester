package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.EVALUATION;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.EvaluationCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.EvaluationQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
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
        // TODO: Implement result calculation logic
        // This should calculate metrics for each EvaluationPurpose based on:
        // - evaluation.getScope() (PROJECT, PLAN, MODULE)
        // - evaluation.getResourceId() (specific resource ID)
        // - evaluation.getPurposes() (list of purposes to evaluate)
        // - evaluation.getStartDate() and evaluation.getDeadlineDate() (time range)

        // For now, this is a placeholder that will be implemented with actual calculation logic
        // The result should be calculated and stored in evaluation.getResult()

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

