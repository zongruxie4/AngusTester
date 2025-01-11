package cloud.xcan.sdf.core.angustester.domain.task.summary;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.count.TaskCount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintWorkSummary {

  private TaskSprintSummary summary;

  private TaskCount taskCount;

  private Progress progress;

  private List<UserInfo> members;

  private List<TaskAssigneeWorkSummary> assigneeSummaries = new ArrayList<>();

  private LinkedHashMap<TaskType, Integer> taskByType = new LinkedHashMap<>();

  private LinkedHashMap<TaskStatus, Integer> taskByStatus = new LinkedHashMap<>();

  private LinkedHashMap<Priority, Integer> taskByPriority = new LinkedHashMap<>();

}
