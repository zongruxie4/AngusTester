package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto;

import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisExportDto {

  @ApiModelProperty(allowableValues = "yaml, json", notes = "OpenAPI document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;
}




