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
  @Schema(description = "Export data scope configuration for selective data export", requiredMode = RequiredMode.REQUIRED)
  private ServicesExportScope exportScope;

  @Schema(description = "API identifiers for selective export when scope is APIS")
  private Set<Long> apiIds;

  @NotEmpty
  @Schema(description = "Service identifiers for export operation", requiredMode = RequiredMode.REQUIRED)
  private Set<Long> serviceIds;

  @Schema(description = "OpenAPI document format specification for export", allowableValues = "yaml, json")
  private SchemaFormat format = SchemaFormat.yaml;

  @Schema(description = "Flag to include only APIs with associated components", example = "false")
  private boolean onlyApisComponents = false;

}




