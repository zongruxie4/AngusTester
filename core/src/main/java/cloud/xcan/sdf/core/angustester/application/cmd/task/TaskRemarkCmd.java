package cloud.xcan.sdf.core.angustester.application.cmd.task;

import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.spec.experimental.IdKey;

/**
 * @author xiaolong.liu
 */
public interface TaskRemarkCmd {

  IdKey<Long, Object> add(TaskRemark taskRemark);

  void delete(Long id);
}
