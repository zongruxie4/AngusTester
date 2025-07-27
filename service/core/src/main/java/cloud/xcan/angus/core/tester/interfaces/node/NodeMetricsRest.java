package cloud.xcan.angus.core.tester.interfaces.node;

import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeMetricsFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeMetricsNameFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.ModeNetworkInfoVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeCpuMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeDiskInfoVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeDiskMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeFilesystemMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeMemoryMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeNetworkMetricsVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Node Metrics", description = "Node Monitoring & Metrics API - Comprehensive monitoring interfaces for node performance metrics, including CPU, memory, disk, network, and filesystem monitoring with real-time data collection.")
@Validated
@RestController
@RequestMapping("/api/v1/node")
public class NodeMetricsRest {

  @Resource
  private NodeMetricsFacade nodeMetricsFacade;

  @Operation(summary = "Query node performance metrics",
      description = "Retrieve paginated list of comprehensive performance metrics for a specific node.",
      operationId = "node:metrics")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics")
  public ApiLocaleResult<PageResult<NodeMetricsVo>> host(
      @Parameter(name = "id", description = "Node identifier for metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.host(id, dto));
  }

  @Operation(summary = "Query latest node performance metrics",
      description = "Retrieve the most recent performance metrics snapshot for a specific node.",
      operationId = "node:metrics:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest node metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/latest")
  public ApiLocaleResult<NodeMetricsVo> hostLatest(
      @Parameter(name = "id", description = "Node identifier for latest metrics query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.hostLatest(id));
  }

  @Operation(summary = "Query node CPU performance metrics",
      description = "Retrieve detailed CPU performance metrics including utilization, load, and frequency data.",
      operationId = "node:metrics:cpu")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "CPU metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/cpu")
  public ApiLocaleResult<PageResult<NodeCpuMetricsVo>> cpu(
      @Parameter(name = "id", description = "Node identifier for CPU metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.cpu(id, dto));
  }

  @Operation(summary = "Query latest node CPU metrics",
      description = "Retrieve the most recent CPU performance metrics snapshot for a specific node.",
      operationId = "node:metrics:cpu:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest CPU metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/cpu/latest")
  public ApiLocaleResult<NodeCpuMetricsVo> cpuLatest(
      @Parameter(name = "id", description = "Node identifier for latest CPU metrics query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.cpuLatest(id));
  }

  @Operation(summary = "Query node memory performance metrics",
      description = "Retrieve detailed memory performance metrics including usage, available, and swap data.",
      operationId = "node:metrics:memory")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Memory metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/memory")
  public ApiLocaleResult<PageResult<NodeMemoryMetricsVo>> memory(
      @Parameter(name = "id", description = "Node identifier for memory metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.memory(id, dto));
  }

  @Operation(summary = "Query latest node memory metrics",
      description = "Retrieve the most recent memory performance metrics snapshot for a specific node.",
      operationId = "node:metrics:memory:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest memory metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/memory/latest")
  public ApiLocaleResult<NodeMemoryMetricsVo> memoryLatest(
      @Parameter(name = "id", description = "Node identifier for latest memory metrics query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.memoryLatest(id));
  }

  @Operation(summary = "Query node filesystem performance metrics",
      description = "Retrieve detailed filesystem performance metrics including usage, inodes, and mount point data.",
      operationId = "node:metrics:filesystem")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Filesystem metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/filesystem")
  public ApiLocaleResult<PageResult<NodeFilesystemMetricsVo>> filesystem(
      @Parameter(name = "id", description = "Node identifier for filesystem metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.filesystem(id, dto));
  }

  @Operation(summary = "Query latest node filesystem metrics",
      description = "Retrieve the most recent filesystem performance metrics snapshot for a specific node.",
      operationId = "node:metrics:filesystem:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest filesystem metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/filesystem/latest")
  public ApiLocaleResult<NodeFilesystemMetricsVo> filesystemLatest(
      @Parameter(name = "id", description = "Node identifier for latest filesystem metrics query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.filesystemLatest(id));
  }

  @Operation(summary = "Query node disk information",
      description = "Retrieve detailed disk hardware information including device names, sizes, and partition data.",
      operationId = "node:metrics:disk:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Disk information retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/disk/info")
  public ApiLocaleResult<List<NodeDiskInfoVo>> diskInfo(
      @Parameter(name = "id", description = "Node identifier for disk information query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.diskInfo(id));
  }

  @Operation(summary = "Query node disk performance metrics",
      description = "Retrieve detailed disk performance metrics including I/O operations, throughput, and latency data.",
      operationId = "node:metrics:disk")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Disk metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/disk")
  public ApiLocaleResult<List<NodeDiskMetricsVo>> disk(
      @Parameter(name = "id", description = "Node identifier for disk metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsNameFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.disk(id, dto));
  }

  @Operation(summary = "Query node network information",
      description = "Retrieve detailed network interface information including IP addresses, MAC addresses, and interface status.",
      operationId = "node:metrics:network:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Network information retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/network/info")
  public ApiLocaleResult<List<ModeNetworkInfoVo>> networkInfo(
      @Parameter(name = "id", description = "Node identifier for network information query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.networkInfo(id));
  }

  @Operation(summary = "Query node network performance metrics",
      description = "Retrieve detailed network performance metrics including bandwidth, packet statistics, and error rates.",
      operationId = "node:metrics:network")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Network metrics retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/network")
  public ApiLocaleResult<List<NodeNetworkMetricsVo>> network(
      @Parameter(name = "id", description = "Node identifier for network metrics query", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsNameFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.network(id, dto));
  }

}
