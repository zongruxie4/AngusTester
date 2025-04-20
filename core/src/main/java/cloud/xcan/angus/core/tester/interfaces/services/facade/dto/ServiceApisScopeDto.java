package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import cloud.xcan.angus.core.tester.domain.services.ServiceApisScope;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ServiceApisScopeDto {

  @NotNull
  @Schema(description = "Modify parameter API scope, default 'ALL'", requiredMode = RequiredMode.REQUIRED)
  private ServiceApisScope scope = ServiceApisScope.ALL;

  @Schema(description = "Selected apis ids, it is required when the scope is a `SELECTED_APIS`")
  private Set<Long> selectedApisIds;

  @Schema(description = "Match apis regex, it is optional when the scope is a `MATCH_APIS`")
  private String matchEndpointRegex;

  @Schema(description = "Match apis regex, it is optional when the scope is a `MATCH_APIS`")
  private HttpMethod matchMethod;

  @Schema(description = "Match apis tags, it is optional when the scope is a `MATCH_APIS`")
  private Set<String> filterTags;

}
