package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class MockServiceExportDto {

  @NotNull
  @Schema(description = "Mock service identifier for export operation", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @Schema(description = "API identifiers to export; if scope is 'APIS', specify the APIs to export. If not specified, all APIs will be exported.")
  private Set<Long> mockApiIds;

  @Schema(allowableValues = "yaml, json", description = "Export document format for mock APIs. Available values: yaml or json. Default is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

}




