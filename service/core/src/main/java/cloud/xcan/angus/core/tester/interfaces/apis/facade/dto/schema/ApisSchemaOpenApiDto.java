package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.schema;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisSchemaOpenApiDto {

  @Schema(example = "yaml", allowableValues = "yaml, json", description = "OpenAPI document format specification with YAML as default")
  private SchemaFormat format = SchemaFormat.yaml;

  @Schema(example = "true", description = "Gzip compression enablement flag for data size reduction, defaults to enabled")
  private Boolean gzipCompression = true;

}
