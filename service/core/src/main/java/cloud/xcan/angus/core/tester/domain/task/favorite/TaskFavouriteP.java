package cloud.xcan.angus.core.tester.domain.task.favorite;


public interface TaskFavouriteP {

  Long getId();

  Long getProjectId();

  Long getSprintId();

  Long getTaskId();

  String getTaskName();

  String getTaskType();

  String getTaskCode();
}
