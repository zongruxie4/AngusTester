package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsNameFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeCpuMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskMetricsVo.DiskValue;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeFilesystemMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMemoryMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeNetworkMetricsVo.NetworkValue;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class NodeMetricsAssembler {

  public static NodeMetricsVo toMetricsHostVo(NodeUsage nodeUsage) {
    return new NodeMetricsVo().setTimestamp0(nodeUsage.getTimestamp())
        .setCvsCpu(nonNull(nodeUsage.getCpu()) ? nodeUsage.getCpu().toString() : "")
        .setCvsFilesystem(nonNull(nodeUsage.getFilesystem())
            ? nodeUsage.getFilesystem().toString() : "")
        .setCvsMemory(nonNull(nodeUsage.getMemory()) ? nodeUsage.getMemory().toString() : "");
  }

  public static NodeCpuMetricsVo toMetricsCpuVo(NodeUsage nodeUsage) {
    return new NodeCpuMetricsVo().setTimestamp0(nodeUsage.getTimestamp())
        .setCvsCpu(nonNull(nodeUsage.getCpu()) ? nodeUsage.getCpu().toString() : "");
  }

  public static NodeMemoryMetricsVo toMetricsMemoryVo(NodeUsage nodeUsage) {
    return new NodeMemoryMetricsVo().setTimestamp0(nodeUsage.getTimestamp())
        .setCvsMemory(nonNull(nodeUsage.getMemory()) ? nodeUsage.getMemory().toString() : "");
  }

  public static NodeFilesystemMetricsVo toMetricsFilesystemVo(NodeUsage nodeUsage) {
    return new NodeFilesystemMetricsVo().setTimestamp0(nodeUsage.getTimestamp())
        .setCvsFilesystem(nonNull(nodeUsage.getFilesystem())
            ? nodeUsage.getFilesystem().toString() : "");
  }

  public static DiskValue toDiskValue(DiskUsage diskDeviceUsage) {
    return new DiskValue().setTimestamp0(diskDeviceUsage.getTimestamp())
        .setCvsValue(diskDeviceUsage.getDisk().toHumanString());
  }

  public static NetworkValue toNetworkValue(NetUsage diskDeviceUsage) {
    return new NetworkValue().setTimestamp0(diskDeviceUsage.getTimestamp())
        .setCvsValue(diskDeviceUsage.getNetwork().toHumanString());
  }

  public static GenericSpecification<NodeUsage> getSpecification(NodeMetricsFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build();
    return new GenericSpecification<>(filters);
  }

  public static GenericSpecification<DiskUsage> getDiskSpecification(NodeMetricsNameFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .matchSearchFields("deviceName")
        .timestampStringToLong(true)
        .build();
    return new GenericSpecification<>(filters);
  }

  public static GenericSpecification<NetUsage> getNetSpecification(NodeMetricsNameFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .matchSearchFields("deviceName")
        .timestampStringToLong(true)
        .build();
    return new GenericSpecification<>(filters);
  }
}
