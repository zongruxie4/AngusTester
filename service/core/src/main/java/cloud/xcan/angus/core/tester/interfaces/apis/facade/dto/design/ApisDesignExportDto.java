package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisDesignExportDto {

  @NotNull
  @Schema(description = "Design ID", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(allowableValues = "yaml, json", description = "OpenAPI document format. Available values yaml or json, the default value is yaml")
  private SchemaFormat format = SchemaFormat.yaml;
}




