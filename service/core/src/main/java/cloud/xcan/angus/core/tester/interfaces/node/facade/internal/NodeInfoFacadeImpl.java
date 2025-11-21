package cloud.xcan.angus.core.tester.interfaces.node.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeInfoAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeInfoAssembler.toNodeInfoDetailVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.agent.message.CheckPortVo;
import cloud.xcan.angus.agent.message.runner.RunnerQueryVo;
import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.interfaces.node.facade.NodeInfoFacade;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentCheckPortDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeAgentStatusQueryDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeInfoFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerQueryDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler.NodeInfoAssembler;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.NodeExecVo;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NodeInfoFacadeImpl implements NodeInfoFacade {

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Resource
  private NodeInfoCmd nodeInfoCmd;

  @Resource
  private ExecQuery execQuery;

  @Override
  public void delete(HashSet<Long> ids) {
    nodeInfoCmd.delete(ids);
  }

  @Override
  public NodeInfoDetailVo detail(Long id, Boolean isFreeNode) {
    return toNodeInfoDetailVo(nodeInfoQuery.detail(id, isFreeNode));
  }

  @Override
  public PageResult<NodeInfoDetailVo> list(NodeInfoFindDto dto) {
    Page<NodeInfo> page = nodeInfoQuery.list(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, NodeInfoAssembler::toNodeInfoDetailVo);
  }

  @NameJoin
  @Override
  public List<NodeExecVo> exec(Long id) {
    List<ExecInfo> execInfos = execQuery.findByNodeId(id);
    return isEmpty(execInfos) ? null
        : execInfos.stream().map(NodeInfoAssembler::toNodeExecInfo).toList();
  }

  @Override
  public Set<Long> agentOnlineNode(Set<Long> ids) {
    return nodeInfoQuery.findAgentOnlineNode(ids);
  }

  @Override
  public AgentInstallCmd agentInstallCmd(Long id) {
    return nodeInfoCmd.agentInstallCmd(id);
  }

  @Override
  public Map<Long, SimpleCommandResult> agentStatus(NodeAgentStatusQueryDto dto) {
    return nodeInfoQuery.agentStatus(dto.getBroadcast(), dto.getNodeIds());
  }

  @Override
  public List<CheckPortVo> checkPort(NodeAgentCheckPortDto dto) {
    return nodeInfoQuery.checkPort(dto);
  }

  @Override
  public RunnerQueryVo runnerProcess(NodeRunnerQueryDto dto) {
    return nodeInfoQuery.runnerProcess(dto);
  }

  @Override
  public Boolean runnerProcessKill(NodeRunnerKillDto dto) {
    return nodeInfoCmd.runnerProcessKill(dto);
  }
}
