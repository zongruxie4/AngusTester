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

  @Override
  public ExecDebug start(boolean broadcast, ExecDebug debug) {
    return new BizTemplate<ExecDebug>() {
      AngusScript angusScript;
      ExecDebug debugDb;

      @Override
      protected void checkParams() {
        if (broadcast) {
          // Check the execution quotas
          execDebugQuery.checkAddQuota(1);

          // Check the execution quota in running
          execDebugQuery.checkConcurrentTaskQuota(1);

          // Parse the script content
          if (nonNull(debug.getConfiguration()) && nonNull(debug.getTask())) {
            angusScript = assembleAngusScript(debug);
          } else if (isNotEmpty(debug.getScript())) {
            angusScript = parseAngusScript(debug.getScript());
          } else {
            throw new SysException("Debug script is empty");
          }

          // Check the available and application nodes is valid
          execQuery.checkNodeValid(angusScript.getConfiguration(), false);
        } else {
          // Check the id is required
          assertNotNull(debug.getId(), "Execution debug id is required when broadcast = false");
          debugDb = execDebugQuery.checkAndFind(debug.getId());
        }
      }

      @Override
      protected ExecDebug process() {
        // formatter:off
        log.info("Controller handle to start execution debug `{}` request", debug.getId());

        try {
          if (!isUserAction()) {
            PrincipalContext.set(createInnerPrincipal(appInfo));
          }

          // Randomly select a node from available nodes, with priority given to idle nodes
          Long nodeId;
          if (broadcast) {
            // Election testing node
            NodeSelector nodeSelector = angusScript.getConfiguration().getNodeSelectors();
            nodeId = selectNodeByStrategy(nodeSelector, 1).getId();
          } else {
            nodeId = debugDb.getExecNodeId();
          }

          // Save debugging records
          if (broadcast) {
            // NOOP: Not initiated based on existing scripts or scenarios
            // deleteByScriptIdAndScenarioId(debug.getScriptId(), debug.getScenarioId());

            debug.setExecNodeId(nodeId).setStatus(ExecStatus.RUNNING);
            add0(debug);
            debugDb = debug;
          }

          Long nodeTenantId = nodeQuery.isTrailNode(nodeId) ? OWNER_TENANT_ID : debugDb.getTenantId();
          ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, nodeTenantId);
          if (nonNull(router)) {
            // Push local controller
            RunnerRunDto runCmd = RunnerRunDto.newBuilder()
                .execId(String.valueOf(debugDb.getId())).script(debugDb.getScript())
                .testTask(debugDb.getScriptType().isTest()).debug(true)
                .build();
            RunnerRunVo result0 = pushRunCmd2Agent(runCmd, router);

            // Save success result
            debugDb.setStatus(result0.isSuccess() ? ExecStatus.COMPLETED : ExecStatus.FAILED)
                .setMessage(result0.getMessage()).setSchedulingResult(result0);

            // Judge setup, sampling and town-down error
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
            // Push remote controller
            if (broadcast) {
              Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
              if (isEmpty(ctrlIpNodeMap)) {
                throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
              }

              List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
              if (isNotEmpty(instances)) {
                String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
                // Send run command to node agent
                ExecDebugStartDto runCmd = ExecDebugStartDto.newBuilder()
                    .broadcast(false).id(debugDb.getId())
                    .name(debugDb.getName()).plugin(debugDb.getPlugin())
                    .configuration(debugDb.getConfiguration()).task(debugDb.getTask())
                    .build();
                boolean broadcastSuccess = false;
                for (ServiceInstance inst : instances) {
                  String broadcastInstanceIp = inst.getHost();
                  // Exclude current controller
                  if (currentInstanceIp.equals(broadcastInstanceIp)
                      // Exclude non exchange server controller
                      || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
                    continue;
                  }

                  String remoteUrl = "http://" + inst.getInstanceId() + EXEC_DEBUG_START_ENDPOINT;
                  RunnerRunVo result0 = broadcastRun2RemoteCtrl(runCmd, remoteUrl);
                  if (nonNull(result0)) {
                    // Save success result
                    debugDb.setStatus(result0.isSuccess() ? ExecStatus.COMPLETED : ExecStatus.FAILED)
                        .setMessage(result0.getMessage())
                        .setSchedulingResult(result0);
                    debugDb.setSchedulingResult(result0);
                    broadcastSuccess = true;
                    break;
                  }
                }
                if (!broadcastSuccess) {
                  String message = message(EXEC_AGENT_ROUTER_NOT_FOUND_T, new Object[]{nodeId});
                  RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
                  debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
                }
              } else {
                String message = message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T, new Object[]{nodeId});
                log.error(message);
                RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
                debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
              }
            } else {
              String message = message(EXEC_REMOTE_CONTROLLER_IGNORED_T, new Object[]{nodeId});
              log.error(message);
              RunnerRunVo result0 = RunnerRunVo.fail(String.valueOf(debugDb.getId()), nodeId, message);
              debugDb.setStatus(ExecStatus.FAILED).setMessage(message).setSchedulingResult(result0);
            }
          }

          execDebugRepo.save(debugDb);

          if (broadcast) {
            // Forcefully disable multi tenant control and allow querying trial nodes for testing
            PrincipalContext.get().setMultiTenantCtrl(false);
            execDebugQuery.setDebugResult(debugDb);
            setExecNodeInfo(nodeId, debugDb);
          }
        } finally {
          if (!isUserAction()) {
            PrincipalContext.remove();
          }
        }
        return debugDb;
        // formatter:on
      }
    }.execute();
  }

  @Override
  public ExecDebug startByScript(boolean broadcast, Long id, @Nonnull Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Check the script exists
        script = scriptQuery.findById(scriptId);
        // Parse the script content
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        // Clear historical debugging results
        deleteByScriptId(ExecDebugSource.SCRIPT, scriptId);

        ExecDebug debug = startToDomain(ExecDebugSource.SCRIPT, id, null, scriptId,
            null, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  @Override
  public ExecDebug startByScenario(boolean broadcast, Long id, Long scenarioId, Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Check the script exists
        script = scriptQuery.findById(scriptId);
        // Parse the script content
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        // Clear historical debugging results
        deleteByScriptId(ExecDebugSource.SCENARIO, scriptId);

        ExecDebug debug = startToDomain(ExecDebugSource.SCENARIO, id, scenarioId, scriptId,
            null, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  @Override
  public ExecDebug startByMonitor(boolean broadcast, Long id, Long monitorId, Long scenarioId,
      Long scriptId, ScriptType scriptType, Configuration configuration, Arguments arguments,
      List<Server> servers) {
    return new BizTemplate<ExecDebug>() {
      Script script;
      AngusScript angusScript;

      @Override
      protected void checkParams() {
        // Check the script exists
        script = scriptQuery.findById(scriptId);
        // Parse  the script content
        angusScript = parseAngusScript(script.getContent());
      }

      @Override
      protected ExecDebug process() {
        PrincipalContext.get().setOptTenantId(script.getTenantId());
        // Clear historical debugging results
        deleteByScriptId(ExecDebugSource.MONITOR, scriptId);

        // Check only HTTP servers is supported
        if (PLUGIN_HTTP_NAME.equals(script.getPlugin()) && isNotEmpty(servers)) {
          // Override exec server configuration parameter in variables
          Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
              : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));
          overrideExecServerParameter(serverMap, angusScript.getConfiguration().getVariables());
          if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
            for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
              if (pipeline instanceof Http) {
                overrideExecServerParameter(serverMap, (Http) pipeline);
              }
            }
          }
        }

        ExecDebug debug = startToDomain(ExecDebugSource.MONITOR, id, scenarioId, scriptId,
            monitorId, scriptType, configuration, arguments, script, angusScript);
        return start(broadcast, debug);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  public void add0(ExecDebug debug) {
    if (!isUserAction()) {
      debug.setTenantId(getOptTenantId());
      debug.setCreatedBy(getUserId()).setCreatedDate(LocalDateTime.now());
    }
    insert(debug);
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteByScriptId(ExecDebugSource source, @Nonnull Long scriptId) {
    List<Long> execIds = execDebugRepo.findIdBySourceAndScriptId(source.name(), scriptId);
    if (isNotEmpty(execIds)) {
      execDebugRepo.deleteByIdIn(execIds);
      deleteExecSamples(execIds);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteExecSamples(Collection<Long> execIds) {
    execSampleRepo.deleteByExecIdIn(execIds);
    execSampleErrorsRepo.deleteByExecIdIn(execIds);
    execSampleExtcRepo.deleteByExecIdIn(execIds);
  }

  private void setExecNodeInfo(Long nodeId, ExecDebug debugDb) {
    List<Node> nodes = nodeQuery.getNodes(Set.of(nodeId), null, null, 1, debugDb.getTenantId());
    if (isNotEmpty(nodes)) {
      debugDb.setExecNode(toExecNodeInfo(nodes.get(0)));
    }
  }

  private RunnerRunVo pushRunCmd2Agent(RunnerRunDto runCmd, ChannelRouter router) {
    ReplyMessage result;
    try {
      result = nodeInfoQuery.pushAgentMessage(RUNNER_RUN, runCmd, router);
    } catch (Exception e) {
      return RunnerRunVo.fail(runCmd.getExecId(), "Push agent message failure: " + e.getMessage());
    }
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), RunnerRunVo.class);
    } else {
      return RunnerRunVo.fail(runCmd.getExecId(), message(AGENT_PUSH_START_FAILED));
    }
  }

  private RunnerRunVo broadcastRun2RemoteCtrl(ExecDebugStartDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      if (response.isSuccessful()) {
        ExecDebugDetailVo detailVo = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<ExecDebugDetailVo>>() {
            }).orElseContentThrow();
        return nonNull(detailVo) ? detailVo.getSchedulingResult() : null;
      } else {
        ApiLocaleResult<?> result = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<?>>() {
            });
        return RunnerRunVo.fail(String.valueOf(dto.getId()), result.getMsg());
      }
    } catch (Throwable e) {
      String message = message(BROADCAST_START_TO_REMOTE_EXCEPTION_T,
          new Object[]{getMessage(e)});
      log.error(message);
      return RunnerRunVo.fail(String.valueOf(dto.getId()), message);
    }
  }

  private NodeInfo selectNodeByStrategy(NodeSelector nodeSelector, int nodeNum) {
    NodeInfo nodeInfo;
    if (nonNull(nodeSelector)) {
      nodeInfo = nodeInfoQuery.selectByStrategy(nodeNum, nodeSelector.getAvailableNodeIds(),
          null, nodeSelector.getStrategy(), true).get(0);
    } else {
      nodeInfo = nodeInfoQuery.selectByStrategy(nodeNum, null,
          null, null, true).get(0);
    }
    return nodeInfo;
  }

  public static AngusScript assembleAngusScript(ExecDebug debug) {
    AngusScript angusScript = new AngusScript().setType(debug.getScriptType())
        .setPlugin(debug.getPlugin()).setConfiguration(debug.getConfiguration())
        .setTask(debug.getTask());
    try {
      debug.setScript(YAML_MAPPER.writeValueAsString(angusScript));
    } catch (JsonProcessingException e) {
      throw new SysException(e.getMessage());
    }
    return angusScript;
  }

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
