package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeNetworkUsageQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsageRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of NodeNetworkUsageQuery that provides network usage metrics
 * for nodes in the AngusTester system.
 * 
 * <p>
 * This class handles network device usage queries, supporting both single device
 * and multi-device network monitoring. It provides current network information
 * and historical usage data for performance analysis.
 * </p>
 * 
 * <p>
 * Key features include:
 * - Network device discovery and monitoring
 * - Current network usage information retrieval
 * - Historical network usage data with pagination
 * - Support for free node metrics access
 * </p>
 * 
 * <p>
 * The implementation automatically handles tenant context switching for free nodes
 * and provides comprehensive network metrics for performance monitoring.
 * </p>
 */
@Biz
public class NodeNetworkUsageQueryImpl implements NodeNetworkUsageQuery {

  @Resource
  private NetUsageRepo netUsageRepo;
  @Resource
  private NodeInfoQuery nodeInfoQuery;

  /**
   * Retrieves current network information for all devices on a specific node.
   * 
   * <p>
   * This method fetches the latest network usage data for all network devices
   * that have been active on the node within the last hour. It automatically
   * switches to owner tenant context for free nodes.
   * </p>
   * 
   * <p>
   * The method discovers available network devices and retrieves their current
   * usage metrics, returning them as a map keyed by device name.
   * </p>
   * 
   * @param nodeId the ID of the node to get network information for
   * @return Map of device name to NetUsage object, or null if no devices found
   */
  @Override
  public Map<String, NetUsage> networkInfo(Long nodeId) {
    return new BizTemplate<Map<String, NetUsage>>(false) {

      @Override
      protected Map<String, NetUsage> process() {
        // Switch to owner tenant context for free node metrics access
        if (!nodeInfoQuery.hasOwnNodes()) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        // Discover network devices active in the last hour
        List<String> deviceNames = netUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(deviceNames)) {
          return null;
        }

        // Retrieve current usage data for each device
        Map<String, NetUsage> deviceUsageMap = new HashMap<>();
        for (String deviceName : deviceNames) {
          NetUsage latestUsage = netUsageRepo
              .findFirstByNodeIdAndDeviceNameOrderByTimestampDesc(nodeId, deviceName);
          if (latestUsage != null) {
            deviceUsageMap.put(deviceName, latestUsage);
          }
        }
        return deviceUsageMap;
      }
    }.execute();
  }

  /**
   * Retrieves historical network usage data for devices on a specific node.
   * 
   * <p>
   * This method provides paginated access to historical network usage data,
   * supporting both single device queries and multi-device batch queries.
   * It automatically handles tenant context for free nodes.
   * </p>
   * 
   * <p>
   * When a specific device name is provided, it returns data for that device only.
   * Otherwise, it returns data for all devices active within the last hour.
   * </p>
   * 
   * @param nodeId the ID of the node to get network usage data for
   * @param spec specification for filtering network usage data
   * @param pageable pagination parameters
   * @return List of NetDeviceUsage objects containing device-specific usage data
   */
  @Override
  public List<NetDeviceUsage> network(Long nodeId, GenericSpecification<NetUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<List<NetDeviceUsage>>(false) {
      String deviceName;

      @Override
      protected void checkParams() {
        // Extract device name from search criteria if specified
        deviceName = CriteriaUtils.findFirstValue(spec.getCriteria(), "deviceName",
            SearchOperation.EQUAL);
        // Add node ID filter to specification
        spec.getCriteria().add(SearchCriteria.equal("nodeId", nodeId));
      }

      @Override
      protected List<NetDeviceUsage> process() {
        // Switch to owner tenant context for free node metrics access
        if (!nodeInfoQuery.hasOwnNodes()) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        List<NetDeviceUsage> result = new ArrayList<>();
        
        // Handle single device query
        if (isNotEmpty(deviceName)) {
          NetDeviceUsage netDeviceInfo = new NetDeviceUsage();
          Page<NetUsage> devicePage = netUsageRepo.findAll(spec, pageable);
          result.add(netDeviceInfo.setDeviceName(deviceName).setDeviceUsage(devicePage));
          return result;
        }

        // Handle multi-device query - discover all active devices
        List<String> deviceNames = netUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(deviceNames)) {
          return result;
        }
        
        // Query data for each discovered device
        deviceNames.forEach(devName -> {
          // Preserve original device name criteria if present
          SearchCriteria originalCriteria = findFirstAndRemove(spec.getCriteria(), "deviceName");
          SearchCriteria deviceCriteria = isNull(originalCriteria) 
              ? SearchCriteria.equal("deviceName", devName) 
              : originalCriteria.setValue(devName);
          spec.add(deviceCriteria);
          
          NetDeviceUsage netDeviceInfo = new NetDeviceUsage();
          Page<NetUsage> devicePage = netUsageRepo.findAll(spec, pageable);
          result.add(netDeviceInfo.setDeviceName(devName).setDeviceUsage(devicePage));
        });
        return result;
      }
    }.execute();
  }

}
