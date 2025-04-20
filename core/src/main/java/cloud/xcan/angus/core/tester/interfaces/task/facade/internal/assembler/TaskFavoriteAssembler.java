package cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavourite;
import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.favorite.TaskFavouriteDetailVo;


public class TaskFavoriteAssembler {

  public static TaskFavourite addDtoToDomain(Long taskId) {
    return new TaskFavourite().setTaskId(taskId);
  }

  public static TaskFavouriteDetailVo toDetailVo(TaskFavouriteP favorite) {
    return new TaskFavouriteDetailVo()
        .setId(favorite.getId())
        .setProjectId(favorite.getProjectId())
        .setSprintId(favorite.getSprintId())
        .setTaskId(favorite.getTaskId())
        .setTaskName(favorite.getTaskName())
        .setTaskCode(favorite.getTaskCode())
        .setTaskType(TaskType.valueOf(favorite.getTaskType()));
  }

}




