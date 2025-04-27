package cloud.xcan.angus.core.tester.domain.task.summary;

import cloud.xcan.angus.core.tester.domain.task.TaskType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskTypeSummary {

  private TaskType type;

  private long total;

}
