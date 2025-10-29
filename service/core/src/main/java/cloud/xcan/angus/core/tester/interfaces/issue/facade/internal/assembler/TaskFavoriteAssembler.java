package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavourite;
import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavouriteP;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.favorite.TaskFavouriteDetailVo;


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




