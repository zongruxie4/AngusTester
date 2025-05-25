package cloud.xcan.angus.core.tester.application.cmd.exec.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.EXEC_MAX_FREE_NODES;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.SCRIPT_NO_AUTH_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.SCRIPT_NO_AUTH_T;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.configReplaceToExec;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.judgeUpdateTestResult;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.localScriptVoToExec;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.overwriteConfigScript;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.remoteScriptVoToExec;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.replaceOptionalConfig;
import static cloud.xcan.angus.core.tester.application.converter.ExecConverter.safeTrialConfiguration;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.toAngusAddScript;
import static cloud.xcan.angus.core.tester.application.query.node.impl.NodeInfoQueryImpl.doHttpPostRequest;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_NOT_RUNNING;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_START_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_STOP_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.BROADCAST_START_TO_REMOTE_EXCEPTION_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.BROADCAST_STOP_TO_REMOTE_EXCEPTION_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_AGENT_ROUTER_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_NODE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_START_EXCEPTION;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_STOP_EXCEPTION;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NOT_MEET_CONDITIONS_NODES;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_REMOTE_CONTROLLER_IGNORED_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_REMOTE_NODE_IS_REQUIRED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_START_IGNORED_WITH_PARSE_ERROR_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_START_IGNORED_WITH_SCRIPT_MISSING;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_START_IS_IGNORED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_START_UP_TIMEOUT;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_STOP_IGNORED_WITH_NO_NODES;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_STOP_IS_IGNORED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NODE_AGENT_UNAVAILABLE_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NO_AVAILABLE_NODES;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.parser.AngusParser.YAML_MAPPER;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.process.ProcessCommand.isStartException;
import static cloud.xcan.angus.spec.utils.DateUtils.formatByDateTimePattern;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.lengthSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;

import cloud.xcan.angus.agent.AgentCommandType;
import cloud.xcan.angus.agent.message.runner.RunnerRunDto;
import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopDto;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecNodeCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNode;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContentRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCauseRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleRepo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.result.command.AbstractCommandResult;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.NodeSelector;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.spec.experimental.DistributedLock;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Biz
public class ExecCmdImpl extends CommCmd<Exec, Long> implements ExecCmd {

  @Resource
  private ExecRepo execRepo;

  @Resource
  private ExecQuery execQuery;

  @Resource
  private ExecNodeCmd execNodeCmd;

  @Resource
  private ExecNodeRepo execNodeRepo;

  @Resource
  private ExecSampleRepo execSampleRepo;

  @Resource
  private ExecSampleErrorCauseRepo execSampleErrorsRepo;

  @Resource
  private ExecSampleContentRepo execSampleExtcRepo;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ScriptCmd scriptCmd;

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private DiscoveryClient discoveryClient;

  @Resource
  private ApplicationInfo appInfo;

  @Resource
  private DistributedLock distributedLock;

