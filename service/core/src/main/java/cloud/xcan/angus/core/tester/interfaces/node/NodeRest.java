package cloud.xcan.angus.core.tester.interfaces.node;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.dto.NodeCountFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeSearchDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeMockServiceListVo;
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

@Tag(name = "Node", description = "Node Resource Management - Unified control plane for AngusTester server resource role allocation and monitoring")
@Validated
@RestController
@RequestMapping("/api/v1/node")
public class NodeRest {

  @Resource
  private NodeFacade nodeFacade;

  @Operation(summary = "Add nodes", operationId = "node:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<NodeAddDto> dto) {
    return ApiLocaleResult.success(nodeFacade.add(dto));
  }

  @Operation(summary = "Update nodes", operationId = "node:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<NodeUpdateDto> dto) {
    nodeFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the name of services", operationId = "services:name:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @Parameter(name = "id", description = "Node id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "name", description = "New node name", required = true) @RequestParam(value = "name") String name) {
    nodeFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Stop nodes, only stopping purchased cloud nodes is supported", operationId = "node:stop")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stop successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/stop")
  public ApiLocaleResult<?> stop(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.stop(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restart nodes, only restart of purchased cloud nodes is supported", operationId = "node:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restarted successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/restart")
  public ApiLocaleResult<?> restart(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") LinkedHashSet<Long> ids) {
    nodeFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable nodes", operationId = "node:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Enabled or disabled successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody LinkedHashSet<EnabledOrDisabledDto> dto) {
    nodeFacade.enabled(dto);
    return ApiLocaleResult.success();
  }

  // TODO 运营端设置免费共享节点

  @Operation(summary = "Delete nodes", operationId = "node:delete")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Node ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.delete(ids);
  }

  @Operation(summary = "Test node configuration", operationId = "node:test")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tested successfully")})
  @PostMapping("/test")
  public ApiLocaleResult<?> test(@Valid @RequestBody NodeTestDto dto) {
    nodeFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Install node agent", operationId = "node:agent:install")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/agent/install")
  public ApiLocaleResult<AgentInstallCmd> agentInstall(
      @Parameter(name = "id", description = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.agentInstall(id));
  }

  @Operation(summary = "Restart node agent", operationId = "node:agent:restart")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restart successfully")})
  @PostMapping("/{id}/agent/restart")
  public ApiLocaleResult<?> agentRestart(
      @Parameter(name = "id", description = "Node id", required = true) @PathVariable("id") Long id) {
    nodeFacade.agentRestart(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Purchase node", operationId = "node:purchase")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Purchase successfully")})
  @PostMapping("/purchase")
  public ApiLocaleResult<?> purchase(@Valid @RequestBody NodePurchaseDto dto) {
    nodeFacade.purchase(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query the detail of node", operationId = "node:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDetailVo> detail(
      @Parameter(name = "id", description = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.detail(id));
  }

  @Operation(summary = "Query the number of node", operationId = "node:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(@Valid @ParameterObject NodeCountFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.count(dto));
  }

  @Operation(summary = "Query the list of mock service instances in the node", operationId = "node:mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/mock/service")
  public ApiLocaleResult<List<NodeMockServiceListVo>> mockServiceList(
      @Parameter(name = "id", description = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.mockServiceList(id));
  }

  @Operation(summary = "Query the list of node", operationId = "node:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid @ParameterObject NodeFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of node", operationId = "node:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<NodeDetailVo>> search(
      @Valid @ParameterObject NodeSearchDto dto) {
    return ApiLocaleResult.success(nodeFacade.search(dto));
  }

}
