package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class MockServiceExportDto {

  @NotNull
  @ApiModelProperty(value = "Export mock service id", required = true)
  private Long mockServiceId;

  @ApiModelProperty(notes = "Export apis ids. Specify the apis to export when scope is by `APIS`, export all when not specified.")
  private Set<Long> mockApiIds;

  @ApiModelProperty(allowableValues = "yaml, json", notes = "Mock apis document format. Available values yaml or json, the default value is yaml.")
  private SchemaFormat format = SchemaFormat.yaml;

}