  private static final String EXEC_LOCK_KEY_FMT = "tester:exec:restart:%s";

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> addByLocalScriptContent(Long projectId, String name,
      String content, Boolean trial) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        String safeName = emptySafe(name, "Script" + formatByDateTimePattern(new Date()));
        AngusScript angusScript = YAML_MAPPER.readValue(content, AngusScript.class);
        IdKey<Long, Object> idKey = scriptCmd.add(toAngusAddScript(projectId, angusScript, content),
            true);
        return addByLocalScript(projectId, safeName, idKey.getId(), content, angusScript, trial);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> addByLocalScriptArgs(Long projectId, String name,
      ScriptType scriptType, String plugin, Configuration configuration, Task task, Boolean trial) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        AngusScript angusScript = AngusScript.newBuilder()
            .type(scriptType).plugin(plugin).configuration(configuration).task(task).build();
        String content = YAML_MAPPER.writeValueAsString(angusScript);
        String safeName = emptySafe(name, "Script" + formatByDateTimePattern(new Date()));
        IdKey<Long, Object> idKey = scriptCmd.add(toAngusAddScript(projectId, angusScript, content),
            true);
        return addByLocalScript(projectId, safeName, idKey.getId(), content, angusScript, trial);
      }
    }.execute();
  }

  @Override
  public IdKey<Long, Object> addByLocalScript(Long projectId, String name, Long scriptId,
      String script, AngusScript angusScript, Boolean trial) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the execution quotas
        execQuery.checkAddQuota(1, nonNull(trial) && trial);

        // Check the purchase or authorization quotas
        execQuery.checkThreadAndNodesQuota(trial, angusScript.getConfiguration());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Trial execution max quota limit: 1 node, 10000 threads, duration 30 minutes, 100000 iterations.
        if (nonNull(trial) && trial) {
          safeTrialConfiguration(angusScript.getConfiguration());
        }
        return add0(localScriptVoToExec(projectId, name, scriptId, script, angusScript, trial));
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> addByRemoteScript(String name, Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments, Boolean trial) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Script script;
      AngusScript angusScript;
      Exec execDb;

      @Override
      protected void checkParams() {
        // Check the script exists
        script = scriptQuery.detail(scriptId);

        // Check the test permission
        BizAssert.assertTrue(!isUserAction() || isEmpty(script.getPermissions())
                || script.getPermissions().contains(ScriptPermission.TEST),
            SCRIPT_NO_AUTH_CODE, SCRIPT_NO_AUTH_T, new Object[]{ScriptPermission.TEST});

        // Query existed execution
        // Important: Different test types of apis correspond to different scripts and executions,
        // but multiple test types in a scenario correspond to one script and multiple executions.
        execDb = execQuery.findByScript(scriptId, script.getType(), script.getSource());

        // Add new exec
        if (isNull(execDb)) {
          // Check execution quotas
          execQuery.checkAddQuota(1, nonNull(trial) && trial);
        }

        // Check the purchase or authorization quotas
        execQuery.checkThreadAndNodesQuota(trial, configuration);

        // Parse the script content
        try {
          angusScript = YAML_MAPPER.readValue(script.getContent(), AngusScript.class);
        } catch (JsonProcessingException e) {
          throw SysException.of("Failed to parse script content: " + e.getMessage());
        }

        // Check the available nodes exists -> Do in add()
        // execQuery.checkNodeValid(nonNull(configuration) ? configuration :
        //   angusScript.getConfiguration());
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (nonNull(configuration)) {
          angusScript.setConfiguration(configuration);
        }

        if (isNotEmpty(arguments)) {
          angusScript.getTask().setArguments(arguments);
        }

        if (nonNull(scriptType)) {
          angusScript.setType(scriptType);
        }

        // Trial execution max quota limit: 1 node, 10000 threads, duration 30 minutes, 100000 iterations.
        if (nonNull(trial) && trial) {
          safeTrialConfiguration(angusScript.getConfiguration());
        }

        // Whether to synchronize configuration changes to the script <- NOOP

        // Add new exec
        try {
          if (isNull(execDb)) {
            Exec exec = remoteScriptVoToExec(name, scriptType, script, angusScript, trial);
            return add0(exec);
          }

          // Fix: The configuration in the script may also be modified
          execDb = configReplaceToExec(execDb, name, scriptType, angusScript.getConfiguration(),
              angusScript);

          // Note: Repeatedly starting and running tasks will continue to execute
          if (!execDb.getStatus().isRunning()) {
            execDb.setStatus(ExecStatus.PENDING);
          }
          execDb.setScript(YAML_MAPPER.writeValueAsString(angusScript));
          execRepo.save(execDb);
        } catch (Exception e) {
          // Handle AngusException or JsonProcessingException
          throw ProtocolException.of(e.getMessage());
        }
        return new IdKey<>(execDb.getId(), execDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void configReplace(Long id, String name, Long iterations, TimeValue duration,
      Integer thread, Integer priority, Boolean ignoreAssertions, Boolean updateTestResult,
      StartMode startMode, LocalDateTime startAtDate, TimeValue reportInterval) {
    new BizTemplate<Void>() {
      Exec execDb;

      @Override
      protected void checkParams() {
        // Check and find exec
        execDb = execQuery.checkAndFind(id);

        // Check the exec permission
        execQuery.checkPermission(execDb);

        // TODO Check purchase or authorization quotas
      }

      @Override
      protected Void process() {
        replaceOptionalConfig(execDb, name, iterations, duration, thread, priority,
            ignoreAssertions, startMode, startAtDate, reportInterval);

        String script = overwriteConfigScript(execDb, execDb.getThread(),
            null, null, execDb.getIterations());
        execDb.setScript(script);

        judgeUpdateTestResult(execDb, updateTestResult);

        execRepo.save(execDb);

        // Whether to synchronize configuration changes to the script <- NOOP
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptConfigReplace(Long id, String name, ScriptType scriptType,
      Configuration configuration, Arguments arguments) {
    new BizTemplate<Void>() {
      Exec execDb;
      AngusScript angusScript;

      @SneakyThrows
      @Override
      protected void checkParams() {
        // Check and find exec
        execDb = execQuery.checkAndFind(id);
        // Check the exec permission
        execQuery.checkPermission(execDb);
        // Parse the script content
        angusScript = YAML_MAPPER.readValue(execDb.getScript(), AngusScript.class);
        // Check the available nodes exists
        execQuery.checkNodeValid(configuration, execDb.isTrial());
      }

      @SneakyThrows
      @Override
      protected Void process() {
        angusScript.setType(scriptType);
        angusScript.setConfiguration(configuration);
        angusScript.getTask().setArguments(arguments);

        execDb.setScript(YAML_MAPPER.writeValueAsString(angusScript));

        execDb = configReplaceToExec(execDb, name, scriptType, configuration, angusScript);

        execRepo.save(execDb);

        // Whether to synchronize configuration changes to the script <- NOOP

        return null;
      }
    }.execute();
  }

  @Override
  public IdKey<Long, Object> add0(Exec exec) {
    return insert(exec);
  }

  //@Transactional(rollbackFor = Exception.class)
  @Override
  public List<RunnerRunVo> start(ExecStartDto dto) {
    return new BizTemplate<List<RunnerRunVo>>() {
      Exec execDb;

      @Override
      protected void checkParams() {
        // Check the exec exists
        execDb = execQuery.checkAndFind(dto.getId());

        // Set the operation tenant
        if (PrincipalContextUtils.isJobOrInnerApi()) {
          PrincipalContext.get().setOptTenantId(execDb.getTenantId());
        }

        if (dto.isBroadcast()) {
          // Check execution quota in running
          execQuery.checkConcurrentTaskQuota(1, execDb.isTrial());
          // Check exec is not running, only on the main controller (Broadcast=true)
          execQuery.checkNotRunning(execDb);
          // Check exec permission
          execQuery.checkPermission(execDb);
          // Check the number of concurrent tasks
        } else {
          // Check selected nodes is required
          assertNotEmpty(dto.getRemoteNodeIds(), message(EXEC_REMOTE_NODE_IS_REQUIRED));
        }
      }

      @Override
      protected List<RunnerRunVo> process() {
        return start0(execDb, dto);
      }
    }.execute();
  }

  @SneakyThrows
  // @Transactional(rollbackFor = Exception.class) -> Lock wait timeout exceeded; try restarting transaction
  @Override
  public List<RunnerRunVo> start0(Exec execDb, ExecStartDto dto) {
    String execId = String.valueOf(execDb.getId());
    List<RunnerRunVo> results = new ArrayList<>();

    // Lock to prevent repeated restart
    // Lock only on the main controller (Broadcast=true)
    if (dto.isBroadcast()) {
      boolean locked = distributedLock.tryLock(format(EXEC_LOCK_KEY_FMT, execId), execId,
          2, TimeUnit.MINUTES);
      if (!locked) {
        // Execution is triggered by other threads during scheduling
        String message = message(EXEC_START_IS_IGNORED);
        log.warn(message);
        results.add(RunnerRunVo.fail(execId, message));
        //execRepo.updateSchedulingFailed(execId, ExecStatus.PENDING, LocalDateTime.now(), results);
        return results;
      }
    }

    // Check execution quota in running for job
    try {
      execQuery.checkConcurrentTaskQuota0(1, execDb.isTrial());
    } catch (Exception e) {
      updateSchedulingFailed(execId, results, e.getMessage());
      return results;
    }

    // Parsing and overwrite script configuration
    String script;
    try {
      script = overwriteConfigScript(execDb, execDb.getThread(),
          null, null, execDb.getIterations());
    } catch (Exception e) {
      updateSchedulingFailed(execId, results, message(EXEC_START_IGNORED_WITH_PARSE_ERROR_T,
          new Object[]{e.getMessage()}));
      return results;
    }

    // Check the script content exists
    if (isEmpty(execDb.getScript())) {
      updateSchedulingFailed(execId, results, message(EXEC_START_IGNORED_WITH_SCRIPT_MISSING));
      return results;
    }

    try {
      if (dto.isBroadcast()) {
        // Check that the pipeline is valid
        if (isNotEmpty(execDb.getTask().getPipelines())
            && execDb.getTask().getPipelines().size() > 1) {
          try {
            PipelineBuilder.of(execDb.getTask().getPipelines());
          } catch (Exception e) {
            updateSchedulingFailed(execId, results, e.getMessage());
            return results;
          }
        }
      } else {
        // Ignore locked executions in this startup to prevent data inconsistency! Important!!
        String locked0 = distributedLock.get(format(EXEC_LOCK_KEY_FMT, execId));
        if (isEmpty(locked0)) {
          updateSchedulingFailed(execId, results, message(EXEC_START_UP_TIMEOUT));
          return results;
        }
      }

      // Select and check available nodes
      LinkedHashSet<Long> nodeIds = new LinkedHashSet<>();
      String selectFailMessage = null;
      if (dto.isBroadcast()) {
        // Otherwise, it will be set as pending and waiting for job scheduling
        // Note：Throw an exception when the node is not satisfied.
        try {
          if (nonNull(execDb.getTrial()) && execDb.getTrial()) {
            if (isUserAction()) {
              // Query idle shared nodes during trial execution
              nodeIds.addAll(selectFreeNode(execDb));
            } else {
              try {
                // Query idle shared nodes during trial execution
                nodeIds.addAll(selectFreeNode(execDb));
              } catch (Exception e) {
                // Ignore NO_AVAILABLE_NODES message
                // If there are no public trial nodes, use the tenant's own nodes
                if (nodeIds.isEmpty()) {
                  nodeIds.addAll(selectNodeByStrategy(execDb));
                }
              }
            }
          } else {
            nodeIds.addAll(selectNodeByStrategy(execDb));
          }
        } catch (Exception e) {
          selectFailMessage = e.getMessage();
          log.error("No execution nodes that meet the policy", e);
          if (e instanceof BizException || execDb.getTrial()) {
            // BizException will terminate and continue scheduling.
            updateSchedulingFailed(execId, results, nullSafe(selectFailMessage,
                message(EXEC_NOT_MEET_CONDITIONS_NODES)));
            return results;
          }
        }
      } else {
        nodeIds.addAll(dto.getRemoteNodeIds());
      }

      if (isEmpty(nodeIds)) {
        updateSchedulingFailedInPending(execId, results, nullSafe(selectFailMessage,
            message(EXEC_NOT_MEET_CONDITIONS_NODES)));
        return results;
      }

      // @DoInFuture("If the selected node successfully locks the node and runs the task")

      // Update to Scheduling
      execRepo.updatePendingStatusById(execDb.getId());

      // Delete old execution records only on the main controller (Broadcast=true), prevent duplicate execution results
      if (dto.isBroadcast()) {
        deleteExecSamplesAndNode(execDb.getId());
      }

      log.info("Controller handle to start execution `{}` request", execId);
      LinkedHashSet<Long> remoteNodeIds = new LinkedHashSet<>();
      LinkedHashSet<Long> successNodeIds = new LinkedHashSet<>();
      Long lastNodeId = dto.getLastNodeId();

      if (nodeIds.size() == 1) {
        Long nodeId = nodeIds.stream().findFirst().get();
        startSingleNodeTask(null, execDb, execId, results, script, nodeId, remoteNodeIds,
            successNodeIds);
      } else {
        ExecutorService executorService = Executors.newFixedThreadPool(nodeIds.size());
        CountDownLatch latch = new CountDownLatch(nodeIds.size());
        // Set sharding field value
        execDb.setOrgThread(execDb.getAngusScript().getConfiguration().getThread());
        int i = 0;
        for (Long nodeId : nodeIds) {
          i++;
          if (isNull(lastNodeId) && i == nodeIds.size()) {
            lastNodeId = nodeId;
          }

          script = shardingScript(execDb, nodeIds.size(), i == 0);
          String finalScript = script;
          executorService.submit(() -> {
            startSingleNodeTask(latch, execDb, execId, results, finalScript, nodeId,
                remoteNodeIds, successNodeIds);
          });
        }
        latch.await();
      }

      // Stop scheduling when scheduling execution fails
      if (isNotEmpty(results) && results.stream().noneMatch(AbstractCommandResult::isSuccess)) {
        updateSchedulingFailed(execId, results, results.get(0).getMessage());
        return results;
      }

      // Push cmd to remote controller only on the main controller (Broadcast=true)
      if (!remoteNodeIds.isEmpty()) {
        if (dto.isBroadcast()) {
          Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
          if (isEmpty(ctrlIpNodeMap)) {
            throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
          }

          List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
          if (isNotEmpty(instances)) {
            String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
            ExecStartDto remoteRunCmd = new ExecStartDto()
                .setBroadcast(false) // Only broadcast once on the first controller
                .setId(dto.getId()).setRemoteNodeIds(remoteNodeIds).setLastNodeId(lastNodeId);
            for (ServiceInstance inst : instances) {
              String broadcastInstanceIp = inst.getHost();
              // Exclude current controller
              if (currentInstanceIp.equals(broadcastInstanceIp)
                  // Exclude non exchange server controller
                  || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
                continue;
              }

              String remoteStartUrl = "http://" + inst.getInstanceId() + EXEC_START_ENDPOINT;
              List<RunnerRunVo> remoteResults = broadcastRun2RemoteCtrl(remoteRunCmd,
                  remoteStartUrl);
              if (isNotEmpty(remoteResults)) {
                results.addAll(remoteResults);
                for (RunnerRunVo result0 : remoteResults) {
                  if (result0.isSuccess()) {
                    successNodeIds.add(result0.getDeviceId());
                  }
                }
              }
            }
            if (isEmpty(successNodeIds)) {
              results.addAll(remoteNodeIds.stream().map(x ->
                      RunnerRunVo.fail(execId, x, message(EXEC_AGENT_ROUTER_NOT_FOUND)))
                  .toList());
            }
          } else {
            String message = message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T,
                new Object[]{getStringJoiner(remoteNodeIds).toString()});
            log.error(message);
            results.add(RunnerRunVo.fail(execId, message));
          }
        } else {
          String message = message(EXEC_REMOTE_CONTROLLER_IGNORED_T,
              new Object[]{getStringJoiner(remoteNodeIds).toString()});
          log.error(message);
          results.add(RunnerRunVo.fail(execId, message));
        }
      }

      if (dto.isBroadcast()) {
        saveStartExecStatus(execDb, results, successNodeIds);
        saveExecNodes(execDb, successNodeIds);
      }
    } finally {
      distributedLock.releaseLock(format(EXEC_LOCK_KEY_FMT, execId), execId);
    }
    return results;
  }

  @Override
  public List<RunnerStopVo> stop(ExecStopDto dto) {
    return new BizTemplate<List<RunnerStopVo>>() {
      ExecInfo execDb;

      @Override
      protected void checkParams() {
        // Check exec exists
        execDb = execQuery.checkAndFindInfo(dto.getId());

        // Set operation tenant
        if (PrincipalContextUtils.isJobOrInnerApi()) {
          PrincipalContext.get().setOptTenantId(execDb.getTenantId());
        }

        if (dto.isBroadcast()) {
          // Check exec is not stopped, only on the main controller (Broadcast=true)
          execQuery.checkNotStopped(execDb);
          // Check exec permission
          execQuery.checkPermissionInfo(execDb);
        } else {
          // Check selected nodes is required
          assertNotEmpty(dto.getRemoteNodeIds(),
              "Selected remote node ids is required when broadcast = false");
        }
      }

      @Override
      protected List<RunnerStopVo> process() {
        return stop0(execDb, dto);
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<RunnerStopVo> stop0(ExecInfo execDb, ExecStopDto dto) {
    String execId = String.valueOf(execDb.getId());
    List<RunnerStopVo> results = new ArrayList<>();

    // Ignore locked executions in this startup to prevent data inconsistency! Important!!
    String locked = distributedLock.get(format(EXEC_LOCK_KEY_FMT, execId));
    if (isNotEmpty(locked)) {
      String message = message(EXEC_STOP_IS_IGNORED);
      log.warn(message);
      results.add(RunnerStopVo.fail(execId, message));
      return results;
    }

    // Select and check available nodes, Note:
    LinkedHashSet<Long> nodeIds = new LinkedHashSet<>();
    if (dto.isBroadcast()) {
      Set<Long> execNodeIds = execNodeRepo.findByExecId(execDb.getId()).stream()
          .map(ExecNode::getNodeId).collect(Collectors.toSet());
      if (isNotEmpty(execNodeIds)) {
        nodeIds.addAll(execNodeIds);
      } else {
        String message = message(EXEC_STOP_IGNORED_WITH_NO_NODES);
        log.warn(message);
        results.add(RunnerStopVo.fail(execId, message));
        execRepo.updateStoppedStatusById(execDb.getId(), now(), getUserId());
        return results;
      }
    } else {
      nodeIds.addAll(dto.getRemoteNodeIds());
    }

    // If the selected node successfully locks the node and runs the task
    log.info("Controller handle to stop execution request, execId: {}", execId);

    LinkedHashSet<Long> remoteNodeIds = new LinkedHashSet<>();
    LinkedHashSet<Long> successNodeIds = new LinkedHashSet<>();
    boolean isLocalRouter = false;
    Long realTenantId = execDb.getTrial() ? OWNER_TENANT_ID : execDb.getTenantId();
    for (Long nodeId : nodeIds) {
      // Push local controller
      ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, realTenantId);
      if (nonNull(router)) {
        isLocalRouter = true;
        try {
          // Send run command to node agent
          RunnerStopDto runCmd = RunnerStopDto.newBuilder().execId(execId).build();
          RunnerStopVo result0 = pushStopCmd2Agent(runCmd, router);
          results.add(result0);
          if (result0.isSuccess()) {
            if (!Objects.equals(nodeId, result0.getDeviceId())) {
              log.error("The scheduling node `{}` and execution node `{}` are inconsistent",
                  nodeId, result0.getDeviceId());
            }
          }
        } catch (Exception e) {
          String message = message(EXEC_CONTROLLER_STOP_EXCEPTION,
              new Object[]{isLocalRouter, getMessage(e)});
          log.error(message);
          results.add(RunnerStopVo.fail(execId, nodeId, message));
        }
      }
      if (!isLocalRouter) {
        remoteNodeIds.add(nodeId);
      }
      // Reset
      isLocalRouter = false;
    }

    // Push remote controller
    if (!remoteNodeIds.isEmpty()) {
      if (dto.isBroadcast()) {
        Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
        if (isEmpty(ctrlIpNodeMap)) {
          throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
        if (isNotEmpty(instances)) {
          String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
          ExecStopDto remoteRunCmd = new ExecStopDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setId(dto.getId()).setRemoteNodeIds(remoteNodeIds);
          for (ServiceInstance inst : instances) {
            String broadcastInstanceIp = inst.getHost();
            // Exclude current controller
            if (currentInstanceIp.equals(broadcastInstanceIp)
                // Exclude non exchange server controller
                || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
              continue;
            }
            String remoteStartUrl = "http://" + inst.getInstanceId() + EXEC_STOP_ENDPOINT;
            List<RunnerStopVo> remoteResults = broadcastStop2RemoteCtrl(remoteRunCmd,
                remoteStartUrl);
            if (isNotEmpty(remoteResults)) {
              results.addAll(remoteResults);
              for (RunnerStopVo result0 : remoteResults) {
                if (result0.isSuccess()) {
                  successNodeIds.add(result0.getDeviceId());
                }
              }
            }
          }
          if (isEmpty(successNodeIds)) {
            results.addAll(remoteNodeIds.stream().map(x ->
                    RunnerStopVo.fail(execId, x, message(EXEC_AGENT_ROUTER_NOT_FOUND)))
                .toList());
          }
        } else {
          String message = message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T,
              new Object[]{getStringJoiner(remoteNodeIds).toString()});
          log.error(message);
          results.add(RunnerStopVo.fail(execId, message));
        }
      } else {
        String message = message(EXEC_REMOTE_CONTROLLER_IGNORED_T,
            new Object[]{getStringJoiner(remoteNodeIds).toString()});
        log.error(message);
        results.add(RunnerStopVo.fail(execId, message));
      }
    }

    // Update exec info and status
    // Fix: To prevent being stopped or due to a short execution time, the ExecMetricsMessage Handler has been updated to stop the task
    if (!execQuery.checkAndFindInfo(execDb.getId()).getStatus().isWideStopped()) {
      boolean allSuccess = results.stream().allMatch(RunnerStopVo::isSuccess);
      if (allSuccess) {
        execRepo.updateStoppedStatusById(execDb.getId(), now(), getUserId());
      } else {
        execRepo.updateStatusById(execDb.getId(), ExecStatus.RUNNING.getValue(),
            getUserId(), now());
      }
    }

    if (isNotEmpty(successNodeIds)) {
      execNodeRepo.deleteByNodeIdIn(successNodeIds);
    }
    return results;
  }

  @Override
  public void delete(LinkedHashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<ExecInfo> execsDb;

      @Override
      protected void checkParams() {
        // Check exec exists
        execsDb = execQuery.checkAndFindInfo(ids);
        // Check exec permission
        for (ExecInfo execInfo : execsDb) {
          execQuery.checkPermissionInfo(execInfo);
        }
      }

      @Override
      protected Void process() {
        for (ExecInfo exec : execsDb) {
          try {
            // Ignore locked executions in this startup to prevent data inconsistency! Important!!
            String locked = distributedLock.get(format(EXEC_LOCK_KEY_FMT, exec.getId()));
            if (isNotBlank(locked)) {
              log.warn("Ignore deletion, execution `{}` is triggered and during scheduling",
                  exec.getId());
              continue;
            }

            if (exec.getStatus().isRunning()) {
              stop(new ExecStopDto().setBroadcast(true).setId(exec.getId()));
            }
            execRepo.deleteById(exec.getId());
            // Fix:: execTargetRepo.deleteByExecId(exec.getId()); -> Deleting it will result in missing test result relationships
          } catch (Exception e) {
            log.error("Delete execution exception, cause: {}", e.getMessage());
          }
        }
        return null;
      }
    }.execute();
  }

  private StringJoiner getStringJoiner(LinkedHashSet<Long> remoteNodeIds) {
    StringJoiner joiner = new StringJoiner(",");
    for (Long remoteNodeId : remoteNodeIds) {
      joiner.add(remoteNodeId.toString());
    }
    return joiner;
  }

  private void startSingleNodeTask(CountDownLatch latch, Exec execDb, String execId,
      List<RunnerRunVo> results, String script, Long nodeId, LinkedHashSet<Long> remoteNodeIds,
      LinkedHashSet<Long> successNodeIds) {
    boolean isLocalRouter = false;

    // Push local controller
    Long realTenantId = execDb.getTrial() ? OWNER_TENANT_ID : execDb.getTenantId();
    ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, realTenantId);
    if (nonNull(router)) {
      isLocalRouter = true;
      try {
        // Send run command to node agent
        RunnerRunDto runCmd = RunnerRunDto.newBuilder()
            .execId(execId).script(script).testTask(execDb.getScriptType().isTest())
            .build();
        RunnerRunVo result0 = pushRunCmd2Agent(runCmd, nodeId, realTenantId, router);
        results.add(result0);
        if (result0.isSuccess()) {
          successNodeIds.add(result0.getDeviceId());
          if (!Objects.equals(nodeId, result0.getDeviceId())) {
            log.error("The scheduling node `{}` and execution node `{}` are inconsistent",
                nodeId, result0.getDeviceId());
          }
        }
      } catch (Exception e) {
        String message = message(EXEC_CONTROLLER_START_EXCEPTION,
            new Object[]{isLocalRouter, getMessage(e)});
        log.error(message);
        results.add(RunnerRunVo.fail(execId, nodeId, message));
      }
    }
    if (!isLocalRouter) {
      remoteNodeIds.add(nodeId);
    }
    if (nonNull(latch)) {
      latch.countDown();
    }
  }

  private void updateSchedulingFailed(String execId, List<RunnerRunVo> results, String cause) {
    log.warn(cause);
    results.add(RunnerRunVo.fail(execId, cause));
    execRepo.updateSchedulingFailed(Long.valueOf(execId), ExecStatus.FAILED.getValue(), now(),
        ObjectMapperWrapper.INSTANCE.toString(results), getUserId(), now());
  }

  private void updateSchedulingFailedInPending(String execId, List<RunnerRunVo> results,
      String message) {
    // If there are no available nodes, display the election results to the front-end (not meeting the nodes) and update them to pending execution
    results.add(RunnerRunVo.fail(execId, message));
    execRepo.updateSchedulingFailed(Long.valueOf(execId), ExecStatus.PENDING.getValue(), now(),
        ObjectMapperWrapper.INSTANCE.toString(results), getUserId(), now());
  }

  private void saveExecNodes(Exec exec, LinkedHashSet<Long> runSuccessNodeIds) {
    if (isNotEmpty(runSuccessNodeIds)) {
      List<Long> existIds = execNodeRepo.findNodeIdByNodeIdIn(runSuccessNodeIds);
      List<Long> saveIds = new ArrayList<>(runSuccessNodeIds);
      saveIds.removeAll(existIds);
      execNodeCmd.add0(saveIds.stream()
          .map(x -> new ExecNode().setExecId(exec.getId()).setNodeId(x))
          .collect(Collectors.toList()));
    }
  }

  private void saveStartExecStatus(Exec execDb, List<RunnerRunVo> results,
      LinkedHashSet<Long> successNodeIds) {
    // Fix: To prevent being stopped or due to a short execution time, the ExecMetricsMessage Handler has been updated to complete the task
    ExecStatus execStatus = execQuery.checkAndFindInfo(execDb.getId()).getStatus();
    judgeExecStatus(execDb, results, execStatus);

    if (execDb.getStatus().isRunning()) {
      execDb.setMeterStatus(null).setMeterMessage(null);
    }

    if (nonNull(execDb.getUpdateTestResult()) && execDb.getUpdateTestResult()) {
      judgeUpdateTestResult(execDb, true);
    }

    execDb.setExecBy(isUserAction() ? getUserId() : execDb.getCreatedBy())
        .setSchedulingNum(execDb.getSchedulingNum() + 1)
        .setLastSchedulingDate(now())
        .setLastSchedulingResult(results)
        .setActualStartDate(now())
        .setEndDate(null) // Restarted
        .setExecNodeIds(successNodeIds)
        .setAssembleAndSendEvent(null);
    execRepo.save(execDb);
  }

  private void judgeExecStatus(Exec execDb, List<RunnerRunVo> results, ExecStatus execStatus) {
    boolean hasSuccess = results.stream().anyMatch(RunnerRunVo::isSuccess);
    boolean exceptionExit = results.stream().allMatch(x -> isStartException(x.getExitCode()));
    if (hasSuccess) {
      execDb.setStatus(ExecStatus.RUNNING);
    } else if (exceptionExit) {
      execDb.setStatus(ExecStatus.FAILED);
      for (RunnerRunVo result : results) {
        if (AbstractCommandResult.FAILED.equals(result.getMessage())
            && nonNull(result.getConsole())) {
          result.setMessage(lengthSafe(result.getConsole().get(result.getConsole().size() - 1),
              1000));
        }
      }
    } else {
      if (execStatus.isPending()) {
        execDb.setStatus(ExecStatus.PENDING);
      } else {
        execDb.setStatus(execStatus);
      }
    }
  }

  private void deleteExecSamplesAndNode(Long execId) {
    execSampleRepo.deleteByExecId(execId);
    execSampleErrorsRepo.deleteByExecId(execId);
    execSampleExtcRepo.deleteByExecId(execId);
    execNodeRepo.deleteByExecId(execId);
  }

  private RunnerRunVo pushRunCmd2Agent(RunnerRunDto runCmd, Long nodeId, Long realTenantId,
      ChannelRouter router) throws Exception {
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.RUNNER_RUN, runCmd,
        router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), RunnerRunVo.class);
    } else {
      return RunnerRunVo.fail(runCmd.getExecId(), message(AGENT_PUSH_START_FAILED));
    }
  }

  private List<RunnerRunVo> broadcastRun2RemoteCtrl(ExecStartDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      if (response.isSuccessful()) {
        List<RunnerRunVo> runVos = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<List<RunnerRunVo>>>() {
            }).orElseContentThrow();
        return isNotEmpty(runVos) ? runVos : dto.getRemoteNodeIds().stream()
            .map(x -> RunnerRunVo.fail(String.valueOf(dto.getId()), x, message(AGENT_NOT_RUNNING)))
            .collect(Collectors.toList());
      } else {
        ApiLocaleResult<?> result = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<?>>() {
            });
        return dto.getRemoteNodeIds().stream()
            .map(x -> RunnerRunVo.fail(String.valueOf(dto.getId()), x, result.getMsg()))
            .collect(Collectors.toList());
      }
    } catch (Throwable e) {
      String message = message(BROADCAST_START_TO_REMOTE_EXCEPTION_T, new Object[]{getMessage(e)});
      log.error(message);
      return dto.getRemoteNodeIds().stream()
          .map(x -> RunnerRunVo.fail(String.valueOf(dto.getId()), x, message))
          .collect(Collectors.toList());
    }
  }

  private RunnerStopVo pushStopCmd2Agent(RunnerStopDto runCmd, ChannelRouter router)
      throws Exception {
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.RUNNER_STOP, runCmd,
        router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), RunnerStopVo.class);
    } else {
      return RunnerStopVo.fail(runCmd.getExecId(), message(AGENT_PUSH_STOP_FAILED));
    }
  }

  private List<RunnerStopVo> broadcastStop2RemoteCtrl(ExecStopDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      if (response.isSuccessful()) {
        List<RunnerStopVo> stopVos = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<List<RunnerStopVo>>>() {
            }).orElseContentThrow();
        return isNotEmpty(stopVos) ? stopVos : dto.getRemoteNodeIds().stream()
            .map(x -> RunnerStopVo.fail(String.valueOf(dto.getId()), x, message(AGENT_NOT_RUNNING)))
            .collect(Collectors.toList());
      } else {
        ApiLocaleResult<?> result = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<?>>() {
            });
        return dto.getRemoteNodeIds().stream()
            .map(x -> RunnerStopVo.fail(String.valueOf(dto.getId()), x, result.getMsg()))
            .collect(Collectors.toList());
      }
    } catch (Throwable e) {
      String message = message(BROADCAST_STOP_TO_REMOTE_EXCEPTION_T, new Object[]{getMessage(e)});
      log.error(message);
      return dto.getRemoteNodeIds().stream()
          .map(x -> RunnerStopVo.fail(String.valueOf(dto.getId()), x, message))
          .collect(Collectors.toList());
    }
  }

  private List<Long> selectFreeNode(Exec execDb) {
    boolean isMultiTenantCtrl = isMultiTenantCtrl();
    long realTenantId = getOptTenantId();
    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(false);
      PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
    }

    List<NodeInfo> selectNodes = nodeInfoQuery.selectWithFree(execDb.getId(),
        1, execDb.getAvailableNodeIds());
    assertTrue(isNotEmpty(selectNodes), message(NO_AVAILABLE_NODES));

    List<Long> selectNodeIds = selectNodes.stream().map(NodeInfo::getId)
        .collect(Collectors.toList());
    Set<Long> liveNodeIds = nodeInfoQuery.getLiveNodeIds(selectNodeIds);
    for (NodeInfo selectNode : selectNodes) {
      BizAssert.assertTrue(liveNodeIds.contains(selectNode.getId()),
          message(NODE_AGENT_UNAVAILABLE_T, new Object[]{selectNode.getId()}));
    }

    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(true);
      PrincipalContext.get().setOptTenantId(realTenantId);
    }
    return selectNodeIds;
  }

  private List<Long> selectNodeByStrategy(Exec execDb) {
    NodeSelector nodeSelector = execDb.getConfiguration().getNodeSelectors();
    List<NodeInfo> selectNodes = nodeInfoQuery.selectByStrategy(execDb.getId(),
        isNull(nodeSelector) || isNull(nodeSelector.getNum())
            ? 1 : nodeSelector.getNum(), execDb.getAvailableNodeIds(), execDb.getExecNodeIds(),
        isNull(nodeSelector) ? null : nodeSelector.getStrategy());
    assertTrue(isNotEmpty(selectNodes), message(NO_AVAILABLE_NODES));

    List<Long> selectNodeIds = selectNodes.stream().map(NodeInfo::getId)
        .collect(Collectors.toList());
    Set<Long> liveNodeIds = nodeInfoQuery.getLiveNodeIds(selectNodeIds);
    for (NodeInfo selectNode : selectNodes) {
      BizAssert.assertTrue(liveNodeIds.contains(selectNode.getId()),
          message(NODE_AGENT_UNAVAILABLE_T, new Object[]{selectNode.getId()}));
    }
    return selectNodeIds;
  }

  private String shardingScript(Exec execDb, int nodeSize, boolean isFirst) {
    // The first node has more concurrency
    Integer shardingThread = !isFirst ? execDb.getThread() / nodeSize
        : (execDb.getThread() / nodeSize) + (execDb.getThread() % nodeSize);
    // The first node has more iterations
    Long shardingIterations = isNull(execDb.getIterations()) ? null : !isFirst
        ? execDb.getIterations() / nodeSize
        : (execDb.getIterations() / nodeSize) + (execDb.getIterations() % nodeSize);

    Integer shardingRampUpThread = null, shardingRampDownThread = null;
    if (nonNull(execDb.getOrgThread())) {
      Integer rampUpThread = execDb.getOrgThread().getRampUpThreads();
      shardingRampUpThread = execDb.getOrgThread().needRampUp() ?
          !isFirst ? rampUpThread / nodeSize
              : (rampUpThread / nodeSize) + (rampUpThread % nodeSize) : null;
      Integer rampDownThread = execDb.getOrgThread().getRampDownThreads();
      shardingRampDownThread = execDb.getOrgThread().needRampDown() ?
          !isFirst ? rampDownThread / nodeSize
              : (rampDownThread / nodeSize) + (rampDownThread % nodeSize) : null;
    }
    return overwriteConfigScript(execDb, shardingThread, shardingRampUpThread,
        shardingRampDownThread, shardingIterations);
  }

  @Override
  protected BaseRepository<Exec, Long> getRepository() {
    return this.execRepo;
  }
}
