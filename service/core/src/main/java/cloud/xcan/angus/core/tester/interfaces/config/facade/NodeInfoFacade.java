package cloud.xcan.angus.core.tester.interfaces.config.facade;

import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.runner.RunnerQueryVo;
import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeAgentStatusQueryDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeRunnerQueryDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeExecVo;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.remote.PageResult;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NodeInfoFacade {

  void delete(HashSet<Long> ids);

  NodeInfoDetailVo detail(Long id, Boolean isFreeNode);

  PageResult<NodeInfoDetailVo> list(NodeInfoFindDto dto);

  List<NodeExecVo> exec(Long id);

  Set<Long> agentOnlineNode(Set<Long> ids);

  AgentInstallCmd agentInstallCmd(Long id);

  Map<Long, SimpleCommandResult> agentStatus(NodeAgentStatusQueryDto dto);

  List<CheckPortVo> checkPort(NodeAgentCheckPortDto dto);

  RunnerQueryVo runnerProcess(NodeRunnerQueryDto dto);

  Boolean runnerProcessKill(NodeRunnerKillDto dto);
}
