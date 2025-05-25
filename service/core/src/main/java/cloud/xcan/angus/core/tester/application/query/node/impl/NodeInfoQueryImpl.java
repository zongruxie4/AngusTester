package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.BROADCAST_CTRL_CONNECTION_TIMEOUT;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.BROADCAST_CTRL_REQUEST_TIMEOUT;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.LATEST_LIVE_NODE_INTERVAL;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.QUERY_MAX_EXEC_NODES;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.QUERY_MAX_FREE_EXEC_NODES;
import static cloud.xcan.angus.api.enums.NodeRole.CONTROLLER;
import static cloud.xcan.angus.api.enums.NodeRole.EXECUTION;
import static cloud.xcan.angus.core.biz.BizAssert.assertTrue;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValueAndRemove;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_PORT_CHECK_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.AGENT_STATUS_CHECK_ENDPOINT;
import static cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd.RUNNER_PROCESS_ENDPOINT;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_CHECK_PORT_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.AGENT_PUSH_CHECK_STATUS_FAILED;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_CONTROLLER_INSTANCE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_LAST_NODES_IS_INSUFFICIENT;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NODES_LESS_AVAILABLE_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NOT_MEET_CONDITIONS_NODES;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NO_FREE_NODES;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NO_FREE_NODES_RETRY_LATER;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_NO_FREE_NODES_RETRY_LATER_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.EXEC_VALID_NODES_IS_INSUFFICIENT_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NODE_AGENT_UNAVAILABLE_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NODE_SPEC_INFO_MISSING_T;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NO_AVAILABLE_EXEC_ROLE_NODES;
import static cloud.xcan.angus.core.tester.domain.CtrlCoreMessage.NO_AVAILABLE_NODES;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import cloud.xcan.angus.agent.AgentCommandType;
import cloud.xcan.angus.agent.message.CheckPortCmdParam;
import cloud.xcan.angus.agent.message.CheckPortDto;
import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.CheckStatusDto;
import cloud.xcan.angus.agent.message.runner.RunnerKillDto;
import cloud.xcan.angus.agent.message.runner.RunnerQueryVo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import cloud.xcan.angus.core.tester.domain.exec.node.NodeExecNum;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfoRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsageRepo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentStatusQueryDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerQueryDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.model.script.configuration.NodeSelectorStrategy;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remoting.common.ClientRole;
import cloud.xcan.angus.remoting.common.MessageService;
import cloud.xcan.angus.remoting.common.message.BusinessMessage;
import cloud.xcan.angus.remoting.common.message.MessageBusinessType;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.remoting.server.RemotingServer;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Request.Builder;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.unit.DataSize;
import cloud.xcan.angus.spec.unit.RateValue;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Slf4j
@Biz
public class NodeInfoQueryImpl implements NodeInfoQuery {

  @Resource
  private NodeInfoRepo nodeInfoRepo;

  @Resource
  private ExecNodeRepo execNodeRepo;

  @Resource
  private NodeUsageRepo nodeUsageRepo;

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private RemotingServer remotingServer;

  @Resource
  private DiscoveryClient discoveryClient;

  @Resource
  private ApplicationInfo appInfo;

  @Resource
  private NodeQuery nodeQuery;

