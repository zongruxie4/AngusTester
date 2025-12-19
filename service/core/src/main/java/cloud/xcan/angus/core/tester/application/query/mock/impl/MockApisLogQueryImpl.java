package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisLogQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfoRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of MockApisLogQuery for managing Mock API log operations and data retrieval.
 * <p>
 * This class provides comprehensive functionality for querying and managing Mock API logs, which
 * track the execution history and performance metrics of mock API calls. It handles log retrieval,
 * filtering, authorization validation, and comprehensive log analysis.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock API log detail retrieval with proper error handling</li>
 *   <li>Paginated log listing with search capabilities and authorization checks</li>
 *   <li>Service-specific log filtering and analysis</li>
 *   <li>Full-text search support for log content</li>
 *   <li>Comprehensive log information enrichment</li>
 *   <li>Authorization validation for log access</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 *
 * @author XiaoLong Liu
 */
@Service
public class MockApisLogQueryImpl implements MockApisLogQuery {

  @Resource
  private MockApisLogRepo mockApisLogRepo;
  @Resource
  private MockApisLogInfoRepo mockApisLogInfoRepo;
  @Resource
  private MockApisLogSearchRepo mockApisLogSearchRepo;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  /**
   * Retrieves detailed information for a specific Mock API log entry.
   * <p>
   * Fetches complete log details including request/response data, timing information, and execution
   * context for comprehensive log analysis and debugging.
   * <p>
   * The method validates log existence and throws ResourceNotFound if the log entry is not found in
   * the system.
   *
   * @param id the log entry ID to retrieve details for
   * @return MockApisLog object with complete log details
   * @throws ResourceNotFound if the log entry is not found
   */
  @Override
  public MockApisLog detail(Long id) {
    return new BizTemplate<MockApisLog>() {

      @Override
      protected MockApisLog process() {
        return mockApisLogRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "MockApiLog"));
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of Mock API log entries with comprehensive filtering.
   * <p>
   * Supports both regular search and full-text search with service-specific filtering. Provides
   * enriched log information including request/response summaries and performance metrics for
   * comprehensive log analysis.
   * <p>
   * The method performs authorization validation to ensure the current user has permission to view
   * logs for the specified mock service.
   * <p>
   * The method supports flexible filtering by mock service and comprehensive search capabilities
   * for efficient log retrieval and analysis.
   *
   * @param mockServiceId  the mock service ID for filtering logs (null for all services)
   * @param spec           the search specification with criteria and filters
   * @param pageable       pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match          array of field names to include in full-text search
   * @return Page of MockApisLogInfo objects with enriched log information
   */
  @Override
  public Page<MockApisLogInfo> list(Long mockServiceId, GenericSpecification<MockApisLogInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<MockApisLogInfo>>() {
      @Override
      protected void checkParams() {
        // Validate user authorization for viewing logs from the specified mock service
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @Override
      protected Page<MockApisLogInfo> process() {
        // Add mock service filter to search criteria for service-specific log retrieval
        spec.getCriteria().add(SearchCriteria.equal("mockServiceId", mockServiceId));

        // Execute search based on whether full-text search is enabled
        return fullTextSearch
            ? mockApisLogSearchRepo.find(spec.getCriteria(), pageable, MockApisLogInfo.class, match)
            : mockApisLogInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

}
