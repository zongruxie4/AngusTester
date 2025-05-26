package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.core.tester.domain.task.remark.TaskRemark;
import cloud.xcan.angus.spec.experimental.IdKey;

/**
 * @author XiaoLong Liu
 */
public interface TaskRemarkCmd {

  IdKey<Long, Object> add(TaskRemark taskRemark);

  void delete(Long id);
}
