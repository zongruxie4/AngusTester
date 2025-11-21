package cloud.xcan.angus.core.tester.application.cmd.config.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.config.NodeRoleCmd;
import cloud.xcan.angus.core.tester.domain.config.node.role.NodeRole;
import cloud.xcan.angus.core.tester.domain.config.node.role.NodeRoleRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command implementation for node role management.
 * <p>
 * Provides methods for adding and replacing node roles.
 * <p>
 * Ensures batch operations and repository access.
 */
@Biz
public class NodeRoleCmdImpl extends CommCmd<NodeRole, Long> implements NodeRoleCmd {

  @Resource
  private NodeRoleRepo nodeRoleRepo;

  /**
   * Add a batch of node roles.
   * <p>
   * Batch inserts node roles for the specified nodes.
   */
  @Override
  public void add0(List<NodeRole> roles) {
    if (isNotEmpty(roles)) {
      batchInsert(roles);
    }
  }

  /**
   * Replace node roles for a batch of nodes.
   * <p>
   * Deletes existing roles and batch inserts new roles for the specified nodes.
   */
  @Override
  public void replace0(List<NodeRole> roles) {
    if (isNotEmpty(roles)) {
      Collection<Long> nodeIds = roles.stream().map(NodeRole::getNodeId)
          .collect(Collectors.toSet());
      nodeRoleRepo.deleteByNodeIdIn(nodeIds);
      batchInsert(roles);
    }
  }

  /**
   * Get the repository for node roles.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<NodeRole, Long> getRepository() {
    return nodeRoleRepo;
  }
}
