package cloud.xcan.sdf.core.angustester.domain.apis.summary;

import cloud.xcan.sdf.model.apis.ApiStatus;
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
