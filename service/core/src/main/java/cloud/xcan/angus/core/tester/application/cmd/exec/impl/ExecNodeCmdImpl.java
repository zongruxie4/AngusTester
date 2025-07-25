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
 * Command implementation for managing execution nodes.
 * <p>
 * Provides methods for adding and retrieving execution nodes.
 */
@Slf4j
@Biz
public class ExecNodeCmdImpl extends CommCmd<ExecNode, Long> implements ExecNodeCmd {

  @Resource
  private ExecNodeRepo execNodeRepo;

  /**
   * Add a batch of execution nodes.
   * <p>
   * Inserts nodes if the list is not empty.
   */
  @Override
  public void add0(List<ExecNode> nodes) {
    if (isNotEmpty(nodes)) {
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
