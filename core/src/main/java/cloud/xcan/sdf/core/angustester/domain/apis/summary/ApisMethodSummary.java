package cloud.xcan.sdf.core.angustester.domain.apis.summary;

import cloud.xcan.sdf.spec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApisMethodSummary {

  private HttpMethod method;

  private long total;

}
