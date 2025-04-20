package cloud.xcan.angus.core.tester.application.cmd.node.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeRoleCmd;
import cloud.xcan.angus.core.tester.domain.node.role.NodeRole;
import cloud.xcan.angus.core.tester.domain.node.role.NodeRoleRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Biz
public class NodeRoleCmdImpl extends CommCmd<NodeRole, Long> implements NodeRoleCmd {

  @Resource
  private NodeRoleRepo nodeRoleRepo;

  @Override
  public void add0(List<NodeRole> roles) {
    if (isNotEmpty(roles)) {
      batchInsert(roles);
    }
  }

  @Override
  public void replace0(List<NodeRole> roles) {
    if (isNotEmpty(roles)) {
      Collection<Long> nodeIds = roles.stream().map(NodeRole::getNodeId)
          .collect(Collectors.toSet());
      nodeRoleRepo.deleteByNodeIdIn(nodeIds);
      batchInsert(roles);
    }
  }

  @Override
  protected BaseRepository<NodeRole, Long> getRepository() {
    return nodeRoleRepo;
  }
}
