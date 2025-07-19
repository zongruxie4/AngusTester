package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskFollowAssembler.judgeMatchFilter;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.task.TaskFollowCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskFollowQuery;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskFollowAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskFollowFacadeImpl implements TaskFollowFacade {

  @Resource
  private TaskFollowCmd taskFollowCmd;

  @Resource
  private TaskFollowQuery taskFollowQuery;

  @Override
  public IdKey<Long, Object> add(Long taskId) {
    return taskFollowCmd.add(TaskFollowAssembler.addDtoToDomain(taskId));
  }

  @Override
  public void cancel(Long taskId) {
    taskFollowCmd.cancel(taskId);
  }

  @Override
  public void cancelAll(Long projectId) {
    taskFollowCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<TaskFollowDetailVo> list(TaskFollowFindDto dto) {
    judgeMatchFilter(dto);
    Page<TaskFollowP> page = taskFollowQuery.list(dto.getProjectId(),
        dto.getTaskName(), dto.tranPage());
    return buildVoPageResult(page, TaskFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return taskFollowQuery.count(projectId);
  }

}




