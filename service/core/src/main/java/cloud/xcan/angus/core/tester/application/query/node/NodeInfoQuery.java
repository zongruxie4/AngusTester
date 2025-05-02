package cloud.xcan.angus.core.tester.application.query.node;

import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.runner.RunnerKillDto;
import cloud.xcan.angus.agent.message.runner.RunnerQueryVo;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerQueryDto;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.model.script.configuration.NodeSelectorStrategy;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface NodeInfoQuery {

  NodeInfo detail(Long id, Boolean isFreeNode);

  Page<NodeInfo> list(GenericSpecification<NodeInfo> spec, PageRequest pageable);

  Map<Long, SimpleCommandResult> agentStatus(boolean broadcast, List<Long> nodeIds);

  List<CheckPortVo> checkPort(NodeAgentCheckPortDto dto);

  RunnerQueryVo runnerProcess(NodeRunnerQueryDto dto);

  boolean hasOwnNodes();

  Set<Long> findAgentOnlineNode(Set<Long> ids);

  Set<Long> getLiveNodeIds(Collection<Long> nodeIds);

  Set<Long> getLiveNodeIds(Collection<Long> nodeIds, long latestLiveNodeInterval);

  Map<Long, Node> getNodeMap(Set<Long> nodeIds, boolean trial);

  List<Node> getValidExecNodeVoMap(Set<Long> nodeIds, int size, Long tenantId);

  Map<String, List<Node>> getValidCtrlIpNodeVoMap();

  List<NodeInfo> selectWithFree(Long execId, Integer num, Set<Long> availableNodeIds);

  List<NodeInfo> selectByStrategy(Long execId, Integer num, Set<Long> availableNodeIds,
      Set<Long> lastExecNodeIds, NodeSelectorStrategy strategy);

  List<Node> getNodes(Set<Long> nodeIds, NodeRole role, Boolean enabled,
      int size, Long tenantId);

  NodeInfo checkAndFind(Long id);

  List<NodeInfo> checkAndFind(Collection<Long> ids);

  void checkNodeExists(Collection<Long> nodeIds);

  ChannelRouter getLocalChannelRouter(Long nodeId, Long tenantId);

  ReplyMessage pushAgentMessage(String customType, Object content, ChannelRouter router)
      throws Exception;

  SimpleCommandResult pushKillRunnerProcessCmd2Agent(RunnerKillDto dto, ChannelRouter router)
      throws Exception;

  Boolean broadcastKillRunnerRemoteCtrl(NodeRunnerKillDto dto, String remoteUrl);
}
