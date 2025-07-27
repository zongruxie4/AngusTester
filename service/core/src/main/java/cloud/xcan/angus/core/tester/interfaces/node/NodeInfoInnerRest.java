package cloud.xcan.angus.core.tester.interfaces.node;

import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_INSTALL_DOOR_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.NODE_INFO_DELETE_DOOR_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.NODE_ONLINE_ENDPOINT_PREFIX;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeInfoFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
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
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Node Info - Internal", description = "Internal Node Management API - Internal service interfaces for node information management and agent control operations.")
@Validated
@RestController
public class NodeInfoInnerRest {

  @Resource
  private NodeInfoFacade nodeInfoFacade;

  @Operation(summary = "Delete node information records",
      description = "Batch delete node information records by their identifiers for internal service operations.",
      operationId = "node:info:delete:inner")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Node information deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(value = NODE_INFO_DELETE_DOOR_ENDPOINT)
  public void delete(
      @Parameter(name = "ids", description = "Node identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    nodeInfoFacade.delete(ids);
  }

  @Operation(summary = "Query online agent nodes",
      description = "Retrieve list of online agent nodes by their identifiers for internal service communication.",
      operationId = "node:info:agent:online:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Online agent nodes retrieved successfully")})
  @GetMapping(value = NODE_ONLINE_ENDPOINT_PREFIX)
  public ApiLocaleResult<Set<Long>> agentOnlineNode(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    return ApiLocaleResult.success(nodeInfoFacade.agentOnlineNode(ids));
  }

  @Operation(summary = "Query agent installation command",
      description = "Retrieve agent installation command for a specific node to enable automated deployment.",
      operationId = "node:agent:install:cmd:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Agent installation command retrieved successfully")})
  @GetMapping(value = AGENT_INSTALL_DOOR_ENDPOINT)
  public ApiLocaleResult<AgentInstallCmd> agentInstallCmdByDoor(
      @Parameter(name = "id", description = "Node identifier for agent installation", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeInfoFacade.agentInstallCmd(id));
  }

}
