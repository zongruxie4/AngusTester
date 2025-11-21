package cloud.xcan.angus.core.tester.interfaces.config.facade;

import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsNameFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.ModeNetworkInfoVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeCpuMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeFilesystemMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMemoryMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeNetworkMetricsVo;
import cloud.xcan.angus.remote.PageResult;
import java.util.List;

public interface NodeMetricsFacade {

  PageResult<NodeMetricsVo> host(Long id, NodeMetricsFindDto dto);

  NodeMetricsVo hostLatest(Long id);

  PageResult<NodeCpuMetricsVo> cpu(Long id, NodeMetricsFindDto dto);

  NodeCpuMetricsVo cpuLatest(Long id);

  PageResult<NodeMemoryMetricsVo> memory(Long id, NodeMetricsFindDto dto);

  NodeMemoryMetricsVo memoryLatest(Long id);

  PageResult<NodeFilesystemMetricsVo> filesystem(Long id, NodeMetricsFindDto dto);

  NodeFilesystemMetricsVo filesystemLatest(Long id);

  List<NodeDiskInfoVo> diskInfo(Long id);

  List<NodeDiskMetricsVo> disk(Long id, NodeMetricsNameFindDto dto);

  List<ModeNetworkInfoVo> networkInfo(Long id);

  List<NodeNetworkMetricsVo> network(Long id, NodeMetricsNameFindDto dto);

}
