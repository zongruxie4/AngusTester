package cloud.xcan.angus.core.tester.domain.task.count;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingType;
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
public class TaskLastResourceCreationCount {

  @Schema(description = "Total number of backlogs")
  private long allBacklog;
  @Schema(description = "The number of new backlogs added in the last one week")
  private long backlogByLastWeek;
  @Schema(description = "The number of new backlogs added in the last one month")
  private long backlogByLastMonth;
  @Schema(description = "The number of backlogs group by type")
  private LinkedHashMap<TaskType, Integer> backlogByType = new LinkedHashMap<>();

  @Schema(description = "Total number of sprint")
  private long allSprint;
  @Schema(description = "The number of new sprints added in the last one week")
  private long sprintByLastWeek;
  @Schema(description = "The number of new sprints added in the last one month")
  private long sprintByLastMonth;
  @Schema(description = "The number of sprints group by status")
  private LinkedHashMap<TaskSprintStatus, Integer> sprintByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of tasks")
  private long allTask;
  @Schema(description = "The number of overdue tasks")
  private long taskByOverdue;
  @Schema(description = "The number of new tasks added in the last one week")
  private long taskByLastWeek;
  @Schema(description = "The number of new tasks added in the last one month")
  private long taskByLastMonth;
  @Schema(description = "The number of tasks group by type")
  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();
  @Schema(description = "The number of tasks group by status")
  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();
  @Schema(description = "The number of tasks group by priority")
  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

  @Schema(description = "Total number of meetings")
  private long allMeeting;
  @Schema(description = "The number of new meetings added in the last one week")
  private long meetingByLastWeek;
  @Schema(description = "The number of new meetings added in the last one month")
  private long meetingByLastMonth;
  @Schema(description = "The number of meetings group by type")
  private LinkedHashMap<TaskMeetingType, Integer> meetingByType = new LinkedHashMap<>();

}
