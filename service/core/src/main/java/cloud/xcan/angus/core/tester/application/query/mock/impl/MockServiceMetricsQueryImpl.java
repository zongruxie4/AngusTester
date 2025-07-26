package cloud.xcan.angus.core.tester.application.query.mock.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.LATEST_LIVE_NODE_INTERVAL;

import cloud.xcan.angus.agent.message.mockservice.StatusCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceMetricsQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsageRepo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Implementation of MockServiceMetricsQuery for managing Mock service metrics and status operations.
 * <p>
 * This class provides functionality for querying and managing Mock service metrics,
 * including JVM service usage data, service status monitoring, and live service detection.
 * It handles metrics retrieval, status checking, and performance monitoring for mock services.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock service metrics retrieval with pagination and filtering</li>
 *   <li>Service status monitoring and health checking</li>
 *   <li>Live service detection based on recent metrics</li>
 *   <li>JVM service usage data analysis</li>
 *   <li>Performance metrics aggregation and reporting</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 */
@Component
public class MockServiceMetricsQueryImpl implements MockServiceMetricsQuery {

  @Resource
  private JvmServiceUsageRepo jvmServiceUsageRepo;

  /**
   * Retrieves paginated metrics data for a specific Mock service.
   * <p>
   * Fetches JVM service usage metrics including memory usage, CPU utilization,
   * and performance statistics for comprehensive service monitoring.
   * <p>
   * The method supports flexible filtering and pagination for efficient
   * metrics data retrieval and analysis.
   *
   * @param id the Mock service ID to retrieve metrics for
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @return Page of JvmServiceUsage objects with service metrics data
   */
  @Override
  public Page<JvmServiceUsage> metrics(Long id, GenericSpecification<JvmServiceUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<JvmServiceUsage>>() {

      @Override
      protected Page<JvmServiceUsage> process() {
        // Note: Single tenant table - tenant filtering is not required
        // spec.getCriteria().add(SearchCriteria.equal("tenantId", getOptTenantId()));
        
        // Add service filter to search criteria for service-specific metrics retrieval
        spec.getCriteria().add(SearchCriteria.equal("serviceId", id));
        return jvmServiceUsageRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Retrieves status information for multiple Mock services.
   * <p>
   * Determines the operational status of Mock services by checking for recent
   * metrics data and comparing against live service detection thresholds.
   * <p>
   * The method processes status command parameters and returns comprehensive
   * status information for each requested service.
   *
   * @param dto the MockServiceStatusDto containing status check parameters
   * @return List of StatusVo objects with service status information
   */
  @Override
  public List<StatusVo> status(MockServiceStatusDto dto) {
    // Extract service IDs from command parameters for live service detection
    Set<Long> getLiveServiceIds = dto.getCmdParams().stream()
        .map(StatusCmdParam::getServiceId).collect(Collectors.toSet());
    
    // Determine which services are currently live based on recent metrics
    Set<Long> liveServiceIds = getLiveServiceIds(getLiveServiceIds);
    
    // Build status response map for efficient lookup
    Map<Long, StatusVo> statusVos = new HashMap<>();
    for (StatusCmdParam cmdParam : dto.getCmdParams()) {
      // Create success status for live services, fail status for non-live services
      statusVos.put(cmdParam.getServiceId(), liveServiceIds.contains(cmdParam.getServiceId())
          ? StatusVo.success(cmdParam.getServiceId())
          : StatusVo.fail(cmdParam.getServiceId(), "No latest metrics available"));
    }
    return new ArrayList<>(statusVos.values());
  }

  /**
   * Determines which services are currently live based on recent metrics data.
   * <p>
   * Identifies services that have reported metrics within the configured live node interval,
   * indicating they are currently operational and responding to monitoring requests.
   * <p>
   * The method uses a time-based threshold to distinguish between active and inactive services.
   *
   * @param serviceIds collection of service IDs to check for live status
   * @return Set of service IDs that are currently live and operational
   */
  @Override
  public Set<Long> getLiveServiceIds(Collection<Long> serviceIds) {
    // Calculate the timestamp threshold for live service detection
    long liveThreshold = System.currentTimeMillis() - LATEST_LIVE_NODE_INTERVAL;
    
    // Find services that have reported metrics within the live threshold
    return jvmServiceUsageRepo.findLatestIdByTimestampBeforeAndServiceIdIn(
        liveThreshold, serviceIds);
  }
}
