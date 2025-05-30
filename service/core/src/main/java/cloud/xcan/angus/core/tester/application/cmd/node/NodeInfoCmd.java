package cloud.xcan.angus.core.tester.application.cmd.node;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.pojo.auth.AgentAuth;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import java.util.Collection;
import java.util.List;

public interface NodeInfoCmd {

  String NODE_INFO_ENDPOINT_PREFIX = "/api/v1/node/info";
  String DETAIL_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/{id}";
  String LIST_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX;
  String EXEC_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/{id}/exec";
  String RUNNER_PROCESS_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/runner/process";
  String KILL_RUNNER_PROCESS_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/runner/process/kill";

  String AGENT_INSTALL_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/{id}/agent/install/cmd";
  String AGENT_STATUS_CHECK_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/agent/status";
  String AGENT_PORT_CHECK_ENDPOINT = NODE_INFO_ENDPOINT_PREFIX + "/agent/port/check";

  String NODE_INFO_DOOR_ENDPOINT_PREFIX = "/innerapi/v1/node/info";
  String NODE_ONLINE_ENDPOINT_PREFIX = NODE_INFO_DOOR_ENDPOINT_PREFIX + "/agent/online";
  String AGENT_INSTALL_DOOR_ENDPOINT = NODE_INFO_DOOR_ENDPOINT_PREFIX + "/{id}/agent/install/cmd";
  String NODE_INFO_DELETE_DOOR_ENDPOINT = NODE_INFO_DOOR_ENDPOINT_PREFIX;

  void update0(NodeInfo nodeInfo);

  void delete(Collection<Long> ids);

  AgentInstallCmd agentInstallCmd(Long nodeId);

  void clearRunner(List<NodeInfo> nodeInfos);

  Boolean runnerProcessKill(NodeRunnerKillDto dto);

  void configureAgentAuth() throws Exception;

  NodeInfo initAgentNodeInfo(Long tenantId, Long nodeId);

  void saveAgentAuthInfo(Long tenantId, Long nodeId, NodeInfo nodeInfo);
}
