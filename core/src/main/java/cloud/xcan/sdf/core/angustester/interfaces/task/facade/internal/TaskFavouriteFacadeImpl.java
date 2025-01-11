package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskFavouriteCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFavouriteQuery;
import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavouriteP;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskFavouriteFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.favorite.TaskFavouriteFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskFavoriteAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.favorite.TaskFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskFavouriteFacadeImpl implements TaskFavouriteFacade {

  @Resource
  private TaskFavouriteCmd taskFavouriteCmd;

  @Resource
  private TaskFavouriteQuery taskFavouriteQuery;

  @Override
  public IdKey<Long, Object> add(Long taskId) {
    return taskFavouriteCmd.add(TaskFavoriteAssembler.addDtoToDomain(taskId));
  }

  @Override
  public void cancel(Long taskId) {
    taskFavouriteCmd.cancel(taskId);
  }

  @Override
  public void cancelAll(Long projectId) {
    taskFavouriteCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<TaskFavouriteDetailVo> search(TaskFavouriteFindDto dto) {
    Page<TaskFavouriteP> pageResult = taskFavouriteQuery.search(
        dto.getProjectId(), dto.getTaskName(), dto.tranPage());
    return buildVoPageResult(pageResult, TaskFavoriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return taskFavouriteQuery.count(projectId);
  }
}




