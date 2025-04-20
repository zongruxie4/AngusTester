package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResourcesTaskCount {

  @Schema(description = "Total number of sprint")
  private long allSprint;
  @Schema(description = "The number of sprints group by status")
  private LinkedHashMap<TaskSprintStatus, Integer> sprintByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of task")
  private long allTask;
  @Schema(description = "The number of tasks group by type")
  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();
  @Schema(description = "The number of tasks group by status")
  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();
  @Schema(description = "The number of tasks group by priority")
  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

  @Schema(description = "Total number of meeting")
  private long allMeeting;

}
