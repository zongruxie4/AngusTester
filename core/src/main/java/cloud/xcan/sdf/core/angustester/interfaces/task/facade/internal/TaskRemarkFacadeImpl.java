package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskRemarkCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskRemarkQuery;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskRemarkFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskRemarkAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class TaskRemarkFacadeImpl implements TaskRemarkFacade {

  @Resource
  private TaskRemarkCmd taskRemarkCmd;

  @Resource
  private TaskRemarkQuery taskRemarkQuery;

  @Override
  public IdKey<Long, Object> add(TaskRemarkAddDto dto) {
    return taskRemarkCmd.add(TaskRemarkAssembler.toTaskRemark(dto));
  }

  @Override
  public void delete(Long id) {
    taskRemarkCmd.delete(id);
  }

  @NameJoin
  @Override
  public PageResult<TaskRemarkVo> list(TaskRemarkFindDto dto) {
    Page<TaskRemark> taskRemarkPage = taskRemarkQuery
        .list(TaskRemarkAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(taskRemarkPage, TaskRemarkAssembler::toTaskRemarkVo);
  }
}
