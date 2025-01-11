package cloud.xcan.sdf.core.angustester.interfaces.node;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.node.dto.NodeCountFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.sdf.api.dto.EnabledOrDisabledDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.NodeFacade;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
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

@Api(tags = "Node")
@Validated
@RestController
@RequestMapping("/api/v1/node")
public class NodeRest {

  @Resource
  private NodeFacade nodeFacade;

  @ApiOperation(value = "Add nodes", nickname = "node:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<NodeAddDto> dto) {
    return ApiLocaleResult.success(nodeFacade.add(dto));
  }

  @ApiOperation(value = "Update nodes", nickname = "node:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<NodeUpdateDto> dto) {
    nodeFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the name of services", nickname = "services:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Node id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "name", value = "New node name", required = true) @Valid @NotBlank @Length(max = DEFAULT_NAME_LENGTH) @RequestParam(value = "name") String name) {
    nodeFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Stop nodes, only stopping purchased cloud nodes is supported", nickname = "node:stop")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Stop successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/stop")
  public ApiLocaleResult<?> stop(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.stop(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart nodes, only restart of purchased cloud nodes is supported", nickname = "node:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Restarted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/restart")
  public ApiLocaleResult<?> restart(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") LinkedHashSet<Long> ids) {
    nodeFacade.restart(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable nodes", nickname = "node:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/enabled")
  public ApiLocaleResult<?> enabled(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody LinkedHashSet<EnabledOrDisabledDto> dto) {
    nodeFacade.enabled(dto);
    return ApiLocaleResult.success();
  }

  // TODO 运营端设置免费共享节点

  @ApiOperation(value = "Delete nodes", nickname = "node:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Node ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeFacade.delete(ids);
  }

  @ApiOperation(value = "Test node configuration", nickname = "node:test")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Tested successfully", response = ApiLocaleResult.class)})
  @PostMapping("/test")
  public ApiLocaleResult<?> test(@Valid @RequestBody NodeTestDto dto) {
    nodeFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Install node agent", nickname = "node:agent:install")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{id}/agent/install")
  public ApiLocaleResult<AgentInstallCmd> agentInstall(
      @ApiParam(name = "id", value = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.agentInstall(id));
  }

  @ApiOperation(value = "Restart node agent", nickname = "node:agent:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Restart successfully", response = ApiLocaleResult.class)})
  @PostMapping("/{id}/agent/restart")
  public ApiLocaleResult<?> agentRestart(
      @ApiParam(name = "id", value = "Node id", required = true) @PathVariable("id") Long id) {
    nodeFacade.agentRestart(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Purchase node", nickname = "node:purchase")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Purchase successfully", response = ApiLocaleResult.class)})
  @PostMapping("/purchase")
  public ApiLocaleResult<?> purchase(@Valid @RequestBody NodePurchaseDto dto) {
    nodeFacade.purchase(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the detail of node", nickname = "node:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<NodeDetailVo> detail(
      @ApiParam(name = "id", value = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.detail(id));
  }

  @ApiOperation(value = "Query the number of node", nickname = "node:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(@Valid NodeCountFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.count(dto));
  }

  @ApiOperation(value = "Query the list of mock service instances in the node", nickname = "node:mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/mock/service")
  public ApiLocaleResult<List<NodeMockServiceListVo>> mockServiceList(
      @ApiParam(name = "id", value = "Node id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeFacade.mockServiceList(id));
  }

  @ApiOperation(value = "Query the list of node", nickname = "node:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<NodeDetailVo>> list(@Valid NodeFindDto dto) {
    return ApiLocaleResult.success(nodeFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of node", nickname = "node:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<NodeDetailVo>> search(@Valid NodeSearchDto dto) {
    return ApiLocaleResult.success(nodeFacade.search(dto));
  }

}
