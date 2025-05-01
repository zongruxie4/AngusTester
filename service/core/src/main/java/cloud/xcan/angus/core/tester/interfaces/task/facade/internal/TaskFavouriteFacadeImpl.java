package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskFavoriteAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.task.TaskFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskFavouriteFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.favorite.TaskFavouriteFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskFavoriteAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.favorite.TaskFavouriteDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
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
    return taskFavouriteCmd.add(addDtoToDomain(taskId));
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
    Page<TaskFavouriteP> page = taskFavouriteQuery.search(
        dto.getProjectId(), dto.getTaskName(), dto.tranPage());
    return buildVoPageResult(page, TaskFavoriteAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return taskFavouriteQuery.count(projectId);
  }
}




