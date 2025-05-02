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
import cloud.xcan.angus.core.tester.application.query.node.NodeDiskUsageQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
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

@Biz
public class NodeDiskUsageQueryImpl implements NodeDiskUsageQuery {

  @Resource
  private DiskUsageRepo diskUsageRepo;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Override
  public Map<String, DiskUsage> diskInfo(Long nodeId) {
    return new BizTemplate<Map<String, DiskUsage>>(false) {

      @Override
      protected Map<String, DiskUsage> process() {
        // Force to query free node metrics
        if (!nodeInfoQuery.hasOwnNodes()){
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        List<String> diskNames = diskUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(diskNames)) {
          return null;
        }

        Map<String, DiskUsage> diskUsageMap = new HashMap<>();
        for (String diskName : diskNames) {
          diskUsageMap.put(diskName, diskUsageRepo
              .findFirstByNodeIdAndDeviceNameOrderByTimestampDesc(nodeId, diskName));
        }
        return diskUsageMap;
      }
    }.execute();
  }

  @Override
  public List<DiskDeviceUsage> disk(Long nodeId, GenericSpecification<DiskUsage> spec,
      PageRequest pageable) {
    return new BizTemplate<List<DiskDeviceUsage>>(false) {
      String deviceName;

      @Override
      protected void checkParams() {
        deviceName = CriteriaUtils.findFirstValue(spec.getCriteria(), "deviceName",
            SearchOperation.EQUAL);
        //spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, tenantId)); -> Single tenant table
        spec.getCriteria().add(SearchCriteria.equal("nodeId", nodeId));
      }

      @Override
      protected List<DiskDeviceUsage> process() {
        // Force to query free node metrics
        if (!nodeInfoQuery.hasOwnNodes()){
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }

        List<DiskDeviceUsage> result = new ArrayList<>();
        if (isNotEmpty(deviceName)) {
          DiskDeviceUsage netDeviceInfo = new DiskDeviceUsage();
          Page<DiskUsage> devicePage = diskUsageRepo.findAll(spec, pageable);
          result.add(netDeviceInfo.setDeviceName(deviceName).setDeviceUsage(devicePage));
          return result;
        }

        List<String> deviceNames = diskUsageRepo.findDeviceNameByNodeId(nodeId,
            LocalDateTime.now().minusHours(1));
        if (isEmpty(deviceNames)) {
          return result;
        }

        deviceNames.forEach(devName -> {
          DiskDeviceUsage diskDeviceInfo = new DiskDeviceUsage();
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
