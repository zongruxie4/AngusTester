package cloud.xcan.sdf.core.angustester.domain.apis.summary;

import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
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
