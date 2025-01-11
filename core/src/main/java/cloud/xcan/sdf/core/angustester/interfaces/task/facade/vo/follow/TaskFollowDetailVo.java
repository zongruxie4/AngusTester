package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.follow;


import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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



