package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskRemarkAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskRemarkAssembler.toTaskRemark;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskRemarkCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.domain.task.remark.TaskRemark;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskRemarkFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskRemarkAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskRemarkFacadeImpl implements TaskRemarkFacade {

  @Resource
  private TaskRemarkCmd taskRemarkCmd;

  @Resource
  private TaskRemarkQuery taskRemarkQuery;

  @Override
  public IdKey<Long, Object> add(TaskRemarkAddDto dto) {
    return taskRemarkCmd.add(toTaskRemark(dto));
  }

  @Override
  public void delete(Long id) {
    taskRemarkCmd.delete(id);
  }

  @NameJoin
  @Override
  public PageResult<TaskRemarkVo> list(TaskRemarkFindDto dto) {
    Page<TaskRemark> page = taskRemarkQuery.list(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TaskRemarkAssembler::toTaskRemarkVo);
  }
}
