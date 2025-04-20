package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class MockServiceExportDto {

  @NotNull
  @Schema(description = "Export mock service id", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @Schema(description = "Export apis ids. Specify the apis to export when scope is by `APIS`, export all when not specified.")
  private Set<Long> mockApiIds;

  @Schema(allowableValues = "yaml, json", description = "Mock apis document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

}




