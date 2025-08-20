package cloud.xcan.angus.core.tester.interfaces.node;

import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_INSTALL_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_PORT_CHECK_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_STATUS_CHECK_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.EXEC_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.KILL_RUNNER_PROCESS_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.LIST_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.RUNNER_PROCESS_ENDPOINT;

import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.runner.RunnerQueryVo;
import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeInfoFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentStatusQueryDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerQueryDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeExecVo;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
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
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Node Info", description = "Node Information & Agent Control API - User-accessible interfaces for node system information, agent status monitoring, and process control operations")
@Validated
@RestController
public class NodeInfoRest {

  @Resource
  private NodeInfoFacade nodeInfoFacade;

  @Operation(summary = "Query node system information details",
      description = "Retrieve detailed system information of a specific node including hardware specs and status.",
      operationId = "node:info:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node system information retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")})
  @GetMapping(value = "")
  public ApiLocaleResult<NodeInfoDetailVo> detail(
      @Parameter(name = "id", description = "Node identifier for system information query", required = true) @PathVariable("id") Long id,
      @Parameter(name = "freeNode", description = "Flag indicating whether this is a free/shared node", required = true) @RequestParam("freeNode") Boolean freeNode) {
    return ApiLocaleResult.success(nodeInfoFacade.detail(id, freeNode));
  }

  @Operation(summary = "Query node system information list",
      description = "Retrieve paginated list of node system information with filtering and search capabilities.",
      operationId = "node:info:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node system information list retrieved successfully")})
  @GetMapping(value = LIST_ENDPOINT)
  public ApiLocaleResult<PageResult<NodeInfoDetailVo>> list(
      @Valid @ParameterObject NodeInfoFindDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.list(dto));
  }

  @Operation(summary = "Query node execution information",
      description = "Retrieve current execution status and information for a specific node.",
      operationId = "node:exec:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Node execution information retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT)
  public ApiLocaleResult<List<NodeExecVo>> exec(
      @Parameter(name = "id", description = "Node identifier for execution information query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeInfoFacade.exec(id));
  }

  @Operation(summary = "Query node agent status",
      description = "Check agent status for multiple nodes to verify connectivity and health.",
      operationId = "node:info:agent:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Agent status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Node not found")})
  @PostMapping(value = AGENT_STATUS_CHECK_ENDPOINT)
  public ApiLocaleResult<Map<Long, SimpleCommandResult>> agentStatus(
      @Valid @RequestBody NodeAgentStatusQueryDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.agentStatus(dto));
  }

  @Operation(summary = "Check agent port availability",
      description = "Verify port availability on agent nodes for network connectivity testing.",
      operationId = "node:info:agent:port:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Port availability check completed successfully")
  })
  @PostMapping(value = AGENT_PORT_CHECK_ENDPOINT)
  public ApiLocaleResult<List<CheckPortVo>> checkPort(
      @Valid @RequestBody NodeAgentCheckPortDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.checkPort(dto));
  }

  @Operation(summary = "Query agent installation command",
      description = "Retrieve agent installation command for a specific node to enable manual deployment.",
      operationId = "node:agent:install:cmd")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Agent installation command retrieved successfully")})
  @GetMapping(value = AGENT_INSTALL_ENDPOINT)
  public ApiLocaleResult<AgentInstallCmd> agentInstallCmdByDoor(
      @Parameter(name = "id", description = "Node identifier for agent installation command", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeInfoFacade.agentInstallCmd(id));
  }

  @Operation(summary = "Query runner process information",
      description = "Retrieve detailed information about runner processes running on nodes.",
      operationId = "node:runner:process:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Runner process information retrieved successfully")})
  @PostMapping(value = RUNNER_PROCESS_ENDPOINT)
  public ApiLocaleResult<RunnerQueryVo> runnerProcess(
      @Valid @ParameterObject NodeRunnerQueryDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.runnerProcess(dto));
  }

  @Operation(summary = "Terminate runner processes",
      description = "Forcefully terminate runner processes on nodes for process control and cleanup.",
      operationId = "node:runner:process:kill")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Runner processes terminated successfully")})
  @PostMapping(value = KILL_RUNNER_PROCESS_ENDPOINT)
  public ApiLocaleResult<Boolean> runnerProcessKill(@Valid @RequestBody NodeRunnerKillDto dto) {
    return ApiLocaleResult.successData(nodeInfoFacade.runnerProcessKill(dto));
  }

}
