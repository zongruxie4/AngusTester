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

@Tag(name = "NodeInfo", description = "User Node & Executor Process Control API - User-accessible node information and executor process management")
@Validated
@RestController
public class NodeInfoRest {

  @Resource
  private NodeInfoFacade nodeInfoFacade;

  @Operation(summary = "Query the detail of node system information", operationId = "node:info:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Not found resource")})
  @GetMapping(value = "")
  public ApiLocaleResult<NodeInfoDetailVo> detail(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id,
      @Parameter(name = "freeNode", description = "Free node flag", required = true) @RequestParam("freeNode") Boolean freeNode) {
    return ApiLocaleResult.success(nodeInfoFacade.detail(id, freeNode));
  }

  @Operation(summary = "Query the list of node system information", operationId = "node:info:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = LIST_ENDPOINT)
  public ApiLocaleResult<PageResult<NodeInfoDetailVo>> list(
      @Valid @ParameterObject NodeInfoFindDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.list(dto));
  }

  @Operation(summary = "Query the list of node execution", operationId = "node:exec:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = EXEC_ENDPOINT)
  public ApiLocaleResult<List<NodeExecVo>> exec(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeInfoFacade.exec(id));
  }

  @Operation(summary = "Query the list of node agent status", operationId = "node:info:agent:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Not found resource")})
  @PostMapping(value = AGENT_STATUS_CHECK_ENDPOINT)
  public ApiLocaleResult<Map<Long, SimpleCommandResult>> agentStatus(
      @Valid @RequestBody NodeAgentStatusQueryDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.agentStatus(dto));
  }

  @Operation(summary = "Check if the agent port is available", operationId = "node:info:agent:port:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully processed")
  })
  @PostMapping(value = AGENT_PORT_CHECK_ENDPOINT)
  public ApiLocaleResult<List<CheckPortVo>> checkPort(
      @Valid @RequestBody NodeAgentCheckPortDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.checkPort(dto));
  }

  @Operation(summary = "Query the installation cmd of node agent by door api", operationId = "node:agent:install:cmd")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = AGENT_INSTALL_ENDPOINT)
  public ApiLocaleResult<AgentInstallCmd> agentInstallCmdByDoor(
      @Parameter(name = "id", description = "Node ID", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(nodeInfoFacade.agentInstallCmd(id));
  }

  @Operation(summary = "Query the list of runner processes", operationId = "node:runner:process:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @PostMapping(value = RUNNER_PROCESS_ENDPOINT)
  public ApiLocaleResult<RunnerQueryVo> runnerProcess(
      @Valid @ParameterObject NodeRunnerQueryDto dto) {
    return ApiLocaleResult.success(nodeInfoFacade.runnerProcess(dto));
  }

  @Operation(summary = "Kill the runner processes", operationId = "node:runner:process:kill")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Kill successfully")})
  @PostMapping(value = KILL_RUNNER_PROCESS_ENDPOINT)
  public ApiLocaleResult<Boolean> runnerProcessKill(@Valid @RequestBody NodeRunnerKillDto dto) {
    return ApiLocaleResult.successData(nodeInfoFacade.runnerProcessKill(dto));
  }

}
