package cloud.xcan.angus.core.tester.domain.issue.summary;

import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSprintStatusSummary {

  private TaskSprintStatus status;

  private long total;

}