  @Override
  public NodeInfo detail(Long id, Boolean isFreeNode) {
    return new BizTemplate<NodeInfo>(false) {

      @Override
      protected NodeInfo process() {
        NodeInfo nodeInfo = nodeInfoRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "ExecTestResultSummary"));
        if (nonNull(isFreeNode) && Boolean.parseBoolean(isFreeNode.toString())) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }
        Set<Long> liveNodeIds = getLiveNodeIds(List.of(id));
        nodeInfo.setAgentOnline(isNotEmpty(liveNodeIds));
        return nodeInfo;
      }
    }.execute();
  }

  @Override
  public Page<NodeInfo> list(GenericSpecification<NodeInfo> spec, PageRequest pageable) {
    return new BizTemplate<Page<NodeInfo>>(false) {

      @Override
      protected Page<NodeInfo> process() {
        Object isFreeNode = findFirstValueAndRemove(spec.getCriteria(), "isFreeNode");
        if (nonNull(isFreeNode) && Boolean.parseBoolean(isFreeNode.toString())) {
          PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
        }
        Page<NodeInfo> page = nodeInfoRepo.findAll(spec, pageable);
        if (page.isEmpty()) {
          return page;
        }
        List<Long> nodeIds = page.getContent().stream().map(NodeInfo::getId)
            .collect(Collectors.toList());
        Set<Long> liveNodeIds = getLiveNodeIds(nodeIds);
        for (NodeInfo nodeInfo : page.getContent()) {
          nodeInfo.setAgentOnline(liveNodeIds.contains(nodeInfo.getId()));
        }
        return page;
      }
    }.execute();
  }

  @Override
  public Map<Long, SimpleCommandResult> agentStatus(boolean broadcast, List<Long> nodeIds) {
    return new BizTemplate<Map<Long, SimpleCommandResult>>(false) {
      @Override
      protected void checkParams() {
        checkNodeExists(nodeIds);
      }

      @Override
      protected Map<Long, SimpleCommandResult> process() {
        Map<Long, SimpleCommandResult> results = new HashMap<>();
        log.info("Controller handle to check agent status request, nodeIds: {}", nodeIds);
        List<Long> remoteNodeIds = new ArrayList<>();
        boolean isLocalRouter = false;
        for (Long nodeId : nodeIds) {
          // Push local controller
          ChannelRouter agentRouter = getLocalChannelRouter(nodeId, getOptTenantId());
          if (nonNull(agentRouter)) {
            isLocalRouter = true;
            try {
              results.put(nodeId, pushCheckStatusCmd2Agent(nodeId, agentRouter));
            } catch (Exception e) {
              log.error("Controller [isLocalRouter={}] check agent status exception: {}",
                  isLocalRouter, e.getMessage());
              results.put(nodeId, SimpleCommandResult.fail(getRootCauseMessage(e)));
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
          if (broadcast) {
            List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
            if (isNotEmpty(instances)) {
              for (ServiceInstance instance : instances) {
                // Exclude current controller
                if (!instance.getInstanceId().equals(appInfo.getInstanceId())) {
                  String remoteStartUrl =
                      "http://" + instance.getInstanceId() + AGENT_STATUS_CHECK_ENDPOINT;
                  NodeAgentStatusQueryDto remoteDto = new NodeAgentStatusQueryDto()
                      .setBroadcast(false) // Only broadcast once on the first controller
                      .setNodeIds(remoteNodeIds);
                  results.putAll(broadcastCheckStatus2RemoteCtrl(remoteDto, remoteStartUrl));
                }
              }
            } else {
              log.error("Controller instance not found for send remote, remote nodes:{} ",
                  remoteNodeIds);
              for (Long id : remoteNodeIds) {
                results
                    .put(id, SimpleCommandResult.fail(message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND)));
              }
            }
          } else {
            log.error("Controller ignore remote routes, ignores nodes: {}", remoteNodeIds);
            for (Long id : remoteNodeIds) {
              results.put(id,
                  SimpleCommandResult.fail(message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE)));
            }
          }
        }
        return results;
      }
    }.execute();
  }

  @Override
  public List<CheckPortVo> checkPort(NodeAgentCheckPortDto dto) {
    return new BizTemplate<List<CheckPortVo>>(false) {

      @Override
      protected List<CheckPortVo> process() {
        List<CheckPortVo> results = new ArrayList<>();
        log.info("Controller handle mock service to check port request, command: {}", dto);
        Map<Long, List<CheckPortCmdParam>> nodeCmdMap = dto.getCmdParams().stream()
            .collect(groupingBy(CheckPortCmdParam::getDeviceId));
        List<CheckPortCmdParam> remoteCmd = new ArrayList<>();
        boolean isLocalRouter = false;
        for (Long nodeId : nodeCmdMap.keySet()) {
          // Push local controller
          ChannelRouter agentRouter = getLocalChannelRouter(nodeId, getOptTenantId());
          if (nonNull(agentRouter)) {
            isLocalRouter = true;
            List<CheckPortCmdParam> nodeCmds = dto.getCmdParams();
            try {
              results.addAll(pushCheckPortCmd2Agent(dto, nodeCmds, agentRouter));
            } catch (Exception e) {
              log.error("Controller [isLocalRouter={}] check port mock service exception: {}",
                  isLocalRouter, e.getMessage());
              for (CheckPortCmdParam param : nodeCmds) {
                results.add(CheckPortVo.fail(getRootCauseMessage(e), param.getServerPort()));
              }
            }
          }
          if (!isLocalRouter) {
            remoteCmd.addAll(nodeCmdMap.get(nodeId));
          }
          // Reset
          isLocalRouter = false;
        }

        // Push remote controller
        if (!remoteCmd.isEmpty()) {
          if (dto.isBroadcast()) {
            List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
            if (isNotEmpty(instances)) {
              for (ServiceInstance instance : instances) {
                // Exclude current controller
                if (!instance.getInstanceId().equals(appInfo.getInstanceId())) {
                  String remoteStartUrl =
                      "http://" + instance.getInstanceId() + AGENT_PORT_CHECK_ENDPOINT;
                  NodeAgentCheckPortDto remoteDto = new NodeAgentCheckPortDto()
                      .setBroadcast(false) // Only broadcast once on the first controller
                      .setCmdParams(remoteCmd);
                  results.addAll(broadcastCheckPort2RemoteCtrl(remoteDto, remoteStartUrl));
                }
              }
            } else {
              log.error("Controller instance not found for send remote, remote nodes:{} ",
                  remoteCmd);
              for (CheckPortCmdParam param : dto.getCmdParams()) {
                results.add(CheckPortVo.fail(message(EXEC_CONTROLLER_INSTANCE_NOT_FOUND),
                    param.getServerPort()));
              }
            }
          } else {
            log.error("Controller ignore remote routes, ignores nodes: {}", remoteCmd);
            for (CheckPortCmdParam param : dto.getCmdParams()) {
              results.add(CheckPortVo.fail(message(MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE),
                  param.getServerPort()));
            }
          }
        }
        return results;
      }
    }.execute();
  }

  @Override
  public RunnerQueryVo runnerProcess(NodeRunnerQueryDto dto) {
    return new BizTemplate<RunnerQueryVo>() {
      NodeInfo nodeInfoDb;

      @Override
      protected void checkParams() {
        if (nullSafe(dto.getFreeNode(), true)) {
          PrincipalContext.get().setMultiTenantCtrl(false);
        }
        nodeInfoDb = checkAndFind(dto.getNodeId());
      }

      @Override
      protected RunnerQueryVo process() {
        log.info("Controller handle to query runner process, nodeId: {}", dto.getNodeId());
        // Push local controller
        ChannelRouter agentRouter = getLocalChannelRouter(dto.getNodeId(),
            nodeInfoDb.getTenantId());
        if (nonNull(agentRouter)) {
          try {
            return pushQueryRunnerProcessCmd2Agent(agentRouter);
          } catch (Exception e) {
            log.error("Controller [isLocalRouter={}] query runner process exception: {}",
                true, e.getCause().getMessage());
          }
          return null;
        }

        // Push remote controller
        if (dto.isBroadcast()) {
          List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
          if (isNotEmpty(instances)) {
            for (ServiceInstance instance : instances) {
              // Exclude current controller
              if (!instance.getInstanceId().equals(appInfo.getInstanceId())) {
                String remoteStartUrl =
                    "http://" + instance.getInstanceId() + RUNNER_PROCESS_ENDPOINT;
                dto.setBroadcast(false);
                RunnerQueryVo queryVo = broadcastQueryRunnerRemoteCtrl(dto, remoteStartUrl);
                if (nonNull(queryVo)) {
                  return queryVo;
                }
              }
            }
          }
        }
        return null;
      }
    }.execute();
  }

  @Override
  public Set<Long> findAgentOnlineNode(Set<Long> ids) {
    return new BizTemplate<Set<Long>>() {

      @Override
      protected Set<Long> process() {
        return getLiveNodeIds(ids);
      }
    }.execute();
  }

  @Override
  public NodeInfo findTenantNode(Long tenantId, String ip) {
    return nodeInfoRepo.findByTenantIdAndIp(tenantId, ip);
  }

  @Override
  public List<Long> selectValidFreeNodeIds(int nodeNum, Set<Long> availableNodeIds) {
    boolean isMultiTenantCtrl = isMultiTenantCtrl();
    long realTenantId = getOptTenantId();
    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(false);
      PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
    }

    List<NodeInfo> selectNodes = selectWithFree0(nodeNum, availableNodeIds);
    ProtocolAssert.assertTrue(ObjectUtils.isNotEmpty(selectNodes), message(NO_AVAILABLE_NODES));

    List<Long> selectNodeIds = selectNodes.stream().map(NodeInfo::getId)
        .collect(Collectors.toList());
    Set<Long> liveNodeIds = getLiveNodeIds(selectNodeIds);
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

  @Override
  public List<NodeInfo> selectValidFreeNode(int nodeNum, Set<Long> availableNodeIds) {
    boolean isMultiTenantCtrl = isMultiTenantCtrl();
    long realTenantId = getOptTenantId();
    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(false);
      PrincipalContext.get().setOptTenantId(OWNER_TENANT_ID);
    }

    List<NodeInfo> selectNodes = selectWithFree0(1, availableNodeIds);
    ProtocolAssert.assertTrue(ObjectUtils.isNotEmpty(selectNodes), message(NO_AVAILABLE_NODES));

    List<Long> selectNodeIds = selectNodes.stream().map(NodeInfo::getId)
        .collect(Collectors.toList());
    Set<Long> liveNodeIds = getLiveNodeIds(selectNodeIds);
    for (NodeInfo selectNode : selectNodes) {
      BizAssert.assertTrue(liveNodeIds.contains(selectNode.getId()),
          message(NODE_AGENT_UNAVAILABLE_T, new Object[]{selectNode.getId()}));
    }

    if (isMultiTenantCtrl) {
      PrincipalContext.get().setMultiTenantCtrl(true);
      PrincipalContext.get().setOptTenantId(realTenantId);
    }
    return selectNodes;
  }

  /**
   * Note: Free experience execution does not support node selection strategy.
   */
  @Override
  public List<NodeInfo> selectWithFree0(Integer num, Set<Long> availableNodeIds) {
    int nodeNum = nullSafe(num, 1);
    List<Node> nodes = nodeQuery.getNodes(availableNodeIds, EXECUTION, true,
        QUERY_MAX_FREE_EXEC_NODES, OWNER_TENANT_ID /*Fix: getOptTenantId()*/);
    assertTrue(isNotEmpty(nodes), message(EXEC_NO_FREE_NODES));
    Set<Long> freeNodeIds = nodes.stream()
        .filter(x -> nonNull(x.getFree()) && x.getFree() && nonNull(x.getInstallAgent())
            && x.getInstallAgent())
        .map(Node::getId).collect(Collectors.toSet());
    assertTrue(isNotEmpty(freeNodeIds), message(EXEC_NO_FREE_NODES));
    List<Long> usedFreeNodeIds = execNodeRepo.findNodeIdByNodeIdIn(freeNodeIds);
    freeNodeIds.removeAll(usedFreeNodeIds);
    assertTrue(isNotEmpty(freeNodeIds), message(EXEC_NO_FREE_NODES_RETRY_LATER));
    if (isNotEmpty(availableNodeIds)) {
      freeNodeIds.retainAll(availableNodeIds);
    }
    assertTrue(isNotEmpty(freeNodeIds), message(EXEC_NO_FREE_NODES_RETRY_LATER));
    assertTrue(freeNodeIds.size() >= nodeNum,
        message(EXEC_NO_FREE_NODES_RETRY_LATER_T, new Object[]{freeNodeIds.size()}));
    List<Long> selectNodeIds = new ArrayList<>(freeNodeIds).subList(0, nodeNum);
    return nodeInfoRepo.findAllById(selectNodeIds);
  }

  /**
   * Note:
   * <p>
   * 1. PrincipalContext must be maintained when calling Job.
   * <p>
   * 2. Agent status must be checked.
   * <p>
   * 3. BizException will terminate and continue scheduling.
   */
  @Override
  public List<NodeInfo> selectByStrategy(Integer num, Set<Long> availableNodeIds,
      Set<Long> lastExecNodeIds, NodeSelectorStrategy strategy, boolean allowTrialNode) {
    Long tenantId = getOptTenantId();
    int nodeNum = nullSafe(num, 1);

    assertTrue(isEmpty(availableNodeIds) || nodeNum == availableNodeIds.size(),
        message(EXEC_NODES_LESS_AVAILABLE_T, new Object[]{nodeNum}));

    List<NodeInfo> nodes;
    boolean selectLastedNodes = nonNull(strategy) && strategy.getEnabled()
        && nonNull(strategy.getLastExecuted()) && strategy.getLastExecuted();
    if (selectLastedNodes && isNotEmpty(lastExecNodeIds)) {
      assertTrue(lastExecNodeIds.size() >= nodeNum, message(EXEC_LAST_NODES_IS_INSUFFICIENT));
      nodes = nodeInfoRepo.findAllById(lastExecNodeIds);
    } else {
      nodes = isNotEmpty(availableNodeIds) ? checkAndFind(availableNodeIds)
          : nodeInfoRepo.findAllByTenantId(tenantId);
      if (isEmpty(nodes) && allowTrialNode){
        nodes = selectValidFreeNode(nodeNum, availableNodeIds);
        assertTrue(isNotEmpty(nodes), message(NO_AVAILABLE_NODES));
        return nodes;
      }
    }

    assertTrue(isNotEmpty(nodes), message(NO_AVAILABLE_NODES));

    Set<Long> nodeIds = nodes.stream().map(NodeInfo::getId).collect(Collectors.toSet());
    List<Node> validNodeVos = getValidExecNodeVoMap(nodeIds, QUERY_MAX_EXEC_NODES,
        getOptTenantId());
    assertTrue(isNotEmpty(validNodeVos), message(NO_AVAILABLE_EXEC_ROLE_NODES));
    assertTrue(validNodeVos.size() >= nodeNum,
        message(EXEC_VALID_NODES_IS_INSUFFICIENT_T, new Object[]{validNodeVos.size()}));

    Map<Long, Node> validNodeVoMap = validNodeVos.stream()
        .collect(toMap(Node::getId, x -> x));

    List<NodeInfo> finalOptionalNodes = nodes.stream()
        .filter(x -> validNodeVoMap.containsKey(x.getId()) && nonNull(x.getInfo()))
        .sorted((s1, s2) -> Integer.compare(s2.getInfo().getCpuNum(), s1.getInfo().getCpuNum()))
        .collect(Collectors.toList());

    assertTrue(isNotEmpty(finalOptionalNodes), message(NODE_SPEC_INFO_MISSING_T),
        new Object[]{validNodeVos.get(0).getId()});

    Map<Long, Long> nodeExecTaskNums = execNodeRepo.countByNodeIdIn(nodeIds).stream()
        .collect(toMap(NodeExecNum::getNodeId, NodeExecNum::getExecNum));

    if (isNull(strategy) || isNull(strategy.getEnabled())
        || !strategy.getEnabled() || selectLastedNodes) { // No node selection strategy
      if (Objects.equals(nodeNum, finalOptionalNodes.size())) {
        return finalOptionalNodes;
      }

      // When policy selection is not enabled, the idle machine with the highest configuration is selected by default
      List<NodeInfo> idleNodes = finalOptionalNodes.stream()
          .filter(x -> !nodeExecTaskNums.containsKey(x.getId())).collect(Collectors.toList());
      return idleNodes.size() >= nodeNum ? idleNodes.subList(0, nodeNum)
          : finalOptionalNodes.subList(0, nodeNum);
    }

    // Select according to strategy
    if (nonNull(strategy.getMaxTaskNum())) {
      finalOptionalNodes = finalOptionalNodes.stream()
          .filter(x -> !nodeExecTaskNums.containsKey(x.getId())
              || strategy.getMaxTaskNum() <= nodeExecTaskNums.get(x.getId()))
          .collect(Collectors.toList());
    }

    if (nonNull(strategy.getSpecEnabled()) && strategy.getSpecEnabled()) {
      finalOptionalNodes = finalOptionalNodes.stream().filter(x -> nonNull(x.getInfo())
          && (isNull(strategy.getCpuSpec()) || x.getInfo().getCpuNum() >= strategy.getCpuSpec())
          && (isNull(strategy.getMemorySpec()) || x.getInfo().getMemTotal() >= DataSize
          .parse(strategy.getMemorySpec()).toBytes())
          && (isNull(strategy.getDiskSpec()) || x.getInfo().getFsTotal() >= DataSize
          .parse(strategy.getDiskSpec()).toBytes())
      ).collect(Collectors.toList());
    }

    if (nonNull(strategy.getIdleRateEnabled()) && strategy.getIdleRateEnabled()) {
      finalOptionalNodes = finalOptionalNodes.stream().filter(x -> {
        NodeUsage nodeUsage = nodeUsageRepo.findFirstByNodeIdOrderByTimestampDesc(x.getId());
        return nonNull(nodeUsage) && (
            (isNull(strategy.getCpuIdleRate()) || nodeUsage.getCpu().getIdle()
                <= RateValue.parse(strategy.getCpuIdleRate()).getValue())
                && (isNull(strategy.getMemoryIdleRate())
                || nodeUsage.getMemory().getActualFreePercent()
                <= RateValue.parse(strategy.getMemoryIdleRate()).getValue())
                && (isNull(strategy.getDiskIdleRate()) || nodeUsage.getFilesystem().getFreePercent()
                <= RateValue.parse(strategy.getDiskIdleRate()).getValue())
        );
      }).collect(Collectors.toList());
    }

    if (finalOptionalNodes.size() >= nodeNum) {
      return finalOptionalNodes.subList(0, nodeNum - 1);
    }
    throw ProtocolException.of(message(EXEC_NOT_MEET_CONDITIONS_NODES));
  }

  @Override
  public boolean hasOwnNodes() {
    return nodeInfoRepo.countByTenantId(getOptTenantId()) > 0;
  }

  @Override
  public Set<Long> getLiveNodeIds(Collection<Long> nodeIds) {
    return nodeUsageRepo.findLatestIdByTimestampBeforeAndNodeIdIn(
        System.currentTimeMillis() - LATEST_LIVE_NODE_INTERVAL, nodeIds);
  }

  @Override
  public Set<Long> getLiveNodeIds(Collection<Long> nodeIds, long latestLiveNodeInterval) {
    return nodeUsageRepo.findLatestIdByTimestampBeforeAndNodeIdIn(
        System.currentTimeMillis() - latestLiveNodeInterval, nodeIds);
  }

  @Override
  public Map<Long, Node> getNodeMap(Set<Long> nodeIds, boolean trial) {
    List<Node> nodes = nodeQuery.getNodes(nodeIds, null, null,
        QUERY_MAX_EXEC_NODES, trial ? OWNER_TENANT_ID : getOptTenantId());
    return isEmpty(nodes) ? null : nodes.stream().collect(toMap(Node::getId, x -> x));
  }

  @Override
  public List<Node> getValidExecNodeVoMap(Set<Long> nodeIds, int size, Long tenantId) {
    List<Node> nodes = nodeQuery.getNodes(nodeIds, EXECUTION, true, size, tenantId);
    if (isNotEmpty(nodes)) {
      Set<Long> onlineIds = getLiveNodeIds(nodes.stream().map(Node::getId)
          .collect(Collectors.toSet()));
      return isEmpty(nodes) ? null : nodes.stream()
          .filter(x -> onlineIds.contains(x.getId())).collect(Collectors.toList());
    }
    return null;
  }

  /**
   * The controller role nodes only belongs to the deployment tenant.
   */
  @Override
  public Map<String, List<Node>> getValidCtrlIpNodeVoMap() {
    List<Node> nodes = nodeQuery.getNodes(null, CONTROLLER, true, QUERY_MAX_EXEC_NODES, null);
    return isEmpty(nodes) ? null : nodes.stream().collect(groupingBy(Node::getIp));
  }

  @Override
  public NodeInfo checkAndFind(Long id) {
    return nodeInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "NodeInfo"));
  }

  @Override
  public List<NodeInfo> checkAndFind(Collection<Long> ids) {
    List<NodeInfo> nodes = nodeInfoRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(nodes), ids, "NodeInfo");
    if (ids.size() != nodes.size()) {
      for (NodeInfo node : nodes) {
        assertResourceNotFound(ids.contains(node.getId()), node.getId(), "NodeInfo");
      }
    }
    return nodes;
  }

  @Override
  public void checkNodeExists(Collection<Long> nodeIds) {
    List<Long> nodeIdsDb = nodeInfoRepo.findIdsByIdIn(nodeIds);
    if (isEmpty(nodeIdsDb) || nodeIdsDb.size() != nodeIds.size()) {
      nodeIds.removeAll(nodeIdsDb);
      throw ResourceNotFound.of(nodeIds.iterator().next(), "NodeInfo");
    }
  }

  @Override
  public ChannelRouter getLocalChannelRouter(Long nodeId, Long tenantId) {
    ChannelRouter router = remotingServer.getRouterManager()
        .lookup(String.valueOf(tenantId), String.valueOf(nodeId), MessageService.Agent,
            ClientRole.HANDLE_TASK);
    return nonNull(router) && nonNull(router.getChannel()) ? router : null;
  }

  @Override
  public ReplyMessage pushAgentMessage(String customType, Object content, ChannelRouter router)
      throws Exception {
    BusinessMessage message = BusinessMessage.newBuilder()
        .id(UUID.randomUUID().toString())
        .sendService(MessageService.Ctrl)
        .clientRole(ClientRole.HANDLE_TASK)
        .businessType(MessageBusinessType.HANDLE_TASK)
        .customType(customType)
        .deviceId(router.getLocation().getDeviceId())
        .tenantId(router.getLocation().getTenantId())
        .content(content)
        .build();
    return remotingServer.sendAndReceive(router.getChannel(), message);
  }

  private SimpleCommandResult pushCheckStatusCmd2Agent(Long nodeId, ChannelRouter router)
      throws Exception {
    CheckStatusDto statusDto = CheckStatusDto.of(nodeId);
    ReplyMessage result = pushAgentMessage(AgentCommandType.AGENT_STATUS_CHECK, statusDto, router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), SimpleCommandResult.class);
    } else {
      return SimpleCommandResult.fail(message(AGENT_PUSH_CHECK_STATUS_FAILED));
    }
  }

  private RunnerQueryVo pushQueryRunnerProcessCmd2Agent(ChannelRouter router) throws Exception {
    ReplyMessage result = pushAgentMessage(AgentCommandType.RUNNER_QUERY, null, router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), RunnerQueryVo.class);
    } else {
      throw SysException.of(result.getFailureMessage());
    }
  }

  @Override
  public SimpleCommandResult pushKillRunnerProcessCmd2Agent(RunnerKillDto dto, ChannelRouter router)
      throws Exception {
    ReplyMessage result = pushAgentMessage(AgentCommandType.RUNNER_KILL, dto, router);
    if (result.isSuccess()) {
      return JsonUtils.fromJson(result.getContent().toString(), SimpleCommandResult.class);
    } else {
      throw SysException.of(result.getFailureMessage());
    }
  }

  private Map<Long, SimpleCommandResult> broadcastCheckStatus2RemoteCtrl(
      NodeAgentStatusQueryDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      return objectMapper.readValue(response.body(),
          new TypeReference<ApiLocaleResult<Map<Long, SimpleCommandResult>>>() {
          }).orElseContentThrow();
    } catch (Throwable e) {
      log.error("Broadcast check port mock service to remote controller exception: {}",
          e.getMessage());
      String cause = getRootCauseMessage(e);
      return dto.getNodeIds().stream()
          .collect(toMap(x -> x, x -> SimpleCommandResult.fail(cause)));
    }
  }

  private List<CheckPortVo> pushCheckPortCmd2Agent(NodeAgentCheckPortDto dto,
      List<CheckPortCmdParam> nodeCmds, ChannelRouter router) throws Exception {
    CheckPortDto statusDto = CheckPortDto.of(dto.getCmdParams());
    ReplyMessage result = pushAgentMessage(AgentCommandType.AGENT_PORT_CHECK, statusDto, router);
    if (result.isSuccess()) {
      CheckPortVo vo = JsonUtils.fromJson(result.getContent().toString(), CheckPortVo.class);
      return vo.getResults();
    } else {
      return nodeCmds.stream().map(
              x -> CheckPortVo.fail(message(AGENT_PUSH_CHECK_PORT_FAILED), x.getServerPort()))
          .collect(Collectors.toList());
    }
  }

  private List<CheckPortVo> broadcastCheckPort2RemoteCtrl(NodeAgentCheckPortDto dto,
      String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      return objectMapper
          .readValue(response.body(), new TypeReference<ApiLocaleResult<List<CheckPortVo>>>() {
          }).orElseContentThrow();
    } catch (Throwable e) {
      log.error("Broadcast check port mock service to remote controller exception: {}",
          e.getMessage());
      String cause = getRootCauseMessage(e);
      return dto.getCmdParams().stream().map(
          x -> CheckPortVo.fail(cause, x.getServerPort())).collect(Collectors.toList());
    }
  }

  private RunnerQueryVo broadcastQueryRunnerRemoteCtrl(NodeRunnerQueryDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      return objectMapper
          .readValue(response.body(), new TypeReference<ApiLocaleResult<RunnerQueryVo>>() {
          }).orElseContentThrow();
    } catch (Throwable e) {
      log.error("Broadcast query runner process to remote controller exception: {}",
          e.getMessage());
    }
    return null;
  }

  @Override
  public Boolean broadcastKillRunnerRemoteCtrl(NodeRunnerKillDto dto, String remoteUrl) {
    try {
      Response response = doHttpPostRequest(dto, remoteUrl);
      return objectMapper
          .readValue(response.body(), new TypeReference<ApiLocaleResult<Boolean>>() {
          }).orElseContentThrow();
    } catch (Throwable e) {
      log.error("Broadcast kill runner process to remote controller exception: {}",
          e.getMessage());
    }
    return null;
  }

  public static Response doHttpPostRequest(Object dto, String remoteUrl) throws Throwable {
    HttpSender sender = new HttpUrlConnectionSender(
        Duration.ofMillis(BROADCAST_CTRL_CONNECTION_TIMEOUT),
        Duration.ofMillis(BROADCAST_CTRL_REQUEST_TIMEOUT));
    Builder builder = sender.post(remoteUrl);
    if (nonNull(dto)) {
      builder.withJsonContent(JsonUtils.toJson(dto));
    }
    if (isNotEmpty(getAuthorization())) { // Fix: Door api invoke
      builder.withAuthentication(BEARER, getAuthorization().substring((BEARER + " ").length()));
    }
    return builder.send();
  }

}
