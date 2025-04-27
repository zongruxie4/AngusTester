package cloud.xcan.angus.core.tester.domain.apis.summary;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.spec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApisBasicStatusSummary {

  public ApiSource source;

  public ApiImportSource importSource;

  public HttpMethod method;

  public ApiStatus status;

}
