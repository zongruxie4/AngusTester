package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.follow;


import cloud.xcan.angus.core.tester.domain.task.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class TaskFollowDetailVo {

  private Long id;

  private Long projectId;

  private Long sprintId;

  private Long taskId;

  private String taskName;

  private TaskType taskType;

  private String taskCode;


}



