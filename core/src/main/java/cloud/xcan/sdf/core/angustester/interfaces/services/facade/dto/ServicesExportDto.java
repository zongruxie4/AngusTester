package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import cloud.xcan.sdf.core.angustester.domain.services.ServicesExportScope;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesExportDto {

  @NotNull
  @ApiModelProperty(value = "Export data scope", required = true)
  private ServicesExportScope exportScope;

  @ApiModelProperty(notes = "Export apis ids. Specify the apis to export when scope is by `APIS`, export all when not specified.")
  private Set<Long> apiIds;

  @NotEmpty
  @ApiModelProperty(value = "Export services ids", required = true)
  private Set<Long> serviceIds;

  @ApiModelProperty(allowableValues = "yaml, json", notes = "OpenAPI document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

  @ApiModelProperty(notes = "Only return apis associated components, if true, return all components, default false.", example = "false")
  private boolean onlyApisComponents = false;

}




