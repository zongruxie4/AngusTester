package cloud.xcan.sdf.core.angustester.domain.task.summary;

import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
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
