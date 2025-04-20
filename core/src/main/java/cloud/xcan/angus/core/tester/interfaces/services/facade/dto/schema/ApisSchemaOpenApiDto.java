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

  @Schema(example = "yaml", allowableValues = "yaml, json",
      description = "OpenAPI document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

  @Schema(example = "true",
      description = "Whether to turn on Gzip compression. It is recommended to enable gzip compression. After enabling it, the data size can be reduced by more than 20 times. By default, gzip compression is enabled.")
  private boolean gzipCompression = true;

  @Schema(description = "Apis ids included. Specify the apis to include, include all when not specified.")
  private Set<Long> apiIds;

  @Schema(example = "false",
      description = "Only return apis associated components, if true, return all components, default false.")
  private boolean onlyApisComponents = false;

}
