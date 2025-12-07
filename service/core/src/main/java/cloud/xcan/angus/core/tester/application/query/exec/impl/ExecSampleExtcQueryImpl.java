package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDynamicDataSourceAspect.DEFAULT_SHARDING_KEY;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_SAMPLE_RESULT_CONTENT;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleExtcQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContentRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of ExecSampleExtcQuery for managing extended sample content queries.
 * <p>
 * This class provides functionality to query and retrieve extended sample content information for
 * execution samples. It handles pagination and filtering of sample content with specific extension
 * fields.
 * <p>
 * Supports both paginated queries with search criteria and direct content retrieval for specific
 * execution IDs with extension field filtering.
 */
@Biz
public class ExecSampleExtcQueryImpl implements ExecSampleExtcQuery {

  @Resource
  private ExecSampleContentRepo execSampleExtcRepo;

  /**
   * Lists extended sample content with pagination and search criteria.
   * <p>
   * Retrieves a paginated list of sample content for a specific execution ID.
   * <p>
   * Adds execution ID and tenant ID filters to the search criteria for proper data isolation and
   * security.
   *
   * @param execId   the execution ID to find sample content for
   * @param spec     the search specification for filtering
   * @param pageable pagination parameters
   * @return Page of ExecSampleContent objects
   */
  @Override
  public Page<ExecSampleContent> list(Long execId, GenericSpecification<ExecSampleContent> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleContent>>() {

      @Override
      protected Page<ExecSampleContent> process() {
        spec.getCriteria().add(SearchCriteria.equal("execId", execId));
        spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, getOptTenantId()));
        return execSampleExtcRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Finds iteration sample content for a specific execution ID.
   * <p>
   * Retrieves all sample content associated with the given execution that contains sample result
   * content in the extension field. This method is specifically designed for functional testing
   * scenarios where detailed sample results are needed.
   *
   * @param execId the execution ID to find iteration sample content for
   * @return List of ExecSampleContent objects with sample result content
   */
  @Override
  public List<ExecSampleContent> findIterationSampleContent(Long execId) {
    return execSampleExtcRepo.findAllByExecIdAndExtField(execId, EXT_KEY_SAMPLE_RESULT_CONTENT);
  }
}
