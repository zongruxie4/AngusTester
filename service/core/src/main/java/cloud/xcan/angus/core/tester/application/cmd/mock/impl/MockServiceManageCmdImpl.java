package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import static cloud.xcan.angus.core.tester.application.query.node.impl.NodeInfoQueryImpl.doHttpPostRequest;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_NOT_RUNNING;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_START_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_STATUS_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_STOP_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_INSTANCE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_NODE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.MOCKSERV_INSTANCE_OR_AGENT_NOT_STARTED;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.AgentCommandType;
import cloud.xcan.angus.agent.message.mockservice.StartCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StartDto;
import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StatusCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StatusDto;
import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.agent.message.mockservice.StopCmdParam;
import cloud.xcan.angus.agent.message.mockservice.StopDto;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceManageCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceMetricsCmd;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisDeleteDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.mockservice.api.MockApisDeleteDto;
import cloud.xcan.angus.mockservice.api.MockApisSyncDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Command implementation for mock service instance management.
 * <p>
 * Provides methods for starting, stopping, querying status, synchronizing, and deleting mock service instances.
 * <p>
 * Ensures distributed coordination, error handling, and integration with agent nodes.
 */
@Slf4j
@Biz
public class MockServiceManageCmdImpl implements MockServiceManageCmd {

  @Resource
  private NodeInfoQuery nodeInfoQuery;
  @Resource
  private MockServiceMetricsCmd mockServiceMetricsCmd;
  @Resource
  private DiscoveryClient discoveryClient;
  @Resource
  private ApplicationInfo appInfo;
  @Resource
  private ObjectMapper objectMapper;

  /**
   * Start mock service instances on specified nodes.
   * <p>
   * Handles local and remote node coordination, error handling, and activity logging.
   */
  @Override
  public List<StartVo> start(MockServiceStartDto dto) {
    List<StartVo> results = new ArrayList<>();
    log.info("Controller handle start mock service request, command: {}", dto);
    Map<Long, List<StartCmdParam>> nodeCmdMap = dto.getCmdParams().stream()
        .collect(Collectors.groupingBy(StartCmdParam::getDeviceId));
    List<StartCmdParam> remoteCmd = new ArrayList<>();
    List<Long> remoteNodeIds = new ArrayList<>();

    boolean isLocalRouter = false;
    for (Long nodeId : nodeCmdMap.keySet()) {
      // Push local controller
      ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, getOptTenantId());
      if (nonNull(router)) {
        isLocalRouter = true;
        List<StartCmdParam> nodeCmds = nodeCmdMap.get(nodeId);
        try {
          results.addAll(pushStartCmd2Agent(nodeCmds, nodeId, router));
        } catch (Exception e) {
          String cause = ExceptionUtils.getMessage(e);
          log.error("Controller [isLocalRouter={}] start mock service exception: {}",
              isLocalRouter, cause);
          for (StartCmdParam param : nodeCmds) {
            results.add(StartVo.fail(param.getServiceId(), cause));
          }
        }
      }
      if (!isLocalRouter) {
        remoteCmd.addAll(nodeCmdMap.get(nodeId));
        remoteNodeIds.add(nodeId);
      }
      // Reset
      isLocalRouter = false;
    }

