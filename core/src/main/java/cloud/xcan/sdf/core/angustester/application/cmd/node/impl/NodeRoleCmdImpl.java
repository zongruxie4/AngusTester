package cloud.xcan.sdf.core.angustester.application.cmd.node.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.angustester.application.cmd.node.NodeRoleCmd;
import cloud.xcan.sdf.core.angustester.domain.node.role.NodeRole;
import cloud.xcan.sdf.core.angustester.domain.node.role.NodeRoleRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

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
