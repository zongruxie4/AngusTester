package cloud.xcan.angus.core.tester.domain.issue.summary;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.project.summary.ProjectSummary;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.count.TaskCount;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskProjectWorkSummary {

  private ProjectSummary summary;

  private TaskCount taskCount = new TaskCount();

  private LinkedHashMap<TaskSprintStatus, Integer> sprintByStatus = new LinkedHashMap<>();

  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();

  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();

  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

  private List<TaskSprintWorkSummary> sprintSummaries = new ArrayList<>();

}
