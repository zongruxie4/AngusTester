package cloud.xcan.angus.core.tester.application.query.config.impl;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstAndRemove;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.config.NodeDiskUsageQuery;
import cloud.xcan.angus.core.tester.application.query.config.NodeInfoQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsageRepo;
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
 * Implementation of NodeDiskUsageQuery for querying disk usage metrics of nodes.
 *
 * <p>This class provides functionality to retrieve disk usage information for specific nodes,
 * including both basic disk information and detailed disk device usage statistics.</p>
 *
 * <p>The implementation handles tenant-specific access control and supports both single device
 * and multi-device disk usage queries with pagination support.</p>
 */
@Biz
public class NodeDiskUsageQueryImpl implements NodeDiskUsageQuery {

  @Resource
  private DiskUsageRepo diskUsageRepo;
  @Resource
  private NodeInfoQuery nodeInfoQuery;

  /**
   * Retrieves basic disk information for a specific node.
   *
   * <p>This method queries disk usage metrics for all disk devices associated with the given node.
   * It retrieves the most recent disk usage data for each device within the last hour.</p>
   *
   * <p>The method handles tenant access control by switching to owner tenant context when
   * the current user doesn't have access to owned nodes.</p>
   *
   * @param nodeId the unique identifier of the node
   * @return a map containing disk device names as keys and their corresponding DiskUsage objects,
   * or null if no disk devices are found for the node
   */
  @Override
  public Map<String, DiskUsage> diskInfo(Long nodeId) {
    return new BizTemplate<Map<String, DiskUsage>>(false) {

      @Override
      protected Map<String, DiskUsage> process() {
        // Switch to owner tenant context for accessing free node metrics
        if (!nodeInfoQuery.hasOwnNodes()) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        // Query disk device names for the node within the last hour
        List<String> diskNames = diskUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(diskNames)) {
          return null;
        }

        // Retrieve the latest disk usage data for each device
        Map<String, DiskUsage> diskUsageMap = new HashMap<>();
        for (String diskName : diskNames) {
          DiskUsage latestUsage = diskUsageRepo
              .findFirstByNodeIdAndDeviceNameOrderByTimestampDesc(nodeId, diskName);
          diskUsageMap.put(diskName, latestUsage);
        }
        return diskUsageMap;
      }
    }.execute();
  }

  /**
   * Retrieves detailed disk device usage information with pagination support.
   *
   * <p>This method supports querying disk usage data for either a specific device or all devices
   * associated with a node. It applies search criteria and pagination to filter and limit
   * results.</p>
   *
   * <p>The method handles tenant access control and provides comprehensive disk usage statistics
   * including device-specific metrics and historical data.</p>
   *
   * @param nodeId   the unique identifier of the node
   * @param spec     the search specification for filtering disk usage data
   * @param pageable pagination parameters for result limiting
   * @return a list of DiskDeviceUsage objects containing device-specific usage information
   */
  @Override
  public List<DiskDeviceUsage> disk(Long nodeId, GenericSpecification<DiskUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<List<DiskDeviceUsage>>(false) {
      String deviceName;

      @Override
      protected void checkParams() {
        // Extract device name from search criteria if specified
        deviceName = CriteriaUtils.findFirstValue(spec.getCriteria(), "deviceName",
            SearchOperation.EQUAL);
        // Add node ID filter to ensure data belongs to the specified node
        spec.getCriteria().add(SearchCriteria.equal("nodeId", nodeId));
      }

      @Override
      protected List<DiskDeviceUsage> process() {
        // Switch to owner tenant context for accessing free node metrics
        if (!nodeInfoQuery.hasOwnNodes()) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        List<DiskDeviceUsage> result = new ArrayList<>();

        // Handle single device query
        if (isNotEmpty(deviceName)) {
          DiskDeviceUsage diskDeviceInfo = new DiskDeviceUsage();
          Page<DiskUsage> devicePage = diskUsageRepo.findAll(spec, pageable);
          result.add(diskDeviceInfo.setDeviceName(deviceName).setDeviceUsage(devicePage));
          return result;
        }

        // Handle multi-device query - get all device names for the node
        List<String> deviceNames = diskUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(deviceNames)) {
          return result;
        }

        // Query usage data for each device
        deviceNames.forEach(devName -> {
          DiskDeviceUsage diskDeviceInfo = new DiskDeviceUsage();
          // Remove existing device name criteria and add new one for current device
          SearchCriteria sc = findFirstAndRemove(spec.getCriteria(), "deviceName");
          spec.add(isNull(sc) ? SearchCriteria.equal("deviceName", devName)
              : sc.setValue(devName));
          Page<DiskUsage> devicePage = diskUsageRepo.findAll(spec, pageable);
          result.add(diskDeviceInfo.setDeviceName(devName).setDeviceUsage(devicePage));
        });
        return result;
      }
    }.execute();
  }
}
