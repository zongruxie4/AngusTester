package cloud.xcan.angus.core.tester.application.cmd.exec.impl;

import static cloud.xcan.angus.agent.AgentCommandType.RUNNER_RUN;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.tester.application.converter.ExecDebugConverter.startToDomain;
import static cloud.xcan.angus.core.tester.application.converter.ExecDebugConverter.toExecNodeInfo;
import static cloud.xcan.angus.core.tester.application.query.node.impl.NodeInfoQueryImpl.doHttpPostRequest;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_START_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.BROADCAST_START_TO_REMOTE_EXCEPTION_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_AGENT_ROUTER_NOT_FOUND_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_NODE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_REMOTE_CONTROLLER_IGNORED_T;
import static cloud.xcan.angus.core.tester.infra.job.ExecEventJob.createInnerPrincipal;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.parser.AngusParser.YAML_MAPPER;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.lengthSafe;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;

import cloud.xcan.angus.agent.message.runner.RunnerRunDto;
import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecDebugCmd;
import cloud.xcan.angus.core.tester.application.query.exec.ExecDebugQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugRepo;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugSource;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContentRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCauseRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleRepo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.type.TestTargetType;
import cloud.xcan.angus.model.meter.MeterStatus;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.NodeSelector;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing debug executions of test scripts.
 * </p>
 * <p>
 * Provides comprehensive debug execution management services including starting, monitoring, 
 * and deleting debug executions. Handles permission checks, node selection, distributed 
 * execution, and result aggregation. Supports debug execution from scripts, scenarios, 
 * and monitors with server configuration overrides.
 * </p>
 * <p>
 * Key features include single-node debug execution, remote controller communication, 
 * script parsing and assembly, and comprehensive error handling with detailed status reporting.
 * </p>
 */
@Slf4j
@Biz
public class ExecDebugCmdImpl extends CommCmd<ExecDebug, Long> implements ExecDebugCmd {

  @Resource
  private ExecDebugRepo execDebugRepo;
  @Resource
  private ExecDebugQuery execDebugQuery;
  @Resource
  private ExecQuery execQuery;
  @Resource
  private NodeQuery nodeQuery;
  @Resource
  private NodeInfoQuery nodeInfoQuery;
  @Resource
  private ExecSampleRepo execSampleRepo;
  @Resource
  private ExecSampleErrorCauseRepo execSampleErrorsRepo;
  @Resource
  private ExecSampleContentRepo execSampleExtcRepo;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private ObjectMapper objectMapper;
  @Resource
  private DiscoveryClient discoveryClient;
  @Resource
  private ApplicationInfo appInfo;

