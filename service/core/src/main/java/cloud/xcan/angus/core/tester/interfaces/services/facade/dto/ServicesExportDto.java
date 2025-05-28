package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import cloud.xcan.angus.core.tester.domain.services.ServicesExportScope;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ServicesExportDto {

  @NotNull
  @Schema(description = "Export data scope", requiredMode = RequiredMode.REQUIRED)
  private ServicesExportScope exportScope;

  @Schema(description = "Export apis ids. Specify the apis to export when scope is by `APIS`, export all when not specified.")
  private Set<Long> apiIds;

  @NotEmpty
  @Schema(description = "Export services ids", requiredMode = RequiredMode.REQUIRED)
  private Set<Long> serviceIds;

  @Schema(allowableValues = "yaml, json", description = "OpenAPI document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

  @Schema(description = "Only return apis associated components, if true, return all components, default false.", example = "false")
  private boolean onlyApisComponents = false;

}




