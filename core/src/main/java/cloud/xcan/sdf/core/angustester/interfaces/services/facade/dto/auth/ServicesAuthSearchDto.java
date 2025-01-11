package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
public class ServicesAuthSearchDto extends PageQuery {

  @ApiModelProperty
  private Long id;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Long serviceId;

  @NotNull
  @ApiModelProperty(example = "USER", value = "Authorization object type", required = true)
  private AuthObjectType authObjectType;

}



