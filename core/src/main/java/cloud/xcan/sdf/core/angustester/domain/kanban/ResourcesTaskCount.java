package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResourcesTaskCount {

  @ApiModelProperty(value = "Total number of sprint")
  private long allSprint;
  @ApiModelProperty(value = "The number of sprints group by status")
  private LinkedHashMap<TaskSprintStatus, Integer> sprintByStatus = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of task")
  private long allTask;
  @ApiModelProperty(value = "The number of tasks group by type")
  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of tasks group by status")
  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of tasks group by priority")
  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of meeting")
  private long allMeeting;

}
