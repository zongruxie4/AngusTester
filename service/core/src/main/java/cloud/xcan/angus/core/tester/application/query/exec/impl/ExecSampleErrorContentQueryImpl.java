package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.tester.infra.metricsds.MetricsDynamicDataSourceAspect.DEFAULT_SHARDING_KEY;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleErrorContentQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCauseRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of ExecSampleErrorContentQuery for managing sample error content queries.
 * <p>
 * This class provides functionality to query and retrieve error content information
 * for execution samples. It handles pagination and filtering of error causes
 * associated with specific executions.
 * <p>
 * Supports both paginated queries with search criteria and direct content retrieval
 * for specific execution IDs.
 */
@Biz
public class ExecSampleErrorContentQueryImpl implements ExecSampleErrorContentQuery {

  @Resource
  private ExecSampleErrorCauseRepo execSampleErrorContentRepo;

  /**
   * Lists error content with pagination and search criteria.
   * <p>
   * Retrieves a paginated list of error causes for a specific execution ID.
   * <p>
   * Adds execution ID and tenant ID filters to the search criteria for proper
   * data isolation and security.
   *
   * @param id the execution ID to find error content for
   * @param spec the search specification for filtering
   * @param pageable pagination parameters
   * @return Page of ExecSampleErrorCause objects
   */
  @Override
  public Page<ExecSampleErrorCause> list(Long id,
      GenericSpecification<ExecSampleErrorCause> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleErrorCause>>() {

      @Override
      protected Page<ExecSampleErrorCause> process() {
        spec.getCriteria().add(SearchCriteria.equal("execId", id));
        spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, getOptTenantId()));
        return execSampleErrorContentRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Finds all error content for a specific execution ID.
   * <p>
   * Retrieves all error causes associated with the given execution without pagination.
   * This method is useful for getting complete error information for analysis.
   *
   * @param id the execution ID to find error content for
   * @return List of ExecSampleErrorCause objects
   */
  @Override
  public List<ExecSampleErrorCause> findErrorContent(Long id) {
    return execSampleErrorContentRepo.findByExecId(id);
  }
}
