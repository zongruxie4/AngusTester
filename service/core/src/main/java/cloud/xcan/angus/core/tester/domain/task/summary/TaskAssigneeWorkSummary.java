package cloud.xcan.angus.core.tester.domain.task.summary;

import cloud.xcan.angus.core.tester.domain.task.count.TaskAssigneeCount;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskAssigneeWorkSummary {

  private Long assigneeId;

  private String assigneeName;

  private String assigneeAvatar;

  private TaskAssigneeCount count;

  private Map<String, List<TaskSummary>> groupByDay = new TreeMap<>();

}