    // Push remote controller
    if (!remoteCmd.isEmpty()) {
      if (dto.isBroadcast()) {
        Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
        if (isEmpty(ctrlIpNodeMap)) {
          throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
        boolean hasRemoteRouter = false;
        if (isNotEmpty(instances)) {
          String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
          MockServiceStartDto remoteDto = new MockServiceStartDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setCmdParams(remoteCmd);
          for (ServiceInstance inst : instances) {
            String broadcastInstanceIp = inst.getHost();
            // Exclude current controller
            if (currentInstanceIp.equals(broadcastInstanceIp)
                // Exclude non exchange server controller
                || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
              continue;
            }
            String remoteStartUrl = "http://" + inst.getInstanceId() + START_ENDPOINT;
            results.addAll(broadcastStart2RemoteCtrl(remoteDto, remoteStartUrl));
            hasRemoteRouter = true;
          }
        }
        if (!hasRemoteRouter) {
          log.error("Controller instance not found or agent not started, remote nodes:{} ",
              remoteCmd);
          for (StartCmdParam param : dto.getCmdParams()) {
            if (remoteNodeIds.contains(param.getDeviceId())) {
              results.add(StartVo.fail(param.getServiceId(),
                  message(MOCKSERV_INSTANCE_OR_AGENT_NOT_STARTED)));
            }
          }
        }
      } else {
        log.error("Controller ignore remote routes, ignores nodes: {}", remoteCmd);
        for (StartCmdParam param : dto.getCmdParams()) {
          if (remoteNodeIds.contains(param.getDeviceId())) {
            results.add(StartVo.fail(param.getServiceId(),
                message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE)));
          }
        }
      }
    }
    return results;
  }

  /**
   * Stop mock service instances on specified nodes.
   * <p>
   * Handles local and remote node coordination, error handling, and activity logging.
   */
  @Override
  public List<StopVo> stop(MockServiceStopDto dto) {
    List<StopVo> results = new ArrayList<>();
    log.info("Controller handle to stop mock service request, command: {}", dto);

    deleteMockServiceMetrics(dto);

    Map<Long, List<StopCmdParam>> nodeCmdMap = dto.getCmdParams().stream()
        .collect(Collectors.groupingBy(StopCmdParam::getDeviceId));
    List<StopCmdParam> remoteCmd = new ArrayList<>();
    List<Long> remoteNodeIds = new ArrayList<>();
    boolean isLocalRouter = false;
    for (Long nodeId : nodeCmdMap.keySet()) {
      // Push local controller
      ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, getOptTenantId());
      if (nonNull(router)) {
        isLocalRouter = true;
        List<StopCmdParam> nodeCmds = nodeCmdMap.get(nodeId);
        try {
          results.addAll(pushStopCmd2Agent(nodeCmds, nodeId, router));
        } catch (Exception e) {
          String cause = ExceptionUtils.getMessage(e);
          log.error("Controller [isLocalRouter={}] stop mock service exception: {}",
              isLocalRouter, cause);
          for (StopCmdParam param : nodeCmds) {
            results.add(StopVo.fail(param.getServiceId(), cause));
          }
        }
      }
      if (!isLocalRouter) {
        remoteCmd.addAll(nodeCmdMap.get(nodeId));
        remoteNodeIds.add(nodeId);
      }
      // Reset
      isLocalRouter = false;
    }

    // Push remote controller
    if (!remoteCmd.isEmpty()) {
      if (dto.isBroadcast()) {
        Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
        if (isEmpty(ctrlIpNodeMap)) {
          throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
        boolean hasRemoteRouter = false;
        if (isNotEmpty(instances)) {
          String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
          MockServiceStopDto remoteDto = new MockServiceStopDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setCmdParams(remoteCmd);
          for (ServiceInstance inst : instances) {
            String broadcastInstanceIp = inst.getHost();
            // Exclude current controller
            if (currentInstanceIp.equals(broadcastInstanceIp)
                // Exclude non exchange server controller
                || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
              continue;
            }
            String remoteStartUrl = "http://" + inst.getInstanceId() + STOP_ENDPOINT;
            results.addAll(broadcastStop2RemoteCtrl(remoteDto, remoteStartUrl));
            hasRemoteRouter = true;
          }
        }
        if (!hasRemoteRouter) {
          log.error("Controller instance not found or agent not started, remote nodes:{} ",
              remoteCmd);
          for (StopCmdParam param : dto.getCmdParams()) {
            if (remoteNodeIds.contains(param.getDeviceId())) {
              results.add(StopVo.fail(param.getServiceId(),
                  message(MOCKSERV_INSTANCE_OR_AGENT_NOT_STARTED)));
            }
          }
        }
      } else {
        log.error("Controller ignore remote mock service routes, ignores nodes: {}", remoteCmd);
        for (StopCmdParam param : dto.getCmdParams()) {
          if (remoteNodeIds.contains(param.getDeviceId())) {
            results.add(StopVo.fail(param.getServiceId(),
                message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE)));
          }
        }
      }
    }
    return results;
  }

  /**
   * Query status of mock service instances on specified nodes.
   * <p>
   * Handles local and remote node coordination, error handling, and returns status results.
   */
  @Override
  public List<StatusVo> status(MockServiceStatusDto dto) {
    List<StatusVo> results = new ArrayList<>();
    log.info("Controller handle to status mock service request, command: {}", dto);
    Map<Long, List<StatusCmdParam>> nodeCmdMap = dto.getCmdParams().stream()
        .collect(Collectors.groupingBy(StatusCmdParam::getDeviceId));
    List<StatusCmdParam> remoteCmd = new ArrayList<>();
    List<Long> remoteNodeIds = new ArrayList<>();
    boolean isLocalRouter = false;
    for (Long nodeId : nodeCmdMap.keySet()) {
      // Push local controller
      ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(nodeId, getOptTenantId());
      if (nonNull(router)) {
        isLocalRouter = true;
        List<StatusCmdParam> nodeCmds = nodeCmdMap.get(nodeId);
        try {
          results.addAll(pushStatusCmd2Agent(nodeCmds, nodeId, router));
        } catch (Exception e) {
          String cause = ExceptionUtils.getMessage(e);
          log.error("Controller [isLocalRouter={}] status mock service exception: {}",
              isLocalRouter, cause);
          for (StatusCmdParam param : nodeCmds) {
            results.add(StatusVo.fail(param.getServiceId(), cause));
          }
        }
      }
      if (!isLocalRouter) {
        remoteCmd.addAll(nodeCmdMap.get(nodeId));
        remoteNodeIds.add(nodeId);
      }
      // Reset
      isLocalRouter = false;
    }

    // Push remote controller
    if (!remoteCmd.isEmpty()) {
      if (dto.isBroadcast()) {
        Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
        if (isEmpty(ctrlIpNodeMap)) {
          throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
        boolean hasRemoteRouter = false;
        if (isNotEmpty(instances)) {
          String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
          MockServiceStatusDto remoteDto = new MockServiceStatusDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setCmdParams(remoteCmd);
          for (ServiceInstance inst : instances) {
            String broadcastInstanceIp = inst.getHost();
            // Exclude current controller
            if (currentInstanceIp.equals(broadcastInstanceIp)
                // Exclude non exchange server controller
                || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
              continue;
            }
            String remoteStartUrl = "http://" + inst.getInstanceId() + STATUS_ENDPOINT;
            results.addAll(broadcastStatus2RemoteCtrl(remoteDto, remoteStartUrl));
            hasRemoteRouter = true;
          }
        }
        if (!hasRemoteRouter) {
          log.error("Controller instance not found or agent not started, remote nodes:{} ",
              remoteCmd);
          for (StatusCmdParam param : dto.getCmdParams()) {
            if (remoteNodeIds.contains(param.getDeviceId())) {
              results.add(StatusVo.fail(param.getServiceId(),
                  message(MOCKSERV_INSTANCE_OR_AGENT_NOT_STARTED)));
            }
          }
        }
      } else {
        log.error("Controller ignore mock service remote routes, ignores nodes: {}", remoteCmd);
        for (StatusCmdParam param : dto.getCmdParams()) {
          if (remoteNodeIds.contains(param.getDeviceId())) {
            results.add(StatusVo.fail(param.getServiceId(),
                message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE)));
          }
        }
      }
    }
    return results;
  }

  /**
   * Synchronize mock APIs to a single node or broadcast to all nodes.
   * <p>
   * Handles distributed coordination and error handling.
   */
  @Override
  public SimpleCommandResult syncApis(MockServiceApisSyncDto dto) {
    log.info("Controller handle to sync mock apis request, command: {}", dto);
    // Push local controller
    ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(
        dto.getCmdParams().getDeviceId(), getOptTenantId());
    SimpleCommandResult result;
    if (nonNull(router)) {
      try {
        result = pushSyncApisCmd2Agent(dto.getCmdParams(), dto.getCmdParams().getDeviceId(),
            router);
      } catch (Exception e) {
        String cause = ExceptionUtils.getMessage(e);
        log.error("Controller [isLocalRouter={}] sync mock apis exception: {}", true, cause);
        result = SimpleCommandResult.fail(String.format("Sync mock apis exception: %s", cause));
      }
      return result;
    }

    if (dto.isBroadcast()) {
      Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
      if (isEmpty(ctrlIpNodeMap)) {
        throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
      }

      MockApisSyncDto remoteCmd = dto.getCmdParams();
      List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
      if (isNotEmpty(instances)) {
        String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
        for (ServiceInstance inst : instances) {
          String broadcastInstanceIp = inst.getHost();
          // Exclude current controller
          if (currentInstanceIp.equals(broadcastInstanceIp)
              // Exclude non exchange server controller
              || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
            continue;
          }
          String remoteStartUrl = "http://" + inst.getInstanceId() + SYNC_APIS_ENDPOINT;
          MockServiceApisSyncDto remoteDto = new MockServiceApisSyncDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setCmdParams(remoteCmd);
          result = broadcastSyncApis2RemoteCtrl(remoteDto, remoteStartUrl);
          // Single node command -> Not null indicates successful processing
          if (nonNull(result)) {
            return result;
          }
        }
      }
      log.error("Controller instance not found for sync mock apis to remote, remote nodes:{} ",
          remoteCmd);
      return SimpleCommandResult.fail(message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND));
    }
    return SimpleCommandResult.fail(message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE));
  }

  /**
   * Delete mock APIs from a single node or broadcast to all nodes.
   * <p>
   * Handles distributed coordination and error handling.
   */
  @Override
  public SimpleCommandResult deleteApis(MockServiceApisDeleteDto dto) {
    log.info("Controller handle to delete mock apis request, command: {}", dto);
    // Push local controller
    ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(
        dto.getCmdParams().getDeviceId(), getOptTenantId());
    SimpleCommandResult result;
    if (nonNull(router)) {
      try {
        result = pushDeleteApisCmd2Agent(dto.getCmdParams(), dto.getCmdParams().getDeviceId(),
            router);
      } catch (Exception e) {
        String cause = ExceptionUtils.getMessage(e);
        log.error("Controller [isLocalRouter={}] delete mock apis exception: {}", true, cause);
        result = SimpleCommandResult.fail(String.format("Delete mock apis exception: %s", cause));
      }
      return result;
    }

    if (dto.isBroadcast()) {
      Map<String, List<Node>> ctrlIpNodeMap = nodeInfoQuery.getValidCtrlIpNodeVoMap();
      if (isEmpty(ctrlIpNodeMap)) {
        throw SysException.of(EXEC_CONTROLLER_NODE_NOT_FOUND);
      }
      MockApisDeleteDto remoteCmd = dto.getCmdParams();
      List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
      if (isNotEmpty(instances)) {
        String currentInstanceIp = appInfo.getInstanceId().split(":")[0];
        for (ServiceInstance inst : instances) {
          String broadcastInstanceIp = inst.getHost();
          // Exclude current controller
          if (currentInstanceIp.equals(broadcastInstanceIp)
              // Exclude non exchange server controller
              || !ctrlIpNodeMap.containsKey(broadcastInstanceIp)) {
            continue;
          }
          String remoteStartUrl = "http://" + inst.getInstanceId() + DELETE_APIS_ENDPOINT;
          MockServiceApisDeleteDto remoteDto = new MockServiceApisDeleteDto()
              .setBroadcast(false) // Only broadcast once on the first controller
              .setCmdParams(remoteCmd);
          result = broadcastDeleteApis2RemoteCtrl(remoteDto, remoteStartUrl);
          // Single node command -> Not null indicates successful processing
          if (nonNull(result)) {
            return result;
          }
        }
      }
      log.error("Controller instance not found for delete mock apis to remote, remote nodes:{} ",
          remoteCmd);
      return SimpleCommandResult.fail(message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND));
    }
    return SimpleCommandResult.fail(message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE));
  }

  /**
   * Delete mock service metrics for specified service IDs.
   * <p>
   * Removes all metrics data associated with the given service IDs.
   */
  private void deleteMockServiceMetrics(MockServiceStopDto dto) {
    Set<Long> serviceIds = dto.getCmdParams().stream().map(StopCmdParam::getServiceId)
        .collect(Collectors.toSet());
    if (isNotEmpty(serviceIds)) {
      mockServiceMetricsCmd.deleteByServiceIds(serviceIds);
    }
  }

  /**
   * Push start command to agent node.
   * <p>
   * Sends start command to the specified agent node and returns results.
   */
  private List<StartVo> pushStartCmd2Agent(List<StartCmdParam> nodeCmds, Long nodeId,
      ChannelRouter router) throws Exception {
    StartDto startDto = StartDto.of(nodeCmds);
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.MOCK_SERVICE_START,
        startDto, router);
    if (result.isSuccess()) {
      StartVo vo = JsonUtils.fromJson(result.getContent().toString(), StartVo.class);
      return vo.getResults();
    } else {
      return nodeCmds.stream().map(x -> StartVo.fail(x.getServiceId(),
          message(AGENT_PUSH_START_FAILED))).toList();
    }
  }

  /**
   * Broadcast start command to remote controllers.
   * <p>
   * Sends start command to all remote controllers and returns results.
   */
  private List<StartVo> broadcastStart2RemoteCtrl(MockServiceStartDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      List<StartVo> startVos = objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<List<StartVo>>>() {
          }).orElseContentThrow();
      if (isNotEmpty(startVos) && startVos.size() == dto.getCmdParams().size()) {
        return startVos;
      }
      if (isEmpty(startVos)) {
        return dto.getCmdParams().stream()
            .map(x -> StartVo.fail(x.getServiceId(), message(AGENT_NOT_RUNNING)))
            .toList();
      }
      Map<Long, StartCmdParam> serviceCmdMap = dto.getCmdParams().stream()
          .collect(Collectors.toMap(StartCmdParam::getServiceId, x -> x));
      List<StartVo> fullStartVos = new ArrayList<>(startVos);
      for (StartVo vo : startVos) {
        if (!serviceCmdMap.containsKey(vo.getServiceId())) {
          fullStartVos.add(StartVo.fail(vo.getServiceId(), message(AGENT_NOT_RUNNING)));
        }
      }
      return fullStartVos;
    } catch (Throwable e) {
      String cause = ExceptionUtils.getMessage(e);
      log.error("Broadcast start mock service to remote controller exception: {}", cause);
      return dto.getCmdParams().stream().map(x -> StartVo.fail(x.getServiceId(), cause))
          .toList();
    }
  }

  /**
   * Push stop command to agent node.
   * <p>
   * Sends stop command to the specified agent node and returns results.
   */
  private List<StopVo> pushStopCmd2Agent(List<StopCmdParam> nodeCmds, Long nodeId,
      ChannelRouter router) throws Exception {
    StopDto stopDto = StopDto.of(nodeCmds);
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.MOCK_SERVICE_STOP,
        stopDto, router);
    if (result.isSuccess()) {
      StopVo vo = JsonUtils.fromJson(result.getContent().toString(), StopVo.class);
      return vo.getResults();
    } else {
      return nodeCmds.stream()
          .map(x -> StopVo.fail(x.getServiceId(), message(AGENT_PUSH_STOP_FAILED)))
          .toList();
    }
  }

  /**
   * Broadcast stop command to remote controllers.
   * <p>
   * Sends stop command to all remote controllers and returns results.
   */
  private List<StopVo> broadcastStop2RemoteCtrl(MockServiceStopDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      List<StopVo> stopVos = objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<List<StopVo>>>() {
          }).orElseContentThrow();
      if (isNotEmpty(stopVos) && stopVos.size() == dto.getCmdParams().size()) {
        return stopVos;
      }
      if (isEmpty(stopVos)) {
        return dto.getCmdParams().stream()
            .map(x -> StopVo.fail(x.getServiceId(), message(AGENT_NOT_RUNNING)))
            .toList();
      }
      Map<Long, StopCmdParam> serviceCmdMap = dto.getCmdParams().stream()
          .collect(Collectors.toMap(StopCmdParam::getServiceId, x -> x));
      List<StopVo> fullStartVos = new ArrayList<>(stopVos);
      for (StopVo vo : stopVos) {
        if (!serviceCmdMap.containsKey(vo.getServiceId())) {
          fullStartVos.add(StopVo.fail(vo.getServiceId(), message(AGENT_NOT_RUNNING)));
        }
      }
      return fullStartVos;
    } catch (Throwable e) {
      String cause = ExceptionUtils.getMessage(e);
      log.error("Broadcast stop mock service to remote controller exception: {}", e.getMessage());
      return dto.getCmdParams().stream().map(x -> StopVo.fail(x.getServiceId(), cause))
          .toList();
    }
  }

  /**
   * Push status command to agent node.
   * <p>
   * Sends status command to the specified agent node and returns results.
   */
  private List<StatusVo> pushStatusCmd2Agent(List<StatusCmdParam> nodeCmds, Long nodeId,
      ChannelRouter router) throws Exception {
    StatusDto statusDto = StatusDto.of(nodeCmds);
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.MOCK_SERVICE_STATUS,
        statusDto, router);
    if (result.isSuccess()) {
      StatusVo vo = JsonUtils.fromJson(result.getContent().toString(), StatusVo.class);
      return vo.getResults();
    } else {
      return nodeCmds.stream().map(x -> StatusVo.fail(x.getServiceId(),
          message(AGENT_PUSH_STATUS_FAILED))).toList();
    }
  }

  /**
   * Broadcast status command to remote controllers.
   * <p>
   * Sends status command to all remote controllers and returns results.
   */
  private List<StatusVo> broadcastStatus2RemoteCtrl(MockServiceStatusDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      List<StatusVo> statusVos = objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<List<StatusVo>>>() {
          }).orElseContentThrow();
      if (isNotEmpty(statusVos) && statusVos.size() == dto.getCmdParams().size()) {
        return statusVos;
      }
      if (isEmpty(statusVos)) {
        return dto.getCmdParams().stream()
            .map(x -> StatusVo.fail(x.getServiceId(), message(AGENT_NOT_RUNNING)))
            .toList();
      }
      Map<Long, StatusCmdParam> serviceCmdMap = dto.getCmdParams().stream()
          .collect(Collectors.toMap(StatusCmdParam::getServiceId, x -> x));
      List<StatusVo> fullStartVos = new ArrayList<>(statusVos);
      for (StatusVo vo : statusVos) {
        if (!serviceCmdMap.containsKey(vo.getServiceId())) {
          fullStartVos.add(StatusVo.fail(vo.getServiceId(), message(AGENT_NOT_RUNNING)));
        }
      }
      return fullStartVos;
    } catch (Throwable e) {
      String cause = ExceptionUtils.getMessage(e);
      log.error("Broadcast status mock service to remote controller exception: {}", e.getMessage());
      return dto.getCmdParams().stream().map(x -> StatusVo.fail(x.getServiceId(), cause))
          .toList();
    }
  }

  /**
   * Push sync APIs command to agent node.
   * <p>
   * Sends sync APIs command to the specified agent node and returns result.
   */
  private SimpleCommandResult pushSyncApisCmd2Agent(MockApisSyncDto nodeCmds, Long nodeId,
      ChannelRouter router) throws Exception {
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.MOCK_SERVICE_APIS_SYNC,
        nodeCmds, router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), SimpleCommandResult.class);
    } else {
      return SimpleCommandResult.fail(message(AGENT_PUSH_STATUS_FAILED));
    }
  }

  /**
   * Broadcast sync APIs command to remote controllers.
   * <p>
   * Sends sync APIs command to all remote controllers and returns result.
   */
  private SimpleCommandResult broadcastSyncApis2RemoteCtrl(MockServiceApisSyncDto dto,
      String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      SimpleCommandResult vo = objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<SimpleCommandResult>>() {
          }).orElseContentThrow();
      return nonNull(vo) ? vo : SimpleCommandResult.fail(message(AGENT_NOT_RUNNING));
    } catch (Throwable e) {
      String cause = ExceptionUtils.getMessage(e);
      log.error("Broadcast sync mock apis to remote controller exception: {}", e.getMessage());
      return SimpleCommandResult.fail(cause);
    }
  }

  /**
   * Push delete APIs command to agent node.
   * <p>
   * Sends delete APIs command to the specified agent node and returns result.
   */
  private SimpleCommandResult pushDeleteApisCmd2Agent(MockApisDeleteDto nodeCmds, Long nodeId,
      ChannelRouter router) throws Exception {
    ReplyMessage result = nodeInfoQuery.pushAgentMessage(AgentCommandType.MOCK_SERVICE_APIS_DELETE,
        nodeCmds, router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), SimpleCommandResult.class);
    } else {
      return SimpleCommandResult.fail(message(AGENT_PUSH_STATUS_FAILED));
    }
  }

  /**
   * Broadcast delete APIs command to remote controllers.
   * <p>
   * Sends delete APIs command to all remote controllers and returns result.
   */
  private SimpleCommandResult broadcastDeleteApis2RemoteCtrl(MockServiceApisDeleteDto dto,
      String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      SimpleCommandResult vo = objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<SimpleCommandResult>>() {
          }).orElseContentThrow();
      return nonNull(vo) ? vo : SimpleCommandResult.fail(message(AGENT_NOT_RUNNING));
    } catch (Throwable e) {
      String cause = ExceptionUtils.getMessage(e);
      log.error("Broadcast delete mock apis to remote controller exception: {}", e.getMessage());
      return SimpleCommandResult.fail(cause);
    }
  }
}
