package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.schema;

import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisSchemaOpenApiDto {

  @ApiModelProperty(example = "yaml", allowableValues = "yaml, json", notes = "OpenAPI document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

  @ApiModelProperty(example = "true", notes = "Whether to turn on Gzip compression. It is recommended to enable gzip compression. After enabling it, the data size can be reduced by more than 20 times. By default, gzip compression is enabled.")
  private Boolean gzipCompression = true;

}
