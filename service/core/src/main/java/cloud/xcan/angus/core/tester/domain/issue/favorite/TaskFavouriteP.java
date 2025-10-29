package cloud.xcan.angus.core.tester.domain.issue.favorite;


public interface TaskFavouriteP {

  Long getId();

  Long getProjectId();

  Long getSprintId();

  Long getTaskId();

  String getTaskName();

  String getTaskType();

  String getTaskCode();
}
