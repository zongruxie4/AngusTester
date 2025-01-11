package cloud.xcan.sdf.core.angustester.domain.task.count;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingType;
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
public class TaskLastResourceCreationCount {

  @ApiModelProperty(value = "Total number of backlogs")
  private long allBacklog;
  @ApiModelProperty(value = "The number of new backlogs added in the last one week")
  private long backlogByLastWeek;
  @ApiModelProperty(value = "The number of new backlogs added in the last one month")
  private long backlogByLastMonth;
  @ApiModelProperty(value = "The number of backlogs group by type")
  private LinkedHashMap<TaskType, Integer> backlogByType = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of sprint")
  private long allSprint;
  @ApiModelProperty(value = "The number of new sprints added in the last one week")
  private long sprintByLastWeek;
  @ApiModelProperty(value = "The number of new sprints added in the last one month")
  private long sprintByLastMonth;
  @ApiModelProperty(value = "The number of sprints group by status")
  private LinkedHashMap<TaskSprintStatus, Integer> sprintByStatus = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of tasks")
  private long allTask;
  @ApiModelProperty(value = "The number of overdue tasks")
  private long taskByOverdue;
  @ApiModelProperty(value = "The number of new tasks added in the last one week")
  private long taskByLastWeek;
  @ApiModelProperty(value = "The number of new tasks added in the last one month")
  private long taskByLastMonth;
  @ApiModelProperty(value = "The number of tasks group by type")
  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of tasks group by status")
  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of tasks group by priority")
  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of meetings")
  private long allMeeting;
  @ApiModelProperty(value = "The number of new meetings added in the last one week")
  private long meetingByLastWeek;
  @ApiModelProperty(value = "The number of new meetings added in the last one month")
  private long meetingByLastMonth;
  @ApiModelProperty(value = "The number of meetings group by type")
  private LinkedHashMap<TaskMeetingType, Integer> meetingByType = new LinkedHashMap<>();

}
