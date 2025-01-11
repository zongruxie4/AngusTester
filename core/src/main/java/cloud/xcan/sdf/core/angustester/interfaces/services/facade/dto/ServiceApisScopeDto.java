package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import cloud.xcan.sdf.core.angustester.domain.services.ServiceApisScope;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Setter
@Getter
public class ServiceApisScopeDto {

  @NotNull
  @ApiModelProperty(value = "Modify parameter API scope, default 'ALL'", required = true)
  private ServiceApisScope scope = ServiceApisScope.ALL;

  @ApiModelProperty(value = "Selected apis ids, it is required when the scope is a `SELECTED_APIS`")
  private Set<Long> selectedApisIds;

  @ApiModelProperty(value = "Match apis regex, it is optional when the scope is a `MATCH_APIS`")
  private String matchEndpointRegex;

  @ApiModelProperty(value = "Match apis regex, it is optional when the scope is a `MATCH_APIS`")
  private HttpMethod matchMethod;

  @ApiModelProperty(value = "Match apis tags, it is optional when the scope is a `MATCH_APIS`")
  private Set<String> filterTags;

}