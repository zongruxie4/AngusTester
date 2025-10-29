package cloud.xcan.angus.core.tester.domain.issue.summary;

import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusSummary {

  private TaskStatus status;

  private long total;

}
