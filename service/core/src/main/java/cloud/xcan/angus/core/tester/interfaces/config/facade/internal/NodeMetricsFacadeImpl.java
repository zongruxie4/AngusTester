package cloud.xcan.angus.core.tester.interfaces.config.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.getDiskSpecification;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.getNetSpecification;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toDiskValue;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toMetricsCpuVo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toMetricsFilesystemVo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toMetricsHostVo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toMetricsMemoryVo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler.toNetworkValue;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.application.query.config.NodeDiskUsageQuery;
import cloud.xcan.angus.core.tester.application.query.config.NodeNetworkUsageQuery;
import cloud.xcan.angus.core.tester.application.query.config.NodeUsageQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import cloud.xcan.angus.core.tester.interfaces.config.facade.NodeMetricsFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsNameFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeMetricsAssembler;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.ModeNetworkInfoVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeCpuMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskMetricsVo.DiskValue;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeFilesystemMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMemoryMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeNetworkMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeNetworkMetricsVo.NetworkValue;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;


@Component
public class NodeMetricsFacadeImpl implements NodeMetricsFacade {

  @Resource
  private NodeUsageQuery nodeUsageQuery;

  @Resource
  private NodeDiskUsageQuery diskUsageQuery;

  @Resource
  private NodeNetworkUsageQuery networkUsageQuery;

  @Override
  public PageResult<NodeMetricsVo> host(Long id, NodeMetricsFindDto dto) {
    Page<NodeUsage> nodePage = nodeUsageQuery.metrics(id, getSpecification(dto), dto.tranPage());
    return buildVoPageResult(nodePage, NodeMetricsAssembler::toMetricsHostVo);
  }

  @Override
  public NodeMetricsVo hostLatest(Long id) {
    NodeUsage nodeUsage = nodeUsageQuery.metricsLatest(id);
    return nonNull(nodeUsage) ? toMetricsHostVo(nodeUsage) : null;
  }

  @Override
  public PageResult<NodeCpuMetricsVo> cpu(Long id, NodeMetricsFindDto dto) {
    Page<NodeUsage> cpuPage = nodeUsageQuery.metrics(id, getSpecification(dto), dto.tranPage());
    return buildVoPageResult(cpuPage, NodeMetricsAssembler::toMetricsCpuVo);
  }

  @Override
  public NodeCpuMetricsVo cpuLatest(Long id) {
    NodeUsage nodeUsage = nodeUsageQuery.metricsLatest(id);
    return nonNull(nodeUsage) ? toMetricsCpuVo(nodeUsage) : null;
  }

  @Override
  public PageResult<NodeMemoryMetricsVo> memory(Long id, NodeMetricsFindDto dto) {
    Page<NodeUsage> memory = nodeUsageQuery.metrics(id, getSpecification(dto), dto.tranPage());
    return buildVoPageResult(memory, NodeMetricsAssembler::toMetricsMemoryVo);
  }

  @Override
  public NodeMemoryMetricsVo memoryLatest(Long id) {
    NodeUsage nodeUsage = nodeUsageQuery.metricsLatest(id);
    return nonNull(nodeUsage) ? toMetricsMemoryVo(nodeUsage) : null;
  }

  @Override
  public PageResult<NodeFilesystemMetricsVo> filesystem(Long id, NodeMetricsFindDto dto) {
    Page<NodeUsage> filesystem = nodeUsageQuery.metrics(id, getSpecification(dto), dto.tranPage());
    return buildVoPageResult(filesystem, NodeMetricsAssembler::toMetricsFilesystemVo);
  }

  @Override
  public NodeFilesystemMetricsVo filesystemLatest(Long id) {
    NodeUsage nodeUsage = nodeUsageQuery.metricsLatest(id);
    return nonNull(nodeUsage) ? toMetricsFilesystemVo(nodeUsage) : null;
  }

  @Override
  public List<NodeDiskInfoVo> diskInfo(Long id) {
    Map<String, DiskUsage> diskUsageMap = diskUsageQuery.diskInfo(id);
    return isEmpty(diskUsageMap) ? null: diskUsageMap.entrySet().stream()
        .map(o -> new NodeDiskInfoVo().setDeviceName(o.getKey())
            .setDiskUsage(nonNull(o.getValue()) ? toDiskValue(o.getValue()) : null))
        .toList();
  }

  @Override
  public List<NodeDiskMetricsVo> disk(Long id, NodeMetricsNameFindDto dto) {
    List<DiskDeviceUsage> usages = diskUsageQuery
        .disk(id, getDiskSpecification(dto), dto.tranPage());

    List<NodeDiskMetricsVo> result = new ArrayList<>();
    for (DiskDeviceUsage info : usages) {
      NodeDiskMetricsVo metricsDiskVo = new NodeDiskMetricsVo().setDeviceName(info.getDeviceName());
      Page<DiskUsage> deviceUsage = info.getDeviceUsage();
      if (nonNull(deviceUsage)) {
        PageResult<DiskValue> diskValuePage = buildVoPageResult(deviceUsage,
            NodeMetricsAssembler::toDiskValue);
        if (nonNull(diskValuePage)) {
          metricsDiskVo.setValues(diskValuePage);
        }
      }
      result.add(metricsDiskVo);
    }
    return result;
  }

  @Override
  public List<ModeNetworkInfoVo> networkInfo(Long id) {
    Map<String, NetUsage> netUsageMap = networkUsageQuery.networkInfo(id);
    return isEmpty(netUsageMap) ? null: netUsageMap.entrySet().stream()
        .map(o -> new ModeNetworkInfoVo().setDeviceName(o.getKey())
            .setNetworkUsage(nonNull(o.getValue()) ? toNetworkValue(o.getValue()) : null)
        ).toList();
  }

  @Override
  public List<NodeNetworkMetricsVo> network(Long id, NodeMetricsNameFindDto dto) {
    List<NetDeviceUsage> usages = networkUsageQuery
        .network(id, getNetSpecification(dto), dto.tranPage());
    List<NodeNetworkMetricsVo> result = new ArrayList<>();
    for (NetDeviceUsage usage : usages) {
      NodeNetworkMetricsVo vo = new NodeNetworkMetricsVo().setDeviceName(usage.getDeviceName());
      Page<NetUsage> deviceUsage = usage.getDeviceUsage();
      if (nonNull(deviceUsage)) {
        PageResult<NetworkValue> netValuePage = buildVoPageResult(deviceUsage,
            NodeMetricsAssembler::toNetworkValue);
        if (nonNull(netValuePage)) {
          vo.setValues(netValuePage);
        }
      }
      result.add(vo);
    }
    return result;
  }
}