  /**
   * <p>
   * Start a debug execution, either broadcast or targeted.
   * </p>
   * <p>
   * Validates quotas, parses script, selects node, and manages execution lifecycle.
   * Handles both local and remote node execution with comprehensive error handling.
   * </p>
   * @param broadcast Whether to broadcast execution or target specific node
   * @param debug Debug execution entity
   * @return Updated debug execution entity with results
   */
  @Override
  public ExecDebug start(boolean broadcast, ExecDebug debug) {
    return new BizTemplate<ExecDebug>() {
      AngusScript angusScript;
      ExecDebug debugDb;

      @Override
      protected void checkParams() {
        if (broadcast) {
          // Validate execution quotas for new debug execution
          execDebugQuery.checkAddQuota(1);

          // Validate concurrent task quotas for running execution
          execDebugQuery.checkConcurrentTaskQuota(1);

          // Parse script content from configuration or script string
          if (nonNull(debug.getConfiguration()) && nonNull(debug.getTask())) {
            // Assemble script from configuration and task components
            angusScript = assembleAngusScript(debug);
          } else if (isNotEmpty(debug.getScript())) {
            // Parse existing script content
            angusScript = parseAngusScript(debug.getScript());
          } else {
            throw new SysException("Debug script is empty");
          }

          // Validate node availability and application nodes
          execQuery.checkNodeValid(angusScript.getConfiguration(), false);
        } else {
          // For targeted mode: validate debug execution ID exists
          assertNotNull(debug.getId(), "Execution debug id is required when broadcast = false");
          debugDb = execDebugQuery.checkAndFind(debug.getId());
        }
      }

      @Override
      protected ExecDebug process() {
        // formatter:off
        log.info("Controller handle to start execution debug `{}` request", debug.getId());

        try {
          // Set inner principal for non-user actions
          if (!isUserAction()) {
            PrincipalContext.set(createInnerPrincipal(appInfo));
          }

          // Select execution node based on mode
          Long nodeId;
          if (broadcast) {
            // Select node using strategy for broadcast mode
            NodeSelector nodeSelector = angusScript.getConfiguration().getNodeSelectors();
            nodeId = selectNodeByStrategy(nodeSelector, 1).getId();
          } else {
            // Use existing node ID for targeted mode
            nodeId = debugDb.getExecNodeId();
          }

          // Save debug execution records for broadcast mode
          if (broadcast) {
            // Note: Historical debug results are not cleared based on existing scripts or scenarios
            // deleteByScriptIdAndScenarioId(debug.getScriptId(), debug.getScenarioId());

            // Set node ID and running status, then save
            debug.setExecNodeId(nodeId).setStatus(ExecStatus.RUNNING);
            add0(debug);
            debugDb = debug;
          }

          // Determine tenant ID for node communication
          Long nodeTenantId = nodeQuery.isTrailNode(nodeId) ? OWNER_TENANT_ID : debugDb.getTenantId();
          
          // Get local channel router for node communication
          ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, nodeTenantId);
          if (nonNull(router)) {
            // Execute on local controller
            RunnerRunDto runCmd = RunnerRunDto.newBuilder()
                .execId(String.valueOf(debugDb.getId())).script(debugDb.getScript())
                .testTask(debugDb.getScriptType().isTest()).debug(true)
                .build();
            RunnerRunVo result0 = pushRunCmd2Agent(runCmd, router);

            // Update execution status based on result
            debugDb.setStatus(result0.isSuccess() ? ExecStatus.COMPLETED : ExecStatus.FAILED)
                .setMessage(result0.getMessage()).setSchedulingResult(result0);

            // Analyze console output for setup, sampling, and teardown errors
            if (result0.isSuccess() && isNotEmpty(result0.getConsole())) {
              for (String c : debugDb.getSchedulingResult().getConsole()) {
                MeterStatus errorCode = MeterStatus.formLogKeyError(c);
                if (nonNull(errorCode)) {
                  String errorMessage = lengthSafe(c, 1000);
                  debugDb.setStatus(ExecStatus.FAILED).setMessage(errorMessage)
                      .setMeterStatus(errorCode.name()).setMeterMessage(errorMessage);
                  break;
                }
              }
            }
          } else {
            // Execute on remote controller
            if (broadcast) {
              // Get valid controller IP to node mapping
              Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
              if (isEmpty(ctrlIpNodeMap)) {
                throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
              }

              // Get service instances for remote communication
              List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
              if (isNotEmpty(instances)) {
                String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
                
                // Build debug start command for remote execution
                ExecDebugStartDto runCmd = ExecDebugStartDto.newBuilder()
                    .broadcast(false).id(debugDb.getId())
                    .name(debugDb.getName()).plugin(debugDb.getPlugin())
                    .configuration(debugDb.getConfiguration()).task(debugDb.getTask())
                    .build();
                
                boolean broadcastSuccess = false;
                for (ServiceInstance inst : instances) {
                  String broadcastInstanceIp = inst.getHost();
                  // Skip current controller and non-exchange server controllers
                  if (currentInstanceIp.equals(broadcastInstanceIp)
                      || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
                    continue;
                  }

                  // Send command to remote controller
                  String remoteUrl = "http://" + inst.getInstanceId() + EXEC_DEBUG_START_ENDPOINT;
                  RunnerRunVo result0 = broadcastRun2RemoteCtrl(runCmd, remoteUrl);
                  if (nonNull(result0)) {
                    // Update execution status based on remote result
                    debugDb.setStatus(result0.isSuccess() ? ExecStatus.COMPLETED : ExecStatus.FAILED)
                        .setMessage(result0.getMessage())
                        .setSchedulingResult(result0);
                    debugDb.setSchedulingResult(result0);
                    broadcastSuccess = true;
                    break;
                  }
                }
                
                // Handle broadcast failure
                if (!broadcastSuccess) {
                  String message = message(EXEC_AGENT_ROUTER_NOT_FOUND_T, new Object[]{nodeId});
                  RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
                  debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
                }
              } else {
                // Handle no service instances available
                String message = message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T, new Object[]{nodeId});
                log.error(message);
                RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
                debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
              }
            } else {
              // Handle remote controller ignored for targeted mode
              String message = message(EXEC_REMOTE_CONTROLLER_IGNORED_T, new Object[]{nodeId});
              log.error(message);
              RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
              debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
            }
          }

          // Save updated debug execution
          execDebugRepo.save(debugDb);

          // Set debug result and node info for broadcast mode
          if (broadcast) {
            execDebugQuery.setDebugResult(debugDb);
            setExecNodeInfo(nodeId, debugDb);
          }
        } finally {
          // Clean up principal context for non-user actions
          if (!isUserAction()) {
            PrincipalContext.remove();
          }
        }
        return debugDb;
        // formatter:on
      }
    }.execute();
  }

  /**
   * <p>
   * Start a debug execution by script.
   * </p>
   * <p>
   * Validates script existence, clears historical results, and delegates to start(boolean, ExecDebug).
   * Handles script parsing and debug execution setup for script-based debugging.
   * </p>
   * @param broadcast Whether to broadcast execution or target specific node
   * @param id Debug execution ID
   * @param scriptId Script ID to debug
   * @param scriptType Script type (optional override)
   * @param configuration Configuration (optional override)
   * @param arguments Arguments (optional override)
   * @return Debug execution entity with results
   */
  @Override
  public ExecDebug startByScript(boolean broadcast, Long id, @Nonnull Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Validate script exists and retrieve details
        script = scriptQuery.findById(scriptId);
        
        // Parse script content from YAML
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        // Set operation tenant from script
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        
        // Clear historical debugging results for this script
        deleteByScriptId(ExecDebugSource.SCRIPT, scriptId);

        // Create debug execution entity and start execution
        ExecDebug debug = startToDomain(ExecDebugSource.SCRIPT, id, null, scriptId,
            null, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  /**
   * <p>
   * Start a debug execution by scenario.
   * </p>
   * <p>
   * Validates script existence, clears historical results, and delegates to start(boolean, ExecDebug).
   * Handles script parsing and debug execution setup for scenario-based debugging.
   * </p>
   * @param broadcast Whether to broadcast execution or target specific node
   * @param id Debug execution ID
   * @param scenarioId Scenario ID
   * @param scriptId Script ID to debug
   * @param scriptType Script type (optional override)
   * @param configuration Configuration (optional override)
   * @param arguments Arguments (optional override)
   * @return Debug execution entity with results
   */
  @Override
  public ExecDebug startByScenario(boolean broadcast, Long id, Long scenarioId, Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Validate script exists and retrieve details
        script = scriptQuery.findById(scriptId);
        
        // Parse script content from YAML
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        // Set operation tenant from script
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        
        // Clear historical debugging results for this script
        deleteByScriptId(ExecDebugSource.SCENARIO, scriptId);

        // Create debug execution entity and start execution
        ExecDebug debug = startToDomain(ExecDebugSource.SCENARIO, id, scenarioId, scriptId,
            null, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  /**
   * <p>
   * Start a debug execution by monitor.
   * </p>
   * <p>
   * Validates script existence, clears historical results, and delegates to start(boolean, ExecDebug).
   * Handles script parsing, server configuration overrides, and debug execution setup for monitor-based debugging.
   * </p>
   * @param broadcast Whether to broadcast execution or target specific node
   * @param id Debug execution ID
   * @param monitorId Monitor ID
   * @param scenarioId Scenario ID
   * @param scriptId Script ID to debug
   * @param scriptType Script type (optional override)
   * @param configuration Configuration (optional override)
   * @param arguments Arguments (optional override)
   * @param servers Server configurations for HTTP overrides
   * @return Debug execution entity with results
   */
  @Override
  public ExecDebug startByMonitor(boolean broadcast, Long id, Long monitorId, Long scenarioId,
      Long scriptId, ScriptType scriptType, Configuration configuration, Arguments arguments,
      List<Server> servers) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Validate script exists and retrieve details
        script = scriptQuery.findById(scriptId);
        
        // Parse script content from YAML
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        // Set operation tenant from script
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        
        // Clear historical debugging results for this script
        deleteByScriptId(ExecDebugSource.MONITOR, scriptId);

        // Apply server configuration overrides for HTTP plugins only
        if (PLUGIN_HTTP_NAME.equals(script.getPlugin()) && isNotEmpty(servers)) {
          // Create server map for parameter overrides
          Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
              : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));
          
          // Override server parameters in configuration variables
          overrideExecServerParameter(serverMap, angusScript.getConfiguration().getVariables());
          
          // Override server parameters in HTTP pipelines
          if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
            for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
              if (pipeline instanceof Http) {
                overrideExecServerParameter(serverMap, (Http) pipeline);
              }
            }
          }
        }

        // Create debug execution entity and start execution
        ExecDebug debug = startToDomain(ExecDebugSource.MONITOR, id, scenarioId, scriptId,
            monitorId, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  /**
   * <p>
   * Add debug execution entity directly.
   * </p>
   * <p>
   * Sets tenant and user information for non-user actions and inserts the debug execution.
   * </p>
   * @param debug Debug execution entity to add
   */
  @Transactional(rollbackFor = Exception.class)
  public void add0(ExecDebug debug) {
    if (!isUserAction()) {
      // Set tenant and user information for system actions
      debug.setTenantId(getOptTenantId());
      debug.setCreatedBy(getUserId()).setCreatedDate(LocalDateTime.now());
    }
    insert(debug);
  }

  /**
   * <p>
   * Delete debug executions by script ID and source.
   * </p>
   * <p>
   * Removes debug executions and associated sample data for the specified script and source.
   * </p>
   * @param source Debug execution source (SCRIPT, SCENARIO, MONITOR)
   * @param scriptId Script ID
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteByScriptId(ExecDebugSource source, @Nonnull Long scriptId) {
    // Find debug execution IDs for the script and source
    List<Long> execIds = execDebugRepo.findIdBySourceAndScriptId(source.name(), scriptId);
    if (isNotEmpty(execIds)) {
      // Delete debug executions and associated sample data
      execDebugRepo.deleteByIdIn(execIds);
      deleteExecSamples(execIds);
    }
  }

  /**
   * <p>
   * Delete execution samples by execution IDs.
   * </p>
   * <p>
   * Removes all sample data associated with the specified execution IDs.
   * </p>
   * @param execIds Collection of execution IDs
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteExecSamples(Collection<Long> execIds) {
    // Delete execution samples, error causes, and content
    execSampleRepo.deleteByExecIdIn(execIds);
    execSampleErrorsRepo.deleteByExecIdIn(execIds);
    execSampleExtcRepo.deleteByExecIdIn(execIds);
  }

  /**
   * <p>
   * Set execution node information for debug execution.
   * </p>
   * <p>
   * Retrieves and sets node details for the debug execution.
   * </p>
   * @param nodeId Node ID
   * @param debugDb Debug execution entity
   */
  private void setExecNodeInfo(Long nodeId, ExecDebug debugDb) {
    // Retrieve node information for the execution
    List<Node> nodes = nodeQuery.getNodes(Set.of(nodeId), null, null, 1, debugDb.getTenantId());
    if (isNotEmpty(nodes)) {
      // Set node information in debug execution
      debugDb.setExecNode(toExecNodeInfo(nodes.get(0)));
    }
  }

  /**
   * <p>
   * Push run command to agent via channel router.
   * </p>
   * <p>
   * Sends execution command to node agent and handles response.
   * </p>
   * @param runCmd Run command DTO
   * @param router Channel router for communication
   * @return Execution result from agent
   */
  private RunnerRunVo pushRunCmd2Agent(RunnerRunDto runCmd, ChannelRouter router) {
    ReplyMessage result;
    try {
      // Send message to agent via router
      result = nodeInfoQuery.pushAgentMessage(RUNNER_RUN, runCmd, router);
    } catch (Exception e) {
      return RunnerRunVo.fail(runCmd.getExecId(), "Push agent message failure: " + e.getMessage());
    }
    if (result.isSuccess()) {
      // Parse successful response
      return JsonUtils.fromJson(result.getContent().toString(), RunnerRunVo.class);
    } else {
      // Handle failed response
      return RunnerRunVo.fail(runCmd.getExecId(), message(AGENT_PUSH_START_FAILED));
    }
  }

  /**
   * <p>
   * Broadcast run command to remote controller.
   * </p>
   * <p>
   * Sends debug start command to remote controller via HTTP and handles response.
   * </p>
   * @param dto Debug start DTO
   * @param remoteUrl Remote controller URL
   * @return Execution result from remote controller
   */
  private RunnerRunVo broadcastRun2RemoteCtrl(ExecDebugStartDto dto, String remoteUrl) {
    try {
      // Send HTTP POST request to remote controller
      Response response = doHttpPostRequest(dto, remoteUrl);
      if (response.isSuccessful()) {
        // Parse successful response
        ExecDebugDetailVo detailVo = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<ExecDebugDetailVo>>() {
            }).orElseContentThrow();
        return nonNull(detailVo) ? detailVo.getSchedulingResult() : null;
      } else {
        // Handle failed response
        ApiLocaleResult<?> result = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<?>>() {
            });
        return RunnerRunVo.fail(String.valueOf(dto.getId()), result.getMsg());
      }
    } catch (Throwable e) {
      // Handle communication exception
      String message = message(BROADCAST_START_TO_REMOTE_EXCEPTION_T,
          new Object[]{getMessage(e)});
      log.error(message);
      return RunnerRunVo.fail(String.valueOf(dto.getId()), message);
    }
  }

  /**
   * <p>
   * Select node by strategy.
   * </p>
   * <p>
   * Applies node selection strategy to choose an appropriate node for execution.
   * </p>
   * @param nodeSelector Node selector configuration
   * @param nodeNum Number of nodes to select
   * @return Selected node information
   */
  private NodeInfo selectNodeByStrategy(NodeSelector nodeSelector, int nodeNum) {
    NodeInfo nodeInfo;
    if (nonNull(nodeSelector)) {
      // Use node selector configuration for selection
      nodeInfo = nodeInfoQuery.selectByStrategy(nodeNum, nodeSelector.getAvailableNodeIds(),
          null, nodeSelector.getStrategy(), true).get(0);
    } else {
      // Use default selection strategy
      nodeInfo = nodeInfoQuery.selectByStrategy(nodeNum, null,
          null, null, true).get(0);
    }
    return nodeInfo;
  }

  /**
   * <p>
   * Assemble AngusScript from debug execution components.
   * </p>
   * <p>
   * Creates AngusScript object from debug execution configuration and task, then converts to YAML.
   * </p>
   * @param debug Debug execution entity
   * @return Assembled AngusScript object
   */
  public static AngusScript assembleAngusScript(ExecDebug debug) {
    // Build AngusScript from debug components
    AngusScript angusScript = new AngusScript().setType(debug.getScriptType())
        .setPlugin(debug.getPlugin()).setConfiguration(debug.getConfiguration())
        .setTask(debug.getTask());
    try {
      // Convert to YAML and set in debug execution
      debug.setScript(YAML_MAPPER.writeValueAsString(angusScript));
    } catch (JsonProcessingException e) {
      throw new SysException(e.getMessage());
    }
    return angusScript;
  }

  /**
   * <p>
   * Parse AngusScript from YAML content.
   * </p>
   * <p>
   * Converts YAML string to AngusScript object with error handling.
   * </p>
   * @param content YAML script content
   * @return Parsed AngusScript object
   */
  public static AngusScript parseAngusScript(String content) {
    try {
      return YAML_MAPPER.readValue(content, AngusScript.class);
    } catch (JsonProcessingException e) {
      throw SysException.of("Script format parsing error: {}", e.getMessage());
    }
  }

  @Override
  protected BaseRepository<ExecDebug, Long> getRepository() {
    return execDebugRepo;
  }
}
