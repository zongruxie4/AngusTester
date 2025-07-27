package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.schema;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisSchemaOpenApiDto {

  @Schema(description = "OpenAPI document format specification for schema generation", example = "yaml", allowableValues = "yaml, json")
  private SchemaFormat format = SchemaFormat.yaml;

  @Schema(description = "Gzip compression flag for data size optimization", example = "true")
  private boolean gzipCompression = true;

  @Schema(description = "API identifiers for selective schema generation")
  private Set<Long> apiIds;

  @Schema(description = "Flag to include only APIs with associated components", example = "false")
  private boolean onlyApisComponents = false;

}
