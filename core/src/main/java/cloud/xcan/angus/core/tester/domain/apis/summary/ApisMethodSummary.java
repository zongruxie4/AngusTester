package cloud.xcan.angus.core.tester.domain.apis.summary;

import cloud.xcan.angus.spec.http.HttpMethod;
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
