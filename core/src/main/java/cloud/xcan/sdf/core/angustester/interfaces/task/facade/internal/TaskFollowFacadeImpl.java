package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollowP;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskFollowAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
  public PageResult<TaskFollowDetailVo> search(TaskFollowFindDto dto) {
    TaskFollowAssembler.judgeMatchFilter(dto);
    Page<TaskFollowP> pageResult = taskFollowQuery
        .search(dto.getProjectId(), dto.getTaskName(), dto.tranPage());
    return buildVoPageResult(pageResult, TaskFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return taskFollowQuery.count(projectId);
  }

}




