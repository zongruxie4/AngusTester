package cloud.xcan.angus.core.tester.domain.func.summary;

import cloud.xcan.angus.api.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FuncCasePrioritySummary {

  private Priority priority;

  private long total;

}
