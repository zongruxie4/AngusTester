package cloud.xcan.angus.core.tester.application.cmd.exec.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecNodeCmd;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNode;
import cloud.xcan.angus.core.tester.domain.exec.node.ExecNodeRepo;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Command implementation for managing execution nodes.
 * </p>
 * <p>
 * Provides methods for adding and retrieving execution nodes. Handles batch operations
 * for execution node management and provides repository access for execution node entities.
 * </p>
 */
@Slf4j
@Biz
public class ExecNodeCmdImpl extends CommCmd<ExecNode, Long> implements ExecNodeCmd {

  @Resource
  private ExecNodeRepo execNodeRepo;

  /**
   * <p>
   * Add a batch of execution nodes.
   * </p>
   * <p>
   * Inserts nodes if the list is not empty. Performs batch insertion for efficiency
   * when adding multiple execution nodes.
   * </p>
   * @param nodes List of execution nodes to add
   */
  @Override
  public void add0(List<ExecNode> nodes) {
    if (isNotEmpty(nodes)) {
      // Perform batch insertion for efficiency
      batchInsert0(nodes);
    }
  }

  /**
   * Get the repository for ExecNode entity.
   * <p>
   * @return the ExecNodeRepo instance
   */
  @Override
  protected BaseRepository<ExecNode, Long> getRepository() {
    return execNodeRepo;
  }
}
