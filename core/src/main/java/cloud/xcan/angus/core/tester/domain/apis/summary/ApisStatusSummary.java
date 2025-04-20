package cloud.xcan.angus.core.tester.domain.apis.summary;

import cloud.xcan.angus.model.apis.ApiStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApisStatusSummary {

  private ApiStatus status;

  private long total;

}
