package cloud.xcan.angus.core.tester.application.query.project.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.evaluation.EvaluationQuery;
import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluation;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationRepo;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of EvaluationQuery for test evaluation query operations.
 * </p>
 * <p>
 * Provides methods for evaluation listing, detail retrieval, and permission checking.
 * </p>
 */
@Biz
public class EvaluationQueryImpl implements EvaluationQuery {

  @Resource
  private EvaluationRepo evaluationRepo;

  @Resource
  private EvaluationSearchRepo evaluationSearchRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private CommonQuery commonQuery;

  /**
   * <p>
   * Get detailed evaluation information including configuration and results.
   * </p>
   * @param id Evaluation ID
   * @return Evaluation with complete information
   */
  @Override
  public TestEvaluation detail(Long id) {
    return new BizTemplate<TestEvaluation>() {
      @Override
      protected TestEvaluation process() {
        return checkAndFind(id);
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
   * @param spec Evaluation search specification
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Full-text search keywords
   * @return Page of evaluations
   */
  @Override
  public Page<TestEvaluation> list(GenericSpecification<TestEvaluation> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<TestEvaluation>>() {

      @Override
      protected Page<TestEvaluation> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<TestEvaluation> page = fullTextSearch
            ? evaluationSearchRepo.find(criteria, pageable, TestEvaluation.class, match)
            : evaluationRepo.findAll(spec, pageable);
        return page;
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find an evaluation by ID, throw exception if not found.
   * </p>
   * @param id Evaluation ID
   * @return Evaluation entity
   */
  @Override
  public TestEvaluation checkAndFind(Long id) {
    return evaluationRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TestEvaluation"));
  }
}

