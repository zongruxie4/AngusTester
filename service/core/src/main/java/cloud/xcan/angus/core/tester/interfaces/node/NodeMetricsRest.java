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

@Tag(name = "NodeMetrics", description = "Node Monitoring & Metrics API - Access point for node monitoring and metrics querying")
@Validated
@RestController
@RequestMapping("/api/v1/node")
public class NodeMetricsRest {

  @Resource
  private NodeMetricsFacade nodeMetricsFacade;

  @Operation(summary = "Query the list of node sampling(cpu,memory,filesystem)", operationId = "node:sample")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics")
  public ApiLocaleResult<PageResult<NodeMetricsVo>> host(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.host(id, dto));
  }

  @Operation(summary = "Query the latest node sampling(cpu,memory,filesystem)", operationId = "node:sample:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/latest")
  public ApiLocaleResult<NodeMetricsVo> hostLatest(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.hostLatest(id));
  }

  @Operation(summary = "Query the list of node cpu sampling", operationId = "node:sample:cpu")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/cpu")
  public ApiLocaleResult<PageResult<NodeCpuMetricsVo>> cpu(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.cpu(id, dto));
  }

  @Operation(summary = "Query the latest of node cpu sampling", operationId = "node:sample:cpu:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/cpu/latest")
  public ApiLocaleResult<NodeCpuMetricsVo> cpuLatest(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.cpuLatest(id));
  }

  @Operation(summary = "Query the list of node memory sampling", operationId = "node:sample:memory")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/memory")
  public ApiLocaleResult<PageResult<NodeMemoryMetricsVo>> memory(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.memory(id, dto));
  }

  @Operation(summary = "Query the latest of node memory sampling", operationId = "node:sample:memory:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/memory/latest")
  public ApiLocaleResult<NodeMemoryMetricsVo> memoryLatest(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.memoryLatest(id));
  }

  @Operation(summary = "Query the list of node filesystem sampling", operationId = "node:sample:filesystem")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/filesystem")
  public ApiLocaleResult<PageResult<NodeFilesystemMetricsVo>> filesystem(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.filesystem(id, dto));
  }

  @Operation(summary = "Query the latest of node filesystem sampling", operationId = "node:sample:filesystem:latest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/filesystem/latest")
  public ApiLocaleResult<NodeFilesystemMetricsVo> filesystemLatest(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.filesystemLatest(id));
  }

  @Operation(summary = "Query the list of node disk info", operationId = "node:sample:disk:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/disk/info")
  public ApiLocaleResult<List<NodeDiskInfoVo>> diskInfo(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.diskInfo(id));
  }

  @Operation(summary = "Query the list of node disk metric sampling", operationId = "node:sample:disk")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/disk")
  public ApiLocaleResult<List<NodeDiskMetricsVo>> disk(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsNameFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.disk(id, dto));
  }

  @Operation(summary = "Query the list of node network info sampling", operationId = "node:sample:network:info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/network/info")
  public ApiLocaleResult<List<ModeNetworkInfoVo>> networkInfo(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeMetricsFacade.networkInfo(id));
  }

  @Operation(summary = "Query the list of node disk metric sampling", operationId = "node:sample:network")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/metrics/network")
  public ApiLocaleResult<List<NodeNetworkMetricsVo>> network(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject NodeMetricsNameFindDto dto) {
    return ApiLocaleResult.success(nodeMetricsFacade.network(id, dto));
  }

}
