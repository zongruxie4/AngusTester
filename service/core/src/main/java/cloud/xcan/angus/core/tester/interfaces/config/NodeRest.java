package cloud.xcan.angus.core.tester.interfaces.config;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.dto.NodeCountFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.NodeFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodePurchaseDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeTestDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMockServiceListVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.dto.EnabledOrDisabledDto;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Node", description = "Node Resource Management API - Unified control plane for AngusTester server resource allocation, lifecycle management, and monitoring operations")
@Validated
@RestController
@RequestMapping("/api/v1/node")
public class NodeRest {

  @Resource
  private NodeFacade nodeFacade;

  @Operation(summary = "Create new nodes",
      description = "Batch create new nodes for resource allocation and testing infrastructure setup",
      operationId = "node:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Nodes created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<NodeAddDto> dto) {
    return ApiLocaleResult.success(nodeFacade.add(dto));
  }

  @Operation(summary = "Update node configurations",
      description = "Batch update node properties and configuration settings for resource management",
      operationId = "node:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Nodes updated successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<NodeUpdateDto> dto) {
    nodeFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Rename node",
      description = "Update the display name of a specific node for better resource identification",
      operationId = "node:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node renamed successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Node identifier for rename operation", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New display name for the node", required = true) @RequestParam(value = "name") String name) {
    nodeFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Stop cloud nodes",
      description = "Stop purchased cloud nodes to suspend resource consumption. Note: Only stopping purchased cloud nodes is supported",
      operationId = "node:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Nodes stopped successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")
  })
  @PatchMapping("/stop")
  public ApiLocaleResult<?> stop(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.stop(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart cloud nodes",
      description = "Restart purchased cloud nodes to restore service availability. Note: Only restart of purchased cloud nodes is supported",
      operationId = "node:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Nodes restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")
  })
  @PatchMapping("/restart")
  public ApiLocaleResult<?> restart(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") LinkedHashSet<Long> ids) {
    nodeFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable nodes",
      description = "Control node availability by enabling or disabling nodes for resource management",
      operationId = "node:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")
  })
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<EnabledOrDisabledDto> dto) {
    nodeFacade.enabled(dto);
    return ApiLocaleResult.success();
  }

  // TODO 运营端设置免费共享节点

  @Operation(summary = "Delete nodes",
      description = "Permanently remove nodes from the system to free up resources and clean up infrastructure",
      operationId = "node:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Nodes deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Node identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.delete(ids);
  }

  @Operation(summary = "Test node configuration",
      description = "Validate node configuration and connectivity to ensure proper setup and operation",
      operationId = "node:test")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node configuration tested successfully")})
  @PostMapping("/test")
  public ApiLocaleResult<?> test(@Valid @RequestBody NodeTestDto dto) {
    nodeFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Install node agent",
      description = "Generate agent installation command for a specific node to enable automated deployment",
      operationId = "node:agent:install")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Agent installation command generated successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/agent/install")
  public ApiLocaleResult<AgentInstallCmd> agentInstall(
      @Parameter(name = "id", description = "Node identifier for agent installation", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.agentInstall(id));
  }

  @Operation(summary = "Restart node agent",
      description = "Restart the agent service on a specific node to refresh connections and reload configurations",
      operationId = "node:agent:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Agent restarted successfully")})
  @PostMapping("/{id}/agent/restart")
  public ApiLocaleResult<?> agentRestart(
      @Parameter(name = "id", description = "Node identifier for agent restart", required = true) @PathVariable("id") Long id) {
    nodeFacade.agentRestart(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Purchase cloud node",
      description = "Initiate cloud node purchase workflow for automated resource provisioning and billing",
      operationId = "node:purchase")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node purchase initiated successfully")})
  @PostMapping("/purchase")
  public ApiLocaleResult<?> purchase(@Valid @RequestBody NodePurchaseDto dto) {
    nodeFacade.purchase(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query node details",
      description = "Retrieve information about a specific node including configuration and status",
      operationId = "node:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDetailVo> detail(
      @Parameter(name = "id", description = "Node identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.detail(id));
  }

  @Operation(summary = "Query node count",
      description = "Get the total count of nodes matching specified criteria for resource planning and monitoring",
      operationId = "node:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node count retrieved successfully")})
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(@Valid @ParameterObject NodeCountFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.count(dto));
  }

  @Operation(summary = "Query mock service instances on node",
      description = "Retrieve list of mock service instances running on a specific node for service management",
      operationId = "node:mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mock service instances retrieved successfully")})
  @GetMapping(value = "/{id}/mock/service")
  public ApiLocaleResult<List<NodeMockServiceListVo>> mockServiceList(
      @Parameter(name = "id", description = "Node identifier for mock service query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.mockServiceList(id));
  }

  @Operation(summary = "Query node list",
      description = "Retrieve paginated list of nodes with filtering and search capabilities for resource management",
      operationId = "node:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid @ParameterObject NodeFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.list(dto));
  }

}
