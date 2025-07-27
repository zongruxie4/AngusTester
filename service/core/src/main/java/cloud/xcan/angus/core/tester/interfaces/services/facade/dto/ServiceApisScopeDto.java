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
  @Schema(description = "API scope configuration for selective operation targeting", requiredMode = RequiredMode.REQUIRED)
  private ServiceApisScope scope = ServiceApisScope.ALL;

  @Schema(description = "Selected API identifiers for targeted scope operations")
  private Set<Long> selectedApisIds;

  @Schema(description = "Endpoint regex pattern for API matching operations")
  private String matchEndpointRegex;

  @Schema(description = "HTTP method for API matching operations")
  private HttpMethod matchMethod;

  @Schema(description = "API tags for filtering operations")
  private Set<String> filterTags;

}
