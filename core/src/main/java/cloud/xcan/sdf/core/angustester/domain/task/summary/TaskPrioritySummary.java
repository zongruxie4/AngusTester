package cloud.xcan.sdf.core.angustester.domain.task.summary;

import cloud.xcan.sdf.api.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskPrioritySummary {

  private Priority priority;

  private long total;

}
