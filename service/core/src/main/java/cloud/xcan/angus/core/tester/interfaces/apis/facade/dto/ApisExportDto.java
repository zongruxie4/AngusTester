package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisExportDto {

  @Schema(allowableValues = "yaml, json", description = "OpenAPI document export format specification with YAML as default")
  private SchemaFormat format = SchemaFormat.yaml;
}
